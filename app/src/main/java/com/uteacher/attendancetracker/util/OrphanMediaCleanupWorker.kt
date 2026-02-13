package com.uteacher.attendancetracker.util

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.uteacher.attendancetracker.data.local.dao.NoteMediaDao
import java.io.File
import org.koin.core.context.GlobalContext

class OrphanMediaCleanupWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val noteMediaDao = GlobalContext.get().get<NoteMediaDao>()
            val noteMediaDir = File(applicationContext.filesDir, NOTE_MEDIA_DIR)
            if (!noteMediaDir.exists()) {
                return Result.success()
            }

            val referencedPaths = noteMediaDao.getAllFilePaths()
                .map { path ->
                    val file = File(path)
                    if (file.isAbsolute) file.absolutePath else File(noteMediaDir, path).absolutePath
                }
                .toSet()

            val orphanedFiles = noteMediaDir.walkTopDown()
                .filter { it.isFile }
                .filterNot { it.absolutePath in referencedPaths }
                .toList()

            orphanedFiles.forEach { file ->
                runCatching {
                    // Re-check references to avoid deleting media added during this worker run.
                    val latestReferences = noteMediaDao.getAllFilePaths().map { path ->
                        val refFile = File(path)
                        if (refFile.isAbsolute) {
                            refFile.absolutePath
                        } else {
                            File(noteMediaDir, path).absolutePath
                        }
                    }.toSet()
                    if (file.absolutePath in latestReferences) {
                        Log.d(TAG, "Skipping newly referenced file: ${file.name}")
                        return@runCatching
                    }

                    if (file.delete()) {
                        Log.d(TAG, "Deleted orphaned file: ${file.name}")
                    } else {
                        Log.w(TAG, "Unable to delete orphaned file: ${file.name}")
                    }
                }.onFailure { throwable ->
                    Log.w(TAG, "Failed to delete ${file.name}: ${throwable.message}")
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Worker failed: ${e.message}", e)
            Result.failure()
        }
    }

    private companion object {
        const val NOTE_MEDIA_DIR = "note_media"
        const val TAG = "MediaCleanup"
    }
}
