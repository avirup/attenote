package com.uteacher.attenote.data.repository

import android.content.Context
import android.content.ContentValues
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.uteacher.attenote.data.local.AppDatabase
import com.uteacher.attenote.data.repository.internal.RepositoryResult
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
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

    override suspend fun exportBackup(): RepositoryResult<String> = withContext(Dispatchers.IO) {
        val tempDir = File(context.cacheDir, EXPORTS_DIR).apply { mkdirs() }
        val zipFile = File(tempDir, "attenote_backup_${System.currentTimeMillis()}.zip")
        val checksumByEntry = linkedMapOf<String, String>()

        return@withContext try {
            checkpointAndCloseDatabase()

            ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zip ->
                val liveDbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
                if (!liveDbFile.exists()) {
                    return@withContext RepositoryResult.Error("Database file not found")
                }
                addFileToZip(zip, liveDbFile, DB_BACKUP_ENTRY, checksumByEntry)

                val liveDataStoreFile = File(context.filesDir, "$DATASTORE_DIR/$DATASTORE_FILE_NAME")
                if (liveDataStoreFile.exists()) {
                    addFileToZip(zip, liveDataStoreFile, DATASTORE_BACKUP_ENTRY, checksumByEntry)
                }

                addDirectoryFilesToZip(
                    zip = zip,
                    sourceDir = File(context.filesDir, APP_IMAGES_DIR),
                    entryPrefix = "$APP_IMAGES_DIR/",
                    checksumByEntry = checksumByEntry
                )
                addDirectoryFilesToZip(
                    zip = zip,
                    sourceDir = File(context.filesDir, NOTE_MEDIA_DIR),
                    entryPrefix = "$NOTE_MEDIA_DIR/",
                    checksumByEntry = checksumByEntry
                )

                val manifestJson = createManifestJson(checksumByEntry)
                zip.putNextEntry(ZipEntry(MANIFEST_FILE))
                zip.write(manifestJson.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }

            val publishedLocation = publishBackupToDownloads(zipFile)
            RepositoryResult.Success(publishedLocation)
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to export backup: ${e.message}")
        } finally {
            runCatching { zipFile.delete() }
            reopenDatabase()
        }
    }

    override suspend fun importBackup(sourceUri: Uri): RepositoryResult<Unit> = withContext(Dispatchers.IO) {
        val extractedBackup = runCatching { extractBackup(sourceUri) }
            .getOrElse {
                return@withContext RepositoryResult.Error("Failed to read backup: ${it.message}")
            }

        return@withContext try {
            validateManifest(extractedBackup.manifest, extractedBackup.files)

            val stagedPaths = mutableMapOf(
                KEY_DB to extractedBackup.databaseFile.absolutePath,
                KEY_APP_IMAGES to extractedBackup.appImagesDir.absolutePath,
                KEY_NOTE_MEDIA to extractedBackup.noteMediaDir.absolutePath
            ).apply {
                extractedBackup.datastoreFile?.let { put(KEY_DATASTORE, it.absolutePath) }
            }
            writeRestoreJournal(
                RestoreJournal(
                    phase = RestorePhase.EXTRACTING,
                    timestamp = System.currentTimeMillis(),
                    stagedPaths = stagedPaths
                )
            )

            checkpointAndCloseDatabase()

            val backupPaths = moveLiveToBackupOld()
            writeRestoreJournal(
                RestoreJournal(
                    phase = RestorePhase.SWAPPING,
                    timestamp = System.currentTimeMillis(),
                    stagedPaths = stagedPaths,
                    backupPaths = backupPaths
                )
            )

            moveStagedToLive(extractedBackup)

            writeRestoreJournal(
                RestoreJournal(
                    phase = RestorePhase.COMPLETED,
                    timestamp = System.currentTimeMillis(),
                    stagedPaths = stagedPaths,
                    backupPaths = backupPaths
                )
            )

            cleanupAfterSuccessfulRestore()
            RepositoryResult.Success(Unit)
        } catch (e: Exception) {
            val journalAtFailure = readRestoreJournal()
            runCatching {
                rollbackFromBackup(journalAtFailure?.backupPaths.orEmpty())
                writeRestoreJournal(
                    RestoreJournal(
                        phase = RestorePhase.ROLLBACK_ATTEMPTED,
                        timestamp = System.currentTimeMillis(),
                        stagedPaths = emptyMap(),
                        backupPaths = journalAtFailure?.backupPaths.orEmpty()
                    )
                )
            }
            RepositoryResult.Error("Failed to import backup: ${e.message}")
        } finally {
            reopenDatabase()
        }
    }

    override suspend fun checkAndRecoverInterruptedRestore(): RepositoryResult<Unit> =
        withContext(Dispatchers.IO) {
            val journal = readRestoreJournal() ?: return@withContext RepositoryResult.Success(Unit)

            return@withContext try {
                when (journal.phase) {
                    RestorePhase.EXTRACTING -> {
                        // Extraction didn't finish swapping, just clear staged leftovers.
                        clearStagingDirectory()
                    }

                    RestorePhase.SWAPPING -> {
                        // Prefer reverting to backup_old state whenever available.
                        val backupPaths = journal.backupPaths.ifEmpty { discoverBackupPathsFromDisk() }
                        rollbackFromBackup(backupPaths)
                        clearStagingDirectory()
                        clearBackupOldDirectory()
                    }

                    RestorePhase.COMPLETED -> {
                        clearBackupOldDirectory()
                        clearStagingDirectory()
                    }

                    RestorePhase.ROLLBACK_ATTEMPTED -> {
                        // Keep live data as-is; clear bookkeeping artifacts.
                        clearStagingDirectory()
                        clearBackupOldDirectory()
                    }
                }
                restoreJournalFile().delete()
                reopenDatabase()
                RepositoryResult.Success(Unit)
            } catch (e: Exception) {
                RepositoryResult.Error("Failed to recover interrupted restore: ${e.message}")
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

    private fun createManifestJson(fileChecksums: Map<String, String>): String {
        val checksumsJson = JSONObject().apply {
            fileChecksums.forEach { (entryPath, checksum) ->
                put(entryPath, checksum)
            }
        }

        val isDebugBuild = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

        return JSONObject()
            .put("applicationId", context.packageName)
            .put("buildVariant", if (isDebugBuild) "debug" else "release")
            .put("appVersion", appVersionName())
            .put("schemaVersion", SCHEMA_VERSION)
            .put("timestamp", System.currentTimeMillis())
            .put("fileChecksums", checksumsJson)
            .toString()
    }

    private fun addDirectoryFilesToZip(
        zip: ZipOutputStream,
        sourceDir: File,
        entryPrefix: String,
        checksumByEntry: MutableMap<String, String>
    ) {
        if (!sourceDir.exists()) return
        sourceDir.walkTopDown()
            .filter { it.isFile }
            .forEach { file ->
                val relative = file.relativeTo(sourceDir).invariantSeparatorsPath
                addFileToZip(zip, file, "$entryPrefix$relative", checksumByEntry)
            }
    }

    private fun addFileToZip(
        zip: ZipOutputStream,
        sourceFile: File,
        entryName: String,
        checksumByEntry: MutableMap<String, String>
    ) {
        zip.putNextEntry(ZipEntry(entryName))
        FileInputStream(sourceFile).use { input ->
            input.copyTo(zip)
        }
        zip.closeEntry()
        checksumByEntry[entryName] = sha256(sourceFile)
    }

    private fun publishBackupToDownloads(tempZip: File): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, tempZip.name)
                put(MediaStore.Downloads.MIME_TYPE, "application/zip")
                put(
                    MediaStore.Downloads.RELATIVE_PATH,
                    "${Environment.DIRECTORY_DOWNLOADS}/$PUBLIC_EXPORT_DIR"
                )
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: throw IllegalStateException("Failed to create backup file in Downloads")

            try {
                resolver.openOutputStream(uri)?.use { output ->
                    tempZip.inputStream().use { input -> input.copyTo(output) }
                } ?: throw IllegalStateException("Failed to write backup file")
                val publish = ContentValues().apply {
                    put(MediaStore.Downloads.IS_PENDING, 0)
                }
                resolver.update(uri, publish, null, null)
                return uri.toString()
            } catch (e: Exception) {
                resolver.delete(uri, null, null)
                throw e
            }
        }

        @Suppress("DEPRECATION")
        val downloadsRoot = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            ?: throw IllegalStateException("Downloads directory unavailable")
        val outDir = File(downloadsRoot, PUBLIC_EXPORT_DIR).apply { mkdirs() }
        val outFile = File(outDir, tempZip.name)
        tempZip.copyTo(outFile, overwrite = true)
        return outFile.absolutePath
    }

    private fun extractBackup(sourceUri: Uri): ExtractedBackup {
        val sourceStream = context.contentResolver.openInputStream(sourceUri)
            ?: throw IllegalStateException("Unable to open backup source uri")

        val stagingRoot = restoreStagingRoot()
        stagingRoot.deleteRecursively()
        stagingRoot.mkdirs()

        val extractedFiles = linkedMapOf<String, File>()
        var manifestJson: JSONObject? = null

        ZipInputStream(BufferedInputStream(sourceStream)).use { zipInput ->
            var entry = zipInput.nextEntry
            while (entry != null) {
                val entryName = entry.name.replace('\\', '/')
                if (!entry.isDirectory) {
                    when {
                        entryName == MANIFEST_FILE -> {
                            manifestJson = JSONObject(zipInput.readBytes().decodeToString())
                        }

                        entryName == DB_BACKUP_ENTRY ||
                            entryName == DATASTORE_BACKUP_ENTRY ||
                            entryName.startsWith("$APP_IMAGES_DIR/") ||
                            entryName.startsWith("$NOTE_MEDIA_DIR/") -> {
                            val outputFile = safeOutputFile(stagingRoot, entryName)
                            outputFile.parentFile?.mkdirs()
                            FileOutputStream(outputFile).use { output ->
                                zipInput.copyTo(output)
                            }
                            extractedFiles[entryName] = outputFile
                        }
                    }
                }
                zipInput.closeEntry()
                entry = zipInput.nextEntry
            }
        }

        val stagedDbFile = extractedFiles[DB_BACKUP_ENTRY]
            ?: throw IllegalStateException("Backup archive missing $DB_BACKUP_ENTRY")
        val stagedAppImagesDir = File(stagingRoot, APP_IMAGES_DIR)
        val stagedNoteMediaDir = File(stagingRoot, NOTE_MEDIA_DIR)

        return ExtractedBackup(
            stagingRoot = stagingRoot,
            files = extractedFiles,
            manifest = manifestJson,
            databaseFile = stagedDbFile,
            datastoreFile = extractedFiles[DATASTORE_BACKUP_ENTRY],
            appImagesDir = stagedAppImagesDir,
            noteMediaDir = stagedNoteMediaDir
        )
    }

    private fun validateManifest(
        manifest: JSONObject?,
        extractedFiles: Map<String, File>
    ) {
        val actualManifest = manifest ?: throw IllegalStateException("Missing $MANIFEST_FILE")

        val appId = actualManifest.optString("applicationId")
        if (appId != context.packageName) {
            throw IllegalStateException("Backup applicationId mismatch: $appId")
        }

        val expectedVariant = if ((context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            "debug"
        } else {
            "release"
        }
        val manifestVariant = actualManifest.optString("buildVariant")
        if (manifestVariant.isNotBlank() && manifestVariant != expectedVariant) {
            Log.w(TAG, "Backup build variant mismatch ($manifestVariant), continuing restore")
        }

        val schemaVersion = actualManifest.optInt("schemaVersion", -1)
        if (schemaVersion != SCHEMA_VERSION) {
            throw IllegalStateException("Unsupported schemaVersion: $schemaVersion")
        }

        val checksumsJson = actualManifest.optJSONObject("fileChecksums")
            ?: throw IllegalStateException("Manifest missing fileChecksums")
        REQUIRED_MANIFEST_ENTRIES.forEach { requiredEntry ->
            if (!checksumsJson.has(requiredEntry)) {
                throw IllegalStateException("Manifest missing required entry: $requiredEntry")
            }
        }

        val manifestEntries = mutableSetOf<String>()
        val keys = checksumsJson.keys()
        while (keys.hasNext()) {
            val entry = keys.next()
            manifestEntries += entry
            val expectedChecksum = checksumsJson.optString(entry)
            if (expectedChecksum.isBlank()) {
                throw IllegalStateException("Manifest checksum missing for $entry")
            }
            val extractedFile = extractedFiles[entry]
                ?: throw IllegalStateException("Manifest references missing file: $entry")
            val actualChecksum = sha256(extractedFile)
            if (!expectedChecksum.equals(actualChecksum, ignoreCase = true)) {
                throw IllegalStateException("Checksum mismatch for $entry")
            }
        }

        extractedFiles.keys
            .filterNot(manifestEntries::contains)
            .forEach { entry ->
                Log.w(TAG, "Ignoring extra backup entry not present in manifest: $entry")
            }
    }

    private fun moveLiveToBackupOld(): Map<String, String> {
        val backupRoot = backupOldRoot()
        backupRoot.deleteRecursively()
        backupRoot.mkdirs()

        val map = mutableMapOf<String, String>()

        val liveDb = context.getDatabasePath(AppDatabase.DATABASE_NAME)
        if (liveDb.exists()) {
            val backupDb = File(backupRoot, "db/${AppDatabase.DATABASE_NAME}")
            movePath(liveDb, backupDb)
            map[KEY_DB] = backupDb.absolutePath
        }

        val liveDataStore = File(context.filesDir, DATASTORE_DIR)
        if (liveDataStore.exists()) {
            val backupDataStore = File(backupRoot, DATASTORE_DIR)
            movePath(liveDataStore, backupDataStore)
            map[KEY_DATASTORE] = backupDataStore.absolutePath
        }

        val liveImages = File(context.filesDir, APP_IMAGES_DIR)
        if (liveImages.exists()) {
            val backupImages = File(backupRoot, APP_IMAGES_DIR)
            movePath(liveImages, backupImages)
            map[KEY_APP_IMAGES] = backupImages.absolutePath
        }

        val liveNoteMedia = File(context.filesDir, NOTE_MEDIA_DIR)
        if (liveNoteMedia.exists()) {
            val backupNoteMedia = File(backupRoot, NOTE_MEDIA_DIR)
            movePath(liveNoteMedia, backupNoteMedia)
            map[KEY_NOTE_MEDIA] = backupNoteMedia.absolutePath
        }

        return map
    }

    private fun moveStagedToLive(extracted: ExtractedBackup) {
        val liveDb = context.getDatabasePath(AppDatabase.DATABASE_NAME)
        liveDb.parentFile?.mkdirs()
        movePath(extracted.databaseFile, liveDb)

        val stagedDataStoreFile = extracted.datastoreFile
        if (stagedDataStoreFile != null && stagedDataStoreFile.exists()) {
            val liveDataStoreFile = File(context.filesDir, "$DATASTORE_DIR/$DATASTORE_FILE_NAME")
            movePath(stagedDataStoreFile, liveDataStoreFile)
        }

        val liveImagesDir = File(context.filesDir, APP_IMAGES_DIR)
        if (extracted.appImagesDir.exists()) {
            movePath(extracted.appImagesDir, liveImagesDir)
        }

        val liveNoteMediaDir = File(context.filesDir, NOTE_MEDIA_DIR)
        if (extracted.noteMediaDir.exists()) {
            movePath(extracted.noteMediaDir, liveNoteMediaDir)
        }
    }

    private fun rollbackFromBackup(backupPaths: Map<String, String>) {
        val backupDb = backupPaths[KEY_DB]?.let(::File)
        if (backupDb != null && backupDb.exists()) {
            val liveDb = context.getDatabasePath(AppDatabase.DATABASE_NAME)
            movePath(backupDb, liveDb)
        }

        val backupDataStore = backupPaths[KEY_DATASTORE]?.let(::File)
        if (backupDataStore != null && backupDataStore.exists()) {
            val liveDataStore = File(context.filesDir, DATASTORE_DIR)
            movePath(backupDataStore, liveDataStore)
        }

        val backupImages = backupPaths[KEY_APP_IMAGES]?.let(::File)
        if (backupImages != null && backupImages.exists()) {
            val liveImages = File(context.filesDir, APP_IMAGES_DIR)
            movePath(backupImages, liveImages)
        }

        val backupNoteMedia = backupPaths[KEY_NOTE_MEDIA]?.let(::File)
        if (backupNoteMedia != null && backupNoteMedia.exists()) {
            val liveNoteMedia = File(context.filesDir, NOTE_MEDIA_DIR)
            movePath(backupNoteMedia, liveNoteMedia)
        }
    }

    private fun cleanupAfterSuccessfulRestore() {
        clearBackupOldDirectory()
        clearStagingDirectory()
        restoreJournalFile().delete()
    }

    private fun clearBackupOldDirectory() {
        backupOldRoot().deleteRecursively()
    }

    private fun clearStagingDirectory() {
        restoreStagingRoot().deleteRecursively()
    }

    private fun movePath(source: File, target: File) {
        if (!source.exists()) return
        if (target.exists()) {
            target.deleteRecursively()
        }
        target.parentFile?.mkdirs()
        if (source.renameTo(target)) {
            return
        }

        if (source.isDirectory) {
            copyDirectory(source, target)
            source.deleteRecursively()
        } else {
            copyFile(source, target)
            source.delete()
        }
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

    private fun safeOutputFile(root: File, entryName: String): File {
        val outputFile = File(root, entryName)
        val rootPath = root.canonicalPath
        val outputPath = outputFile.canonicalPath
        if (!outputPath.startsWith(rootPath + File.separator)) {
            throw IllegalStateException("Unsafe zip entry path: $entryName")
        }
        return outputFile
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun writeRestoreJournal(journal: RestoreJournal) {
        restoreJournalFile().writeText(journal.toJson().toString())
    }

    private fun readRestoreJournal(): RestoreJournal? {
        val file = restoreJournalFile()
        if (!file.exists()) return null
        return runCatching {
            RestoreJournal.fromJson(JSONObject(file.readText()))
        }.getOrNull()
    }

    private fun restoreJournalFile(): File = File(context.filesDir, RESTORE_JOURNAL_FILE)

    private fun restoreStagingRoot(): File = File(context.filesDir, RESTORE_STAGING_DIR)

    private fun backupOldRoot(): File = File(context.filesDir, BACKUP_OLD_DIR)

    private fun discoverBackupPathsFromDisk(): Map<String, String> {
        val backupRoot = backupOldRoot()
        if (!backupRoot.exists()) return emptyMap()
        val discovered = mutableMapOf<String, String>()
        val backupDb = File(backupRoot, "db/${AppDatabase.DATABASE_NAME}")
        if (backupDb.exists()) discovered[KEY_DB] = backupDb.absolutePath
        val backupDataStore = File(backupRoot, DATASTORE_DIR)
        if (backupDataStore.exists()) discovered[KEY_DATASTORE] = backupDataStore.absolutePath
        val backupImages = File(backupRoot, APP_IMAGES_DIR)
        if (backupImages.exists()) discovered[KEY_APP_IMAGES] = backupImages.absolutePath
        val backupNoteMedia = File(backupRoot, NOTE_MEDIA_DIR)
        if (backupNoteMedia.exists()) discovered[KEY_NOTE_MEDIA] = backupNoteMedia.absolutePath
        return discovered
    }

    @Suppress("DEPRECATION")
    private fun appVersionName(): String {
        return runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown"
        }.getOrDefault("unknown")
    }

    private data class ExtractedBackup(
        val stagingRoot: File,
        val files: Map<String, File>,
        val manifest: JSONObject?,
        val databaseFile: File,
        val datastoreFile: File?,
        val appImagesDir: File,
        val noteMediaDir: File
    )

    private data class RestoreJournal(
        val phase: RestorePhase,
        val timestamp: Long,
        val stagedPaths: Map<String, String> = emptyMap(),
        val backupPaths: Map<String, String> = emptyMap()
    ) {
        fun toJson(): JSONObject {
            return JSONObject()
                .put("phase", phase.name)
                .put("timestamp", timestamp)
                .put("stagedPaths", JSONObject(stagedPaths))
                .put("backupPaths", JSONObject(backupPaths))
        }

        companion object {
            fun fromJson(json: JSONObject): RestoreJournal {
                val phaseName = json.optString("phase", RestorePhase.ROLLBACK_ATTEMPTED.name)
                val phase = runCatching { RestorePhase.valueOf(phaseName) }
                    .getOrDefault(RestorePhase.ROLLBACK_ATTEMPTED)
                return RestoreJournal(
                    phase = phase,
                    timestamp = json.optLong("timestamp", 0L),
                    stagedPaths = json.optJSONObject("stagedPaths").toMap(),
                    backupPaths = json.optJSONObject("backupPaths").toMap()
                )
            }

            private fun JSONObject?.toMap(): Map<String, String> {
                if (this == null) return emptyMap()
                val map = linkedMapOf<String, String>()
                val keys = keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    map[key] = optString(key)
                }
                return map
            }
        }
    }

    private enum class RestorePhase {
        EXTRACTING,
        SWAPPING,
        COMPLETED,
        ROLLBACK_ATTEMPTED
    }

    private companion object {
        const val TAG = "BackupSupportRepo"
        const val SCHEMA_VERSION = 3

        const val EXPORTS_DIR = "exports"
        const val PUBLIC_EXPORT_DIR = "attenote"

        const val DATASTORE_DIR = "datastore"
        const val DATASTORE_FILE_NAME = "attenote_preferences.preferences_pb"
        const val APP_IMAGES_DIR = "app_images"
        const val NOTE_MEDIA_DIR = "note_media"

        const val MANIFEST_FILE = "backup_manifest.json"
        const val DB_BACKUP_ENTRY = "attendance_tracker_db"
        const val DATASTORE_BACKUP_ENTRY = DATASTORE_FILE_NAME

        const val RESTORE_JOURNAL_FILE = "restore_journal.json"
        const val RESTORE_STAGING_DIR = "_restore_staging"
        const val BACKUP_OLD_DIR = "_backup_old"

        const val KEY_DB = "database"
        const val KEY_DATASTORE = "datastore"
        const val KEY_APP_IMAGES = "app_images"
        const val KEY_NOTE_MEDIA = "note_media"
        val REQUIRED_MANIFEST_ENTRIES = setOf(DB_BACKUP_ENTRY)
    }
}
