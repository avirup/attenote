package com.uteacher.attendancetracker.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.uteacher.attendancetracker.data.local.AppDatabase
import com.uteacher.attendancetracker.data.local.dao.NoteDao
import com.uteacher.attendancetracker.data.local.dao.NoteMediaDao
import com.uteacher.attendancetracker.data.repository.internal.InputNormalizer
import com.uteacher.attendancetracker.data.repository.internal.RepositoryResult
import com.uteacher.attendancetracker.domain.mapper.toDomain
import com.uteacher.attendancetracker.domain.mapper.toEntity
import com.uteacher.attendancetracker.domain.model.Note
import com.uteacher.attendancetracker.domain.model.NoteMedia
import java.io.File
import java.net.URLConnection
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NoteRepositoryImpl(
    private val noteDao: NoteDao,
    private val mediaDao: NoteMediaDao,
    private val db: AppDatabase,
    private val context: Context
) : NoteRepository {

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

    override suspend fun deleteNote(noteId: Long): RepositoryResult<Unit> {
        val noteWithMedia = noteDao.getNoteWithMedia(noteId)
            ?: return RepositoryResult.Error("Note not found")

        return try {
            val deletedRows = noteDao.deleteNote(noteId)
            if (deletedRows == 0) {
                return RepositoryResult.Error("Note not found")
            }

            noteWithMedia.media.forEach { media ->
                cleanupMediaFileIfUnreferenced(media.filePath)
            }
            RepositoryResult.Success(Unit)
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to delete note: ${e.message}")
        }
    }

    override suspend fun deleteMedia(mediaId: Long): RepositoryResult<Unit> {
        val media = mediaDao.getMediaById(mediaId)
            ?: return RepositoryResult.Error("Media not found")

        return try {
            val deletedRows = mediaDao.deleteMedia(mediaId)
            if (deletedRows == 0) {
                return RepositoryResult.Error("Media not found")
            }
            cleanupMediaFileIfUnreferenced(media.filePath)
            RepositoryResult.Success(Unit)
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to delete media: ${e.message}")
        }
    }

    private fun normalizeMediaPaths(mediaPaths: List<String>): List<String> {
        return mediaPaths.mapNotNull { it.trim().ifBlank { null } }.distinct()
    }

    private suspend fun cleanupMediaFileIfUnreferenced(filePath: String) {
        runCatching {
            if (mediaDao.countMediaReferences(filePath) > 0) return

            val file = if (File(filePath).isAbsolute) {
                File(filePath)
            } else {
                File(context.filesDir, filePath)
            }
            if (file.exists()) {
                file.delete()
            }
        }
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
}
