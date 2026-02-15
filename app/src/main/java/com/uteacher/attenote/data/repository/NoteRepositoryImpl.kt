package com.uteacher.attenote.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.uteacher.attenote.data.local.AppDatabase
import com.uteacher.attenote.data.local.dao.NoteDao
import com.uteacher.attenote.data.local.dao.NoteMediaDao
import com.uteacher.attenote.data.repository.internal.InputNormalizer
import com.uteacher.attenote.data.repository.internal.LocalMediaFileCleaner
import com.uteacher.attenote.data.repository.internal.MediaCleanupResult
import com.uteacher.attenote.data.repository.internal.MediaCleanupStatus
import com.uteacher.attenote.data.repository.internal.PermanentMediaDeleteGateway
import com.uteacher.attenote.data.repository.internal.PermanentNoteDeleteGateway
import com.uteacher.attenote.data.repository.internal.RepositoryResult
import com.uteacher.attenote.data.repository.internal.performPermanentMediaDelete
import com.uteacher.attenote.data.repository.internal.performPermanentNoteDelete
import com.uteacher.attenote.domain.mapper.toDomain
import com.uteacher.attenote.domain.mapper.toEntity
import com.uteacher.attenote.domain.model.Note
import com.uteacher.attenote.domain.model.NoteMedia
import java.io.File
import java.net.URLConnection
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NoteRepositoryImpl(
    private val noteDao: NoteDao,
    private val mediaDao: NoteMediaDao,
    private val db: AppDatabase,
    filesDir: File
) : NoteRepository {
    private val mediaFileCleaner = LocalMediaFileCleaner(filesDir)

    override fun observeNotesForDate(date: LocalDate): Flow<List<Note>> =
        noteDao.observeNotesForDate(date.toString()).map { it.toDomain() }

    override fun observeAllNotes(): Flow<List<Note>> =
        noteDao.observeAllNotes().map { it.toDomain() }

    override suspend fun getNoteById(noteId: Long): Note? =
        noteDao.getNoteById(noteId)?.toDomain()

    override suspend fun getNoteWithMedia(noteId: Long): Pair<Note, List<NoteMedia>>? {
        val relation = noteDao.getNoteWithMedia(noteId) ?: return null
        return relation.note.toDomain() to relation.media.toDomain()
    }

    override suspend fun createNote(
        note: Note,
        mediaPaths: List<String>
    ): RepositoryResult<Long> {
        return try {
            val normalizedNote = note.copy(
                noteId = 0L,
                title = InputNormalizer.normalize(note.title),
                content = note.content.trim(),
                updatedAt = LocalDate.now()
            )
            val normalizedMediaPaths = normalizeMediaPaths(mediaPaths)

            val noteId = db.withTransaction {
                val insertedId = noteDao.insertNote(normalizedNote.toEntity())
                if (normalizedMediaPaths.isNotEmpty()) {
                    val mediaItems = normalizedMediaPaths.map { path ->
                        NoteMedia(
                            mediaId = 0L,
                            noteId = insertedId,
                            filePath = path,
                            mimeType = detectMimeType(path),
                            addedAt = LocalDate.now()
                        )
                    }
                    mediaDao.insertMediaList(mediaItems.toEntity())
                }
                insertedId
            }
            RepositoryResult.Success(noteId)
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to create note: ${e.message}")
        }
    }

    override suspend fun updateNote(note: Note): RepositoryResult<Unit> {
        return try {
            val updatedRows = noteDao.updateNote(
                note.copy(
                    title = InputNormalizer.normalize(note.title),
                    content = note.content.trim(),
                    updatedAt = LocalDate.now()
                ).toEntity()
            )
            if (updatedRows == 0) {
                RepositoryResult.Error("Note not found")
            } else {
                RepositoryResult.Success(Unit)
            }
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to update note: ${e.message}")
        }
    }

    override suspend fun addMediaToNote(
        noteId: Long,
        mediaPaths: List<String>
    ): RepositoryResult<Unit> {
        if (mediaPaths.isEmpty()) {
            return RepositoryResult.Success(Unit)
        }

        return try {
            val existingNote = noteDao.getNoteById(noteId)
                ?: return RepositoryResult.Error("Note not found")
            val normalizedMediaPaths = normalizeMediaPaths(mediaPaths)

            db.withTransaction {
                if (normalizedMediaPaths.isNotEmpty()) {
                    val mediaItems = normalizedMediaPaths.map { path ->
                        NoteMedia(
                            mediaId = 0L,
                            noteId = existingNote.noteId,
                            filePath = path,
                            mimeType = detectMimeType(path),
                            addedAt = LocalDate.now()
                        )
                    }
                    mediaDao.insertMediaList(mediaItems.toEntity())
                }
            }
            RepositoryResult.Success(Unit)
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to add media: ${e.message}")
        }
    }

    override suspend fun deleteNotePermanently(noteId: Long): RepositoryResult<Unit> {
        return try {
            val deleteResult = performPermanentNoteDelete(
                noteId = noteId,
                gateway = object : PermanentNoteDeleteGateway {
                    override suspend fun <T> runInTransaction(block: suspend () -> T): T {
                        return db.withTransaction { block() }
                    }

                    override suspend fun getNoteMediaPaths(noteId: Long): List<String> {
                        return noteDao.getNoteWithMedia(noteId)?.media?.map { it.filePath }.orEmpty()
                    }

                    override suspend fun countMediaReferences(filePath: String): Int {
                        return mediaDao.countMediaReferences(filePath)
                    }

                    override suspend fun deleteAllNoteMedia(noteId: Long) {
                        mediaDao.deleteAllMediaForNote(noteId)
                    }

                    override suspend fun deleteNote(noteId: Long): Int {
                        return noteDao.deleteNote(noteId)
                    }
                },
                cleaner = mediaFileCleaner
            )

            logCleanupWarnings(
                operation = "note delete",
                subject = "noteId=$noteId",
                statuses = deleteResult.mediaCleanupResults
            )
            RepositoryResult.Success(Unit)
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to delete note: ${e.message}")
        }
    }

    override suspend fun deleteNoteMediaPermanently(mediaId: Long): RepositoryResult<Unit> {
        return try {
            val deleteResult = performPermanentMediaDelete(
                mediaId = mediaId,
                gateway = object : PermanentMediaDeleteGateway {
                    override suspend fun <T> runInTransaction(block: suspend () -> T): T {
                        return db.withTransaction { block() }
                    }

                    override suspend fun getMediaPath(mediaId: Long): String? {
                        return mediaDao.getMediaById(mediaId)?.filePath
                    }

                    override suspend fun countMediaReferences(filePath: String): Int {
                        return mediaDao.countMediaReferences(filePath)
                    }

                    override suspend fun deleteMedia(mediaId: Long): Int {
                        return mediaDao.deleteMedia(mediaId)
                    }
                },
                cleaner = mediaFileCleaner
            )

            logCleanupWarnings(
                operation = "media delete",
                subject = "mediaId=$mediaId",
                statuses = deleteResult.mediaCleanupResults
            )
            RepositoryResult.Success(Unit)
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to delete media: ${e.message}")
        }
    }

    @Deprecated(
        message = "Use deleteNotePermanently for irreversible delete semantics",
        replaceWith = ReplaceWith("deleteNotePermanently(noteId)")
    )
    override suspend fun deleteNote(noteId: Long): RepositoryResult<Unit> {
        return deleteNotePermanently(noteId)
    }

    @Deprecated(
        message = "Use deleteNoteMediaPermanently for irreversible delete semantics",
        replaceWith = ReplaceWith("deleteNoteMediaPermanently(mediaId)")
    )
    override suspend fun deleteMedia(mediaId: Long): RepositoryResult<Unit> {
        return deleteNoteMediaPermanently(mediaId)
    }

    private fun normalizeMediaPaths(mediaPaths: List<String>): List<String> {
        return mediaPaths.mapNotNull { it.trim().ifBlank { null } }.distinct()
    }

    private fun detectMimeType(filePath: String): String {
        val guessed = URLConnection.guessContentTypeFromName(filePath)
        if (!guessed.isNullOrBlank()) return guessed

        return when (filePath.substringAfterLast('.', missingDelimiterValue = "").lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            "mp4" -> "video/mp4"
            "mkv" -> "video/x-matroska"
            "pdf" -> "application/pdf"
            else -> "application/octet-stream"
        }
    }

    private fun logCleanupWarnings(
        operation: String,
        subject: String,
        statuses: List<MediaCleanupResult>
    ) {
        statuses
            .filter { it.status == MediaCleanupStatus.FAILED }
            .forEach { failure ->
                Log.w(
                    TAG,
                    "Media cleanup warning during $operation ($subject, path=${failure.filePath}): ${failure.errorMessage}"
                )
            }
    }

    private companion object {
        const val TAG = "NoteRepository"
    }
}
