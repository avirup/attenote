package com.uteacher.attendancetracker.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.uteacher.attendancetracker.data.local.AppDatabase
import com.uteacher.attendancetracker.data.repository.internal.RepositoryResult
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.time.Instant
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class BackupSupportRepositoryImpl(
    private val db: AppDatabase,
    private val dataStore: DataStore<Preferences>,
    private val context: Context
) : BackupSupportRepository {

    override suspend fun exportBackup(destinationUri: Uri): RepositoryResult<Unit> = withContext(Dispatchers.IO) {
        val destinationStream = context.contentResolver.openOutputStream(destinationUri)
            ?: return@withContext RepositoryResult.Error("Unable to open destination Uri")

        val checksumByPath = linkedMapOf<String, String>()
        try {
            checkpointAndCloseDatabase()

            ZipOutputStream(BufferedOutputStream(destinationStream)).use { zip ->
                val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
                if (!dbFile.exists()) {
                    return@withContext RepositoryResult.Error("Database file not found")
                }
                addFileToZip(zip, dbFile, "attenote.db", checksumByPath)

                val datastoreDir = File(context.filesDir, DATASTORE_DIR)
                if (datastoreDir.exists()) {
                    datastoreDir.walkTopDown()
                        .filter { it.isFile && it.name.endsWith(DATASTORE_SUFFIX) }
                        .forEach { file ->
                            val relative = file.relativeTo(datastoreDir).invariantSeparatorsPath
                            addFileToZip(zip, file, "$DATASTORE_DIR/$relative", checksumByPath)
                        }
                }

                val imagesDir = File(context.filesDir, IMAGES_DIR)
                if (imagesDir.exists()) {
                    imagesDir.walkTopDown()
                        .filter { it.isFile }
                        .forEach { file ->
                            val relative = file.relativeTo(imagesDir).invariantSeparatorsPath
                            addFileToZip(zip, file, "$IMAGES_DIR/$relative", checksumByPath)
                        }
                }

                val manifest = createManifestJson(checksumByPath)
                zip.putNextEntry(ZipEntry(MANIFEST_FILE))
                zip.write(manifest.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }

            RepositoryResult.Success(Unit)
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to export backup: ${e.message}")
        } finally {
            reopenDatabase()
        }
    }

    override suspend fun importBackup(sourceUri: Uri): RepositoryResult<Unit> = withContext(Dispatchers.IO) {
        val extracted = runCatching { extractBackup(sourceUri) }
            .getOrElse { return@withContext RepositoryResult.Error("Failed to read backup: ${it.message}") }

        try {
            validateManifest(extracted.manifest, extracted.files)

            writeRestoreJournal(phase = JOURNAL_PHASE_EXTRACTING)
            checkpointAndCloseDatabase()

            backupLiveData()
            writeRestoreJournal(phase = JOURNAL_PHASE_SWAPPING)

            restoreExtractedData(extracted)

            writeRestoreJournal(phase = JOURNAL_PHASE_COMPLETE)
            cleanupBackupData()
            restoreJournalFile().delete()
            RepositoryResult.Success(Unit)
        } catch (e: Exception) {
            runCatching { rollbackFromBackupData() }
            RepositoryResult.Error("Failed to import backup: ${e.message}")
        } finally {
            reopenDatabase()
            extracted.stagingDir.deleteRecursively()
        }
    }

    override suspend fun hasInterruptedRestore(): Boolean = withContext(Dispatchers.IO) {
        val journal = restoreJournalFile()
        if (!journal.exists()) {
            return@withContext false
        }

        val phase = runCatching {
            JSONObject(journal.readText()).optString("phase", "")
        }.getOrDefault("")

        phase.isNotBlank() && phase != JOURNAL_PHASE_COMPLETE
    }

    override suspend fun completeInterruptedRestore(): RepositoryResult<Unit> = withContext(Dispatchers.IO) {
        val journal = restoreJournalFile()
        if (!journal.exists()) {
            return@withContext RepositoryResult.Success(Unit)
        }

        return@withContext try {
            val phase = JSONObject(journal.readText()).optString("phase", "")
            if (phase == JOURNAL_PHASE_COMPLETE) {
                cleanupBackupData()
            } else {
                rollbackFromBackupData()
                cleanupBackupData()
            }
            journal.delete()
            RepositoryResult.Success(Unit)
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to complete interrupted restore: ${e.message}")
        } finally {
            reopenDatabase()
        }
    }

    private fun checkpointAndCloseDatabase() {
        runCatching { db.openHelper.writableDatabase.execSQL("PRAGMA wal_checkpoint(TRUNCATE)") }
        db.close()
    }

    private fun reopenDatabase() {
        runCatching { db.openHelper.writableDatabase }
        runCatching { dataStore.data }
    }

    private fun createManifestJson(checksumByPath: Map<String, String>): String {
        val checksumsJson = JSONObject().apply {
            checksumByPath.forEach { (path, checksum) ->
                put(path, "sha256:$checksum")
            }
        }

        val isDebugBuild = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val appVersion = appVersionName()

        return JSONObject()
            .put("applicationId", context.packageName)
            .put("buildVariant", if (isDebugBuild) "debug" else "release")
            .put("appVersion", appVersion)
            .put("schemaVersion", SCHEMA_VERSION)
            .put("timestamp", Instant.now().toString())
            .put("fileChecksums", checksumsJson)
            .toString()
    }

    private fun addFileToZip(
        zip: ZipOutputStream,
        file: File,
        entryName: String,
        checksumByPath: MutableMap<String, String>
    ) {
        zip.putNextEntry(ZipEntry(entryName))
        FileInputStream(file).use { input ->
            input.copyTo(zip)
        }
        zip.closeEntry()
        checksumByPath[entryName] = sha256(file)
    }

    private fun extractBackup(sourceUri: Uri): ExtractedBackup {
        val input = context.contentResolver.openInputStream(sourceUri)
            ?: throw IllegalStateException("Unable to open backup source Uri")

        val stagingDir = File(context.cacheDir, "backup_import_${System.currentTimeMillis()}")
        stagingDir.mkdirs()

        val extractedFiles = linkedMapOf<String, File>()
        var manifestJson: JSONObject? = null

        ZipInputStream(BufferedInputStream(input)).use { zipInput ->
            var entry: ZipEntry? = zipInput.nextEntry
            while (entry != null) {
                val entryName = entry.name.replace('\\', '/')
                if (!entry.isDirectory) {
                    if (entryName == MANIFEST_FILE) {
                        manifestJson = JSONObject(zipInput.readBytes().decodeToString())
                    } else if (
                        entryName == "attenote.db" ||
                        entryName.startsWith("$DATASTORE_DIR/") ||
                        entryName.startsWith("$IMAGES_DIR/")
                    ) {
                        val outFile = safeOutputFile(stagingDir, entryName)
                        outFile.parentFile?.mkdirs()
                        FileOutputStream(outFile).use { output ->
                            zipInput.copyTo(output)
                        }
                        extractedFiles[entryName] = outFile
                    }
                }
                zipInput.closeEntry()
                entry = zipInput.nextEntry
            }
        }

        if (!extractedFiles.containsKey("attenote.db")) {
            throw IllegalStateException("Backup archive does not contain attenote.db")
        }

        return ExtractedBackup(
            stagingDir = stagingDir,
            files = extractedFiles,
            manifest = manifestJson
        )
    }

    private fun validateManifest(
        manifest: JSONObject?,
        extractedFiles: Map<String, File>
    ) {
        val actualManifest = manifest ?: return

        val appId = actualManifest.optString("applicationId")
        if (appId.isNotBlank() && appId != context.packageName) {
            throw IllegalStateException("Backup app id mismatch: $appId")
        }

        val schemaVersion = actualManifest.optInt("schemaVersion", SCHEMA_VERSION)
        if (schemaVersion != SCHEMA_VERSION) {
            throw IllegalStateException("Unsupported schema version: $schemaVersion")
        }

        val checksums = actualManifest.optJSONObject("fileChecksums") ?: return
        val keys = checksums.keys()
        while (keys.hasNext()) {
            val path = keys.next()
            val expected = checksums.optString(path).removePrefix("sha256:")
            val actual = extractedFiles[path]?.let(::sha256)
                ?: throw IllegalStateException("Missing file from backup archive: $path")
            if (!expected.equals(actual, ignoreCase = true)) {
                throw IllegalStateException("Checksum mismatch for $path")
            }
        }
    }

    private fun restoreExtractedData(extracted: ExtractedBackup) {
        val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
        dbFile.parentFile?.mkdirs()
        copyFile(extracted.files.getValue("attenote.db"), dbFile)

        val datastoreStaged = File(extracted.stagingDir, DATASTORE_DIR)
        val datastoreLive = File(context.filesDir, DATASTORE_DIR)
        if (datastoreStaged.exists()) {
            datastoreLive.deleteRecursively()
            copyDirectory(datastoreStaged, datastoreLive)
        }

        val imagesStaged = File(extracted.stagingDir, IMAGES_DIR)
        val imagesLive = File(context.filesDir, IMAGES_DIR)
        if (imagesStaged.exists()) {
            imagesLive.deleteRecursively()
            copyDirectory(imagesStaged, imagesLive)
        }
    }

    private fun backupLiveData() {
        val backupRoot = backupRootDir()
        backupRoot.deleteRecursively()
        backupRoot.mkdirs()

        val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
        if (dbFile.exists()) {
            copyFile(dbFile, File(backupRoot, "db/${AppDatabase.DATABASE_NAME}"))
        }

        val datastoreDir = File(context.filesDir, DATASTORE_DIR)
        if (datastoreDir.exists()) {
            copyDirectory(datastoreDir, File(backupRoot, DATASTORE_DIR))
        }

        val imagesDir = File(context.filesDir, IMAGES_DIR)
        if (imagesDir.exists()) {
            copyDirectory(imagesDir, File(backupRoot, IMAGES_DIR))
        }
    }

    private fun rollbackFromBackupData() {
        val backupRoot = backupRootDir()
        if (!backupRoot.exists()) return

        val backedUpDb = File(backupRoot, "db/${AppDatabase.DATABASE_NAME}")
        if (backedUpDb.exists()) {
            val liveDb = context.getDatabasePath(AppDatabase.DATABASE_NAME)
            liveDb.parentFile?.mkdirs()
            copyFile(backedUpDb, liveDb)
        }

        val backedUpDataStore = File(backupRoot, DATASTORE_DIR)
        if (backedUpDataStore.exists()) {
            val liveDataStore = File(context.filesDir, DATASTORE_DIR)
            liveDataStore.deleteRecursively()
            copyDirectory(backedUpDataStore, liveDataStore)
        }

        val backedUpImages = File(backupRoot, IMAGES_DIR)
        if (backedUpImages.exists()) {
            val liveImages = File(context.filesDir, IMAGES_DIR)
            liveImages.deleteRecursively()
            copyDirectory(backedUpImages, liveImages)
        }
    }

    private fun cleanupBackupData() {
        backupRootDir().deleteRecursively()
    }

    private fun writeRestoreJournal(phase: String) {
        val journal = JSONObject()
            .put("phase", phase)
            .put("timestamp", Instant.now().toString())
        restoreJournalFile().writeText(journal.toString())
    }

    private fun restoreJournalFile(): File = File(context.filesDir, RESTORE_JOURNAL_FILE)

    private fun backupRootDir(): File = File(context.cacheDir, RESTORE_BACKUP_DIR)

    private fun safeOutputFile(root: File, entryName: String): File {
        val outFile = File(root, entryName)
        val rootPath = root.canonicalPath
        val outPath = outFile.canonicalPath
        if (!outPath.startsWith(rootPath + File.separator)) {
            throw IllegalStateException("Unsafe zip entry path: $entryName")
        }
        return outFile
    }

    private fun copyDirectory(sourceDir: File, targetDir: File) {
        sourceDir.walkTopDown().forEach { source ->
            val relative = source.relativeTo(sourceDir).path
            val target = if (relative.isEmpty()) targetDir else File(targetDir, relative)
            if (source.isDirectory) {
                target.mkdirs()
            } else {
                copyFile(source, target)
            }
        }
    }

    private fun copyFile(source: File, target: File) {
        target.parentFile?.mkdirs()
        source.inputStream().use { input ->
            target.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val bytesRead = input.read(buffer)
                if (bytesRead <= 0) break
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    @Suppress("DEPRECATION")
    private fun appVersionName(): String {
        return runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown"
        }.getOrDefault("unknown")
    }

    private data class ExtractedBackup(
        val stagingDir: File,
        val files: Map<String, File>,
        val manifest: JSONObject?
    )

    private companion object {
        const val DATASTORE_DIR = "datastore"
        const val DATASTORE_SUFFIX = ".preferences_pb"
        const val IMAGES_DIR = "app_images"
        const val MANIFEST_FILE = "backup_manifest.json"
        const val RESTORE_JOURNAL_FILE = "restore_journal.json"
        const val RESTORE_BACKUP_DIR = "restore_backup"
        const val SCHEMA_VERSION = 1
        const val JOURNAL_PHASE_EXTRACTING = "extracting"
        const val JOURNAL_PHASE_SWAPPING = "swapping"
        const val JOURNAL_PHASE_COMPLETE = "complete"
    }
}
