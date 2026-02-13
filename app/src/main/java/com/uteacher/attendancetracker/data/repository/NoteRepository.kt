package com.uteacher.attendancetracker.data.repository

import com.uteacher.attendancetracker.data.repository.internal.RepositoryResult
import com.uteacher.attendancetracker.domain.model.Note
import com.uteacher.attendancetracker.domain.model.NoteMedia
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

    suspend fun deleteNote(noteId: Long): RepositoryResult<Unit>
    suspend fun deleteMedia(mediaId: Long): RepositoryResult<Unit>
}
