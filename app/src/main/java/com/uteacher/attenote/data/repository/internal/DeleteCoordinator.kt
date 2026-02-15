package com.uteacher.attenote.data.repository.internal

import java.io.File

internal enum class MediaCleanupStatus {
    DELETED,
    MISSING,
    FAILED
}

internal data class MediaCleanupResult(
    val filePath: String,
    val status: MediaCleanupStatus,
    val errorMessage: String? = null
)

internal interface MediaFileCleaner {
    fun delete(filePath: String): MediaCleanupResult
}

internal class LocalMediaFileCleaner(
    private val filesDir: File
) : MediaFileCleaner {
    override fun delete(filePath: String): MediaCleanupResult {
        val normalizedPath = filePath.trim()
        if (normalizedPath.isEmpty()) {
            return MediaCleanupResult(
                filePath = filePath,
                status = MediaCleanupStatus.MISSING
            )
        }

        val explicitFile = File(normalizedPath)
        val resolvedFile = if (explicitFile.isAbsolute) {
            explicitFile
        } else {
            File(filesDir, normalizedPath)
        }

        if (!resolvedFile.exists()) {
            return MediaCleanupResult(
                filePath = normalizedPath,
                status = MediaCleanupStatus.MISSING
            )
        }

        return runCatching {
            if (resolvedFile.delete() || !resolvedFile.exists()) {
                MediaCleanupResult(
                    filePath = normalizedPath,
                    status = MediaCleanupStatus.DELETED
                )
            } else {
                MediaCleanupResult(
                    filePath = normalizedPath,
                    status = MediaCleanupStatus.FAILED,
                    errorMessage = "Failed to delete media file"
                )
            }
        }.getOrElse { error ->
            MediaCleanupResult(
                filePath = normalizedPath,
                status = MediaCleanupStatus.FAILED,
                errorMessage = error.message ?: error::class.java.simpleName
            )
        }
    }
}

internal data class ClassCascadeDeleteResult(
    val classDeleted: Boolean,
    val mediaCleanupResults: List<MediaCleanupResult>
)

internal data class StudentCascadeDeleteResult(
    val studentDeleted: Boolean
)

internal data class PermanentNoteDeleteResult(
    val noteDeleted: Boolean,
    val mediaCleanupResults: List<MediaCleanupResult>
)

internal data class PermanentMediaDeleteResult(
    val mediaDeleted: Boolean,
    val mediaCleanupResults: List<MediaCleanupResult>
)

internal interface ClassCascadeDeleteGateway {
    suspend fun <T> runInTransaction(block: suspend () -> T): T
    suspend fun getNoteIdsForClass(classId: Long): List<Long>
    suspend fun getMediaPathsForNoteIds(noteIds: List<Long>): List<String>
    suspend fun countMediaReferences(filePath: String): Int
    suspend fun deleteAttendanceRecordsForClass(classId: Long)
    suspend fun deleteAttendanceSessionsForClass(classId: Long)
    suspend fun deleteSchedulesForClass(classId: Long)
    suspend fun deleteNoteMediaForNoteIds(noteIds: List<Long>)
    suspend fun deleteNotesForClass(classId: Long)
    suspend fun deleteClass(classId: Long): Int
}

internal interface StudentCascadeDeleteGateway {
    suspend fun <T> runInTransaction(block: suspend () -> T): T
    suspend fun deleteAttendanceRecordsForStudent(studentId: Long)
    suspend fun deleteStudent(studentId: Long): Int
}

internal interface PermanentNoteDeleteGateway {
    suspend fun <T> runInTransaction(block: suspend () -> T): T
    suspend fun getNoteMediaPaths(noteId: Long): List<String>
    suspend fun countMediaReferences(filePath: String): Int
    suspend fun deleteAllNoteMedia(noteId: Long)
    suspend fun deleteNote(noteId: Long): Int
}

internal interface PermanentMediaDeleteGateway {
    suspend fun <T> runInTransaction(block: suspend () -> T): T
    suspend fun getMediaPath(mediaId: Long): String?
    suspend fun countMediaReferences(filePath: String): Int
    suspend fun deleteMedia(mediaId: Long): Int
}

