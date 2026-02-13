package com.uteacher.attendancetracker.data.repository

import com.uteacher.attendancetracker.data.repository.internal.RepositoryResult
import com.uteacher.attendancetracker.domain.model.Class as DomainClass
import com.uteacher.attendancetracker.domain.model.Schedule
import kotlinx.coroutines.flow.Flow

interface ClassRepository {
    fun observeClasses(isOpen: Boolean): Flow<List<DomainClass>>
    fun observeAllClasses(): Flow<List<DomainClass>>

    suspend fun getClassById(classId: Long): DomainClass?
    suspend fun getClassWithSchedules(classId: Long): Pair<DomainClass, List<Schedule>>?

    suspend fun createClass(class_: DomainClass, schedules: List<Schedule>): RepositoryResult<Long>

    suspend fun updateClass(class_: DomainClass): RepositoryResult<Unit>
    suspend fun updateClassOpenState(classId: Long, isOpen: Boolean): RepositoryResult<Unit>

    suspend fun deleteClass(classId: Long): RepositoryResult<Unit>
}
