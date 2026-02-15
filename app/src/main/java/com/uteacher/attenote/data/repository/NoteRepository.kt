package com.uteacher.attenote.data.repository

import com.uteacher.attenote.data.repository.internal.RepositoryResult
import com.uteacher.attenote.domain.model.Note
import com.uteacher.attenote.domain.model.NoteMedia
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun observeNotesForDate(date: LocalDate): Flow<List<Note>>
    fun observeAllNotes(): Flow<List<Note>>

    suspend fun getNoteById(noteId: Long): Note?
    suspend fun getNoteWithMedia(noteId: Long): Pair<Note, List<NoteMedia>>?

    suspend fun createNote(note: Note, mediaPaths: List<String> = emptyList()): RepositoryResult<Long>

    suspend fun updateNote(note: Note): RepositoryResult<Unit>
    suspend fun addMediaToNote(noteId: Long, mediaPaths: List<String>): RepositoryResult<Unit>

    suspend fun deleteNotePermanently(noteId: Long): RepositoryResult<Unit>
    suspend fun deleteNoteMediaPermanently(mediaId: Long): RepositoryResult<Unit>

    @Deprecated(
        message = "Use deleteNotePermanently for irreversible delete semantics",
        replaceWith = ReplaceWith("deleteNotePermanently(noteId)")
    )
    suspend fun deleteNote(noteId: Long): RepositoryResult<Unit> =
        deleteNotePermanently(noteId)

    @Deprecated(
        message = "Use deleteNoteMediaPermanently for irreversible delete semantics",
        replaceWith = ReplaceWith("deleteNoteMediaPermanently(mediaId)")
    )
    suspend fun deleteMedia(mediaId: Long): RepositoryResult<Unit> =
        deleteNoteMediaPermanently(mediaId)
}
