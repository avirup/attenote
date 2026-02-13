package com.uteacher.attendancetracker.data.repository

import com.uteacher.attendancetracker.data.repository.internal.RepositoryResult
import com.uteacher.attendancetracker.domain.model.AttendanceRecord
import com.uteacher.attendancetracker.domain.model.AttendanceSession
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface AttendanceRepository {
    fun observeAllSessions(): Flow<List<AttendanceSession>>
    fun observeSessionsForClass(classId: Long): Flow<List<AttendanceSession>>

    suspend fun getSessionById(sessionId: Long): AttendanceSession?
    suspend fun getRecordsForSession(sessionId: Long): List<AttendanceRecord>
    suspend fun findSession(classId: Long, scheduleId: Long, date: LocalDate): AttendanceSession?

    suspend fun saveAttendance(
        classId: Long,
        scheduleId: Long,
        date: LocalDate,
        lessonNotes: String?,
        records: List<AttendanceRecord>
    ): RepositoryResult<Long>

    suspend fun countSessionsOutsideDateRange(
        classId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Int

    suspend fun deleteSession(sessionId: Long): RepositoryResult<Unit>
}
