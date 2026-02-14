package com.uteacher.attenote.data.local.dao
import kotlin.jvm.JvmSuppressWildcards

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.uteacher.attenote.data.local.entity.ClassEntity
import com.uteacher.attenote.data.local.entity.ClassWithSchedules
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface ClassDao {

    @Query("SELECT * FROM classes WHERE isOpen = :isOpen ORDER BY createdAt DESC")
    fun observeClassesByOpenState(isOpen: Boolean): Flow<List<ClassEntity>>

    @Query("SELECT * FROM classes ORDER BY createdAt DESC")
    fun observeAllClasses(): Flow<List<ClassEntity>>

    @Query("SELECT * FROM classes WHERE classId = :id")
    suspend fun getClassById(id: Long): ClassEntity?

    @Transaction
    @Query("SELECT * FROM classes WHERE classId = :id")
    suspend fun getClassWithSchedules(id: Long): ClassWithSchedules?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertClass(classEntity: ClassEntity): Long

    @Update
    suspend fun updateClass(classEntity: ClassEntity): Int

    @Query("DELETE FROM classes WHERE classId = :classId")
    suspend fun deleteClass(classId: Long): Int
}
