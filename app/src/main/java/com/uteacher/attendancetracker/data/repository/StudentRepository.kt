package com.uteacher.attendancetracker.data.repository

import com.uteacher.attendancetracker.data.repository.internal.RepositoryResult
import com.uteacher.attendancetracker.domain.model.Student
import kotlinx.coroutines.flow.Flow

interface StudentRepository {
    fun observeAllStudents(): Flow<List<Student>>
    fun observeActiveStudentsForClass(classId: Long): Flow<List<Student>>

    suspend fun getStudentById(studentId: Long): Student?

    suspend fun createStudent(student: Student): RepositoryResult<Long>
    suspend fun updateStudent(student: Student): RepositoryResult<Unit>
    suspend fun updateStudentActiveState(studentId: Long, isActive: Boolean): RepositoryResult<Unit>

    suspend fun deleteStudent(studentId: Long): RepositoryResult<Unit>

    suspend fun addStudentToClass(classId: Long, studentId: Long): RepositoryResult<Unit>
    suspend fun removeStudentFromClass(classId: Long, studentId: Long): RepositoryResult<Unit>
    suspend fun updateStudentActiveInClass(
        classId: Long,
        studentId: Long,
        isActive: Boolean
    ): RepositoryResult<Unit>
}
