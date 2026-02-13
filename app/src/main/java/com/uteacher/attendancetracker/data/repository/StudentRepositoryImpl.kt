package com.uteacher.attendancetracker.data.repository

import android.database.sqlite.SQLiteConstraintException
import androidx.room.withTransaction
import com.uteacher.attendancetracker.data.local.AppDatabase
import com.uteacher.attendancetracker.data.local.dao.ClassStudentCrossRefDao
import com.uteacher.attendancetracker.data.local.dao.StudentDao
import com.uteacher.attendancetracker.data.local.entity.ClassStudentCrossRef
import com.uteacher.attendancetracker.data.repository.internal.InputNormalizer
import com.uteacher.attendancetracker.data.repository.internal.RepositoryResult
import com.uteacher.attendancetracker.domain.mapper.toDomain
import com.uteacher.attendancetracker.domain.mapper.toEntity
import com.uteacher.attendancetracker.domain.model.Student
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class StudentRepositoryImpl(
    private val studentDao: StudentDao,
    private val crossRefDao: ClassStudentCrossRefDao,
    private val db: AppDatabase
) : StudentRepository {

    override fun observeAllStudents(): Flow<List<Student>> =
        studentDao.observeAllStudents().map { it.toDomain() }

    override fun observeActiveStudentsForClass(classId: Long): Flow<List<Student>> =
        studentDao.observeActiveStudentsForClass(classId).map { it.toDomain() }

    override fun observeStudentsForClass(classId: Long): Flow<List<StudentWithClassStatus>> =
        studentDao.observeStudentsForClass(classId).map { entities ->
            entities.map { entity ->
                StudentWithClassStatus(
                    student = entity.student.toDomain(),
                    isActiveInClass = entity.isActiveInClass
                )
            }
        }

    override suspend fun getStudentById(studentId: Long): Student? =
        studentDao.getStudentById(studentId)?.toDomain()

    override suspend fun findStudentByNameAndRegistration(
        name: String,
        registrationNumber: String
    ): Student? {
        val normalizedName = InputNormalizer.normalize(name)
        val normalizedReg = InputNormalizer.normalize(registrationNumber)
        return studentDao.getStudentByUniqueKey(normalizedName, normalizedReg)?.toDomain()
    }

    override suspend fun getStudentsForClass(classId: Long): List<StudentWithClassStatus> {
        return studentDao.observeStudentsForClass(classId).first().map { entity ->
            StudentWithClassStatus(
                student = entity.student.toDomain(),
                isActiveInClass = entity.isActiveInClass
            )
        }
    }

    override suspend fun createStudent(student: Student): RepositoryResult<Long> {
        val normalizedStudent = student.normalizeForSave()

        if (hasDuplicateStudent(normalizedStudent)) {
            return RepositoryResult.Error("Student already exists with same name and registration number")
        }

        return try {
            val studentId = studentDao.insertStudent(normalizedStudent.toEntity())
            RepositoryResult.Success(studentId)
        } catch (constraint: SQLiteConstraintException) {
            RepositoryResult.Error("Student already exists with same name and registration number")
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to create student: ${e.message}")
        }
    }

    override suspend fun updateStudent(student: Student): RepositoryResult<Unit> {
        val normalizedStudent = student.normalizeForSave()

        if (hasDuplicateStudent(normalizedStudent, ignoreStudentId = normalizedStudent.studentId)) {
            return RepositoryResult.Error("Another student exists with same name and registration number")
        }

        return try {
            val updatedRows = studentDao.updateStudent(normalizedStudent.toEntity())
            if (updatedRows == 0) {
                RepositoryResult.Error("Student not found")
            } else {
                RepositoryResult.Success(Unit)
            }
        } catch (constraint: SQLiteConstraintException) {
            RepositoryResult.Error("Another student exists with same name and registration number")
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to update student: ${e.message}")
        }
    }

    override suspend fun updateStudentActiveState(
        studentId: Long,
        isActive: Boolean
    ): RepositoryResult<Unit> {
        return try {
            val studentEntity = studentDao.getStudentById(studentId)
                ?: return RepositoryResult.Error("Student not found")
            studentDao.updateStudent(studentEntity.copy(isActive = isActive))
            RepositoryResult.Success(Unit)
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to update student active state: ${e.message}")
        }
    }

    override suspend fun deleteStudent(studentId: Long): RepositoryResult<Unit> {
        return try {
            val deletedRows = studentDao.deleteStudent(studentId)
            if (deletedRows == 0) {
                RepositoryResult.Error("Student not found")
            } else {
                RepositoryResult.Success(Unit)
            }
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to delete student: ${e.message}")
        }
    }

    override suspend fun addStudentToClass(classId: Long, studentId: Long): RepositoryResult<Unit> {
        return try {
            db.withTransaction {
                val student = studentDao.getStudentById(studentId)
                    ?: throw IllegalStateException("Student not found")

                val existingLink = crossRefDao.getLink(classId, student.studentId)
                when {
                    existingLink == null -> crossRefDao.insertLink(
                        ClassStudentCrossRef(classId = classId, studentId = studentId)
                    )

                    existingLink.isActiveInClass -> Unit

                    else -> crossRefDao.updateLink(existingLink.copy(isActiveInClass = true))
                }
            }
            RepositoryResult.Success(Unit)
        } catch (e: IllegalStateException) {
            RepositoryResult.Error(e.message ?: "Failed to add student to class")
        } catch (constraint: SQLiteConstraintException) {
            RepositoryResult.Error("Student already belongs to this class")
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to add student to class: ${e.message}")
        }
    }

    override suspend fun removeStudentFromClass(
        classId: Long,
        studentId: Long
    ): RepositoryResult<Unit> {
        return try {
            val deletedRows = crossRefDao.deleteLink(classId, studentId)
            if (deletedRows == 0) {
                RepositoryResult.Error("Student is not linked to this class")
            } else {
                RepositoryResult.Success(Unit)
            }
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to remove student from class: ${e.message}")
        }
    }

    override suspend fun updateStudentActiveInClass(
        classId: Long,
        studentId: Long,
        isActive: Boolean
    ): RepositoryResult<Unit> {
        return try {
            val link = crossRefDao.getLink(classId, studentId)
                ?: return RepositoryResult.Error("Student is not linked to this class")
            crossRefDao.updateLink(link.copy(isActiveInClass = isActive))
            RepositoryResult.Success(Unit)
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to update class roster state: ${e.message}")
        }
    }

    private suspend fun hasDuplicateStudent(
        student: Student,
        ignoreStudentId: Long? = null
    ): Boolean {
        val allStudents = studentDao.observeAllStudents().first()
        return allStudents.any { existing ->
            existing.studentId != ignoreStudentId &&
                InputNormalizer.areEqual(existing.name, student.name) &&
                InputNormalizer.areEqual(existing.registrationNumber, student.registrationNumber)
        }
    }

    private fun Student.normalizeForSave(): Student {
        return copy(
            name = InputNormalizer.normalize(name),
            registrationNumber = InputNormalizer.normalize(registrationNumber),
            rollNumber = rollNumber?.trim()?.ifBlank { null },
            email = email?.trim()?.ifBlank { null },
            phone = phone?.trim()?.ifBlank { null }
        )
    }
}