internal suspend fun performClassCascadeDelete(
    classId: Long,
    gateway: ClassCascadeDeleteGateway,
    cleaner: MediaFileCleaner
): ClassCascadeDeleteResult {
    var deletedRows = 0
    var mediaPathsToDelete: Set<String> = emptySet()

    gateway.runInTransaction {
        val noteIds = gateway.getNoteIdsForClass(classId)
        mediaPathsToDelete = if (noteIds.isEmpty()) {
            emptySet()
        } else {
            gateway.getMediaPathsForNoteIds(noteIds).toSet()
        }

        gateway.deleteAttendanceRecordsForClass(classId)
        gateway.deleteAttendanceSessionsForClass(classId)
        gateway.deleteSchedulesForClass(classId)

        if (noteIds.isNotEmpty()) {
            gateway.deleteNoteMediaForNoteIds(noteIds)
        }
        gateway.deleteNotesForClass(classId)

        deletedRows = gateway.deleteClass(classId)
    }

    val unreferencedMediaPaths = filterUnreferencedPaths(
        mediaPaths = mediaPathsToDelete,
        countReferences = gateway::countMediaReferences
    )

    return ClassCascadeDeleteResult(
        classDeleted = deletedRows > 0,
        mediaCleanupResults = cleanupMediaFiles(unreferencedMediaPaths, cleaner)
    )
}

internal suspend fun performStudentCascadeDelete(
    studentId: Long,
    gateway: StudentCascadeDeleteGateway
): StudentCascadeDeleteResult {
    var deletedRows = 0

    gateway.runInTransaction {
        gateway.deleteAttendanceRecordsForStudent(studentId)
        deletedRows = gateway.deleteStudent(studentId)
    }

    return StudentCascadeDeleteResult(studentDeleted = deletedRows > 0)
}

internal suspend fun performPermanentNoteDelete(
    noteId: Long,
    gateway: PermanentNoteDeleteGateway,
    cleaner: MediaFileCleaner
): PermanentNoteDeleteResult {
    var deletedRows = 0
    var mediaPathsToDelete: Set<String> = emptySet()

    gateway.runInTransaction {
        mediaPathsToDelete = gateway.getNoteMediaPaths(noteId).toSet()
        gateway.deleteAllNoteMedia(noteId)
        deletedRows = gateway.deleteNote(noteId)
    }

    val unreferencedMediaPaths = filterUnreferencedPaths(
        mediaPaths = mediaPathsToDelete,
        countReferences = gateway::countMediaReferences
    )

    return PermanentNoteDeleteResult(
        noteDeleted = deletedRows > 0,
        mediaCleanupResults = cleanupMediaFiles(unreferencedMediaPaths, cleaner)
    )
}

internal suspend fun performPermanentMediaDelete(
    mediaId: Long,
    gateway: PermanentMediaDeleteGateway,
    cleaner: MediaFileCleaner
): PermanentMediaDeleteResult {
    var deletedRows = 0
    var mediaPath: String? = null

    gateway.runInTransaction {
        mediaPath = gateway.getMediaPath(mediaId)
        if (mediaPath != null) {
            deletedRows = gateway.deleteMedia(mediaId)
        }
    }

    val unreferencedMediaPaths = mediaPath?.let { path ->
        if (gateway.countMediaReferences(path) > 0) {
            emptySet()
        } else {
            setOf(path)
        }
    }.orEmpty()

    val cleanupResults = cleanupMediaFiles(unreferencedMediaPaths, cleaner)

    return PermanentMediaDeleteResult(
        mediaDeleted = deletedRows > 0,
        mediaCleanupResults = cleanupResults
    )
}

internal fun cleanupMediaFiles(
    filePaths: Collection<String>,
    cleaner: MediaFileCleaner
): List<MediaCleanupResult> {
    return filePaths
        .asSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .distinct()
        .map { path -> cleaner.delete(path) }
        .toList()
}

private suspend fun filterUnreferencedPaths(
    mediaPaths: Set<String>,
    countReferences: suspend (String) -> Int
): Set<String> {
    if (mediaPaths.isEmpty()) return emptySet()

    val removablePaths = linkedSetOf<String>()
    mediaPaths.forEach { path ->
        if (countReferences(path) <= 0) {
            removablePaths += path
        }
    }
    return removablePaths
}
