package com.uteacher.attenote.data.repository

import com.uteacher.attenote.data.repository.internal.RepositoryResult
import com.uteacher.attenote.domain.model.Student
import kotlinx.coroutines.flow.Flow

data class StudentWithClassStatus(
    val student: Student,
    val isActiveInClass: Boolean
)

interface StudentRepository {
    fun observeAllStudents(): Flow<List<Student>>
    fun observeActiveStudentsForClass(classId: Long): Flow<List<Student>>
    fun observeStudentsForClass(classId: Long): Flow<List<StudentWithClassStatus>>

    suspend fun getStudentById(studentId: Long): Student?
    suspend fun findStudentByNameAndRegistration(name: String, registrationNumber: String): Student?
    suspend fun getStudentsForClass(classId: Long): List<StudentWithClassStatus>

    suspend fun createStudent(student: Student): RepositoryResult<Long>
    suspend fun updateStudent(student: Student): RepositoryResult<Unit>
    suspend fun updateStudentActiveState(studentId: Long, isActive: Boolean): RepositoryResult<Unit>
    suspend fun mergeStudentIntoExisting(sourceStudentId: Long, targetStudentId: Long): RepositoryResult<Unit>

    suspend fun deleteStudentPermanently(studentId: Long): RepositoryResult<Unit>

    @Deprecated(
        message = "Use deleteStudentPermanently for irreversible cascade delete semantics",
        replaceWith = ReplaceWith("deleteStudentPermanently(studentId)")
    )
    suspend fun deleteStudent(studentId: Long): RepositoryResult<Unit> =
        deleteStudentPermanently(studentId)

    suspend fun addStudentToClass(classId: Long, studentId: Long): RepositoryResult<Unit>
    suspend fun removeStudentFromClass(classId: Long, studentId: Long): RepositoryResult<Unit>
    suspend fun updateStudentActiveInClass(
        classId: Long,
        studentId: Long,
        isActive: Boolean
    ): RepositoryResult<Unit>
}
