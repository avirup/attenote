package com.uteacher.attendancetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.uteacher.attendancetracker.data.local.entity.StudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {

    @Query("SELECT * FROM students ORDER BY name ASC")
    fun observeAllStudents(): Flow<List<StudentEntity>>

    @Query(
        """
        SELECT s.* FROM students s
        INNER JOIN class_student_cross_ref cs ON s.studentId = cs.studentId
        WHERE cs.classId = :classId
        AND s.isActive = 1
        AND cs.isActiveInClass = 1
        ORDER BY s.name ASC
        """
    )
    fun observeActiveStudentsForClass(classId: Long): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE studentId = :id")
    suspend fun getStudentById(id: Long): StudentEntity?

    @Query("SELECT * FROM students WHERE name = :name AND registrationNumber = :regNumber")
    suspend fun getStudentByUniqueKey(name: String, regNumber: String): StudentEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertStudent(student: StudentEntity): Long

    @Update
    suspend fun updateStudent(student: StudentEntity): Int

    @Query("DELETE FROM students WHERE studentId = :studentId")
    suspend fun deleteStudent(studentId: Long): Int
}
