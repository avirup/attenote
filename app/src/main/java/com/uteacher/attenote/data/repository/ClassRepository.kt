package com.uteacher.attenote.data.repository

import com.uteacher.attenote.data.repository.internal.RepositoryResult
import com.uteacher.attenote.domain.model.Class as DomainClass
import com.uteacher.attenote.domain.model.Schedule
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface ClassRepository {
    fun observeClasses(isOpen: Boolean): Flow<List<DomainClass>>
    fun observeAllClasses(): Flow<List<DomainClass>>

    suspend fun getClassById(classId: Long): DomainClass?
    suspend fun getClassWithSchedules(classId: Long): Pair<DomainClass, List<Schedule>>?

    suspend fun createClass(class_: DomainClass, schedules: List<Schedule>): RepositoryResult<Long>

    suspend fun updateClass(class_: DomainClass): RepositoryResult<Unit>
    suspend fun updateClassOpenState(classId: Long, isOpen: Boolean): RepositoryResult<Unit>
    suspend fun updateClassDateRange(
        classId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): RepositoryResult<Unit>

    suspend fun deleteClassPermanently(classId: Long): RepositoryResult<Unit>

    @Deprecated(
        message = "Use deleteClassPermanently for irreversible cascade delete semantics",
        replaceWith = ReplaceWith("deleteClassPermanently(classId)")
    )
    suspend fun deleteClass(classId: Long): RepositoryResult<Unit> =
        deleteClassPermanently(classId)
}
