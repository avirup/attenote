package com.uteacher.attendancetracker.data.repository

import android.database.sqlite.SQLiteConstraintException
import androidx.room.withTransaction
import com.uteacher.attendancetracker.data.local.AppDatabase
import com.uteacher.attendancetracker.data.local.dao.ClassDao
import com.uteacher.attendancetracker.data.local.dao.ScheduleDao
import com.uteacher.attendancetracker.data.repository.internal.InputNormalizer
import com.uteacher.attendancetracker.data.repository.internal.RepositoryResult
import com.uteacher.attendancetracker.data.repository.internal.ScheduleValidation
import com.uteacher.attendancetracker.domain.mapper.toDomain
import com.uteacher.attendancetracker.domain.mapper.toEntity
import com.uteacher.attendancetracker.domain.model.Class as DomainClass
import com.uteacher.attendancetracker.domain.model.Schedule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class ClassRepositoryImpl(
    private val classDao: ClassDao,
    private val scheduleDao: ScheduleDao,
    private val db: AppDatabase
) : ClassRepository {

    override fun observeClasses(isOpen: Boolean): Flow<List<DomainClass>> =
        classDao.observeClassesByOpenState(isOpen).map { it.toDomain() }

    override fun observeAllClasses(): Flow<List<DomainClass>> =
        classDao.observeAllClasses().map { it.toDomain() }

    override suspend fun getClassById(classId: Long): DomainClass? =
        classDao.getClassById(classId)?.toDomain()

    override suspend fun getClassWithSchedules(classId: Long): Pair<DomainClass, List<Schedule>>? {
        val relation = classDao.getClassWithSchedules(classId) ?: return null
        return relation.classEntity.toDomain() to relation.schedules.toDomain()
    }

    override suspend fun createClass(
        class_: DomainClass,
        schedules: List<Schedule>
    ): RepositoryResult<Long> {
        val normalizedClass = class_.normalizeIdentity()

        if (hasDuplicateIdentity(normalizedClass)) {
            return RepositoryResult.Error("Class already exists with this identity")
        }

        validateSchedules(schedules)?.let { return RepositoryResult.Error(it) }

        return try {
            val classId = db.withTransaction {
                val insertedId = classDao.insertClass(normalizedClass.toEntity())
                if (schedules.isNotEmpty()) {
                    scheduleDao.insertSchedules(
                        schedules.map { it.copy(classId = insertedId) }.toEntity()
                    )
                }
                insertedId
            }
            RepositoryResult.Success(classId)
        } catch (constraint: SQLiteConstraintException) {
            RepositoryResult.Error("Class already exists with this identity")
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to create class: ${e.message}")
        }
    }

    override suspend fun updateClass(class_: DomainClass): RepositoryResult<Unit> {
        val normalizedClass = class_.normalizeIdentity()

        if (hasDuplicateIdentity(normalizedClass, ignoreClassId = normalizedClass.classId)) {
            return RepositoryResult.Error("Another class exists with this identity")
        }

        return try {
            val updatedRows = classDao.updateClass(normalizedClass.toEntity())
            if (updatedRows == 0) {
                RepositoryResult.Error("Class not found")
            } else {
                RepositoryResult.Success(Unit)
            }
        } catch (constraint: SQLiteConstraintException) {
            RepositoryResult.Error("Another class exists with this identity")
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to update class: ${e.message}")
        }
    }

    override suspend fun updateClassOpenState(classId: Long, isOpen: Boolean): RepositoryResult<Unit> {
        return try {
            val classEntity = classDao.getClassById(classId)
                ?: return RepositoryResult.Error("Class not found")

            classDao.updateClass(classEntity.copy(isOpen = isOpen))
            RepositoryResult.Success(Unit)
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to update class state: ${e.message}")
        }
    }

    override suspend fun deleteClass(classId: Long): RepositoryResult<Unit> {
        return try {
            val deletedRows = classDao.deleteClass(classId)
            if (deletedRows == 0) {
                RepositoryResult.Error("Class not found")
            } else {
                RepositoryResult.Success(Unit)
            }
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to delete class: ${e.message}")
        }
    }

    private suspend fun hasDuplicateIdentity(
        class_: DomainClass,
        ignoreClassId: Long? = null
    ): Boolean {
        val allClasses = classDao.observeAllClasses().first()
        return allClasses.any { existing ->
            existing.classId != ignoreClassId &&
                InputNormalizer.areEqual(existing.instituteName, class_.instituteName) &&
                InputNormalizer.areEqual(existing.session, class_.session) &&
                InputNormalizer.areEqual(existing.department, class_.department) &&
                InputNormalizer.areEqual(existing.semester, class_.semester) &&
                InputNormalizer.areEqual(existing.subject, class_.subject) &&
                InputNormalizer.areEqual(existing.section, class_.section)
        }
    }

    private fun DomainClass.normalizeIdentity(): DomainClass {
        return copy(
            instituteName = InputNormalizer.normalize(instituteName),
            session = InputNormalizer.normalize(session),
            department = InputNormalizer.normalize(department),
            semester = InputNormalizer.normalize(semester),
            subject = InputNormalizer.normalize(subject),
            section = InputNormalizer.normalize(section)
        )
    }

    private fun validateSchedules(schedules: List<Schedule>): String? {
        schedules.forEach { schedule ->
            ScheduleValidation.validateTimeOrder(schedule.startTime, schedule.endTime)?.let {
                return it
            }
        }

        for (i in schedules.indices) {
            for (j in i + 1 until schedules.size) {
                val first = schedules[i]
                val second = schedules[j]
                if (first.dayOfWeek != second.dayOfWeek) continue
                if (first.startTime < second.endTime && second.startTime < first.endTime) {
                    return "Schedule overlaps with existing schedule on ${first.dayOfWeek}"
                }
            }
        }
        return null
    }
}
