package com.uteacher.attendancetracker.data.local.dao
import kotlin.jvm.JvmSuppressWildcards

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.uteacher.attendancetracker.data.local.entity.NoteMediaEntity
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface NoteMediaDao {

    @Query("SELECT * FROM note_media WHERE noteId = :noteId ORDER BY addedAt ASC")
    fun observeMediaForNote(noteId: Long): Flow<List<NoteMediaEntity>>

    @Query("SELECT * FROM note_media WHERE mediaId = :id")
    suspend fun getMediaById(id: Long): NoteMediaEntity?

    @Query("SELECT COUNT(*) FROM note_media WHERE filePath = :filePath")
    suspend fun countMediaReferences(filePath: String): Int

    @Query("SELECT filePath FROM note_media")
    suspend fun getAllFilePaths(): List<String>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMedia(media: NoteMediaEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMediaList(mediaList: List<NoteMediaEntity>): List<Long>

    @Update
    suspend fun updateMedia(media: NoteMediaEntity): Int

    @Query("DELETE FROM note_media WHERE mediaId = :mediaId")
    suspend fun deleteMedia(mediaId: Long): Int

    @Query("DELETE FROM note_media WHERE noteId = :noteId")
    suspend fun deleteAllMediaForNote(noteId: Long): Int
}
