package com.uteacher.attenote.data.local.dao
import kotlin.jvm.JvmSuppressWildcards

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.uteacher.attenote.data.local.entity.NoteEntity
import com.uteacher.attenote.data.local.entity.NoteWithMedia
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface NoteDao {

    @Query("SELECT * FROM notes WHERE date = :date ORDER BY updatedAt DESC")
    fun observeNotesForDate(date: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun observeAllNotes(): Flow<List<NoteEntity>>

    @Transaction
    @Query("SELECT * FROM notes WHERE date = :date ORDER BY updatedAt DESC")
    fun observeNotesWithMediaForDate(date: String): Flow<List<NoteWithMedia>>

    @Query("SELECT * FROM notes WHERE noteId = :id")
    suspend fun getNoteById(id: Long): NoteEntity?

    @Transaction
    @Query("SELECT * FROM notes WHERE noteId = :id")
    suspend fun getNoteWithMedia(id: Long): NoteWithMedia?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity): Int

    @Query("DELETE FROM notes WHERE noteId = :noteId")
    suspend fun deleteNote(noteId: Long): Int
}
