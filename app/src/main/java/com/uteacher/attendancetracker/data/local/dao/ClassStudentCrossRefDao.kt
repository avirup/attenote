package com.uteacher.attendancetracker.data.local.dao
import kotlin.jvm.JvmSuppressWildcards

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.uteacher.attendancetracker.data.local.entity.ClassStudentCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface ClassStudentCrossRefDao {

    @Query(
        """
        SELECT * FROM class_student_cross_ref
        WHERE classId = :classId
        ORDER BY addedAt DESC
        """
    )
    fun observeLinksForClass(classId: Long): Flow<List<ClassStudentCrossRef>>

    @Query(
        """
        SELECT * FROM class_student_cross_ref
        WHERE classId = :classId AND studentId = :studentId
        """
    )
    suspend fun getLink(classId: Long, studentId: Long): ClassStudentCrossRef?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertLink(link: ClassStudentCrossRef): Long

    @Update
    suspend fun updateLink(link: ClassStudentCrossRef): Int

    @Query(
        """
        DELETE FROM class_student_cross_ref
        WHERE classId = :classId AND studentId = :studentId
        """
    )
    suspend fun deleteLink(classId: Long, studentId: Long): Int

    @Query("DELETE FROM class_student_cross_ref WHERE classId = :classId")
    suspend fun deleteAllLinksForClass(classId: Long): Int
}
