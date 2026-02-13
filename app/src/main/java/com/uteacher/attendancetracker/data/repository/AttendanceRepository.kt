package com.uteacher.attendancetracker.data.repository

import com.uteacher.attendancetracker.data.repository.internal.RepositoryResult
import com.uteacher.attendancetracker.domain.model.AttendanceRecord
import com.uteacher.attendancetracker.domain.model.AttendanceSession
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface AttendanceRepository {
    fun observeSessionsForClass(classId: Long): Flow<List<AttendanceSession>>

    suspend fun getSessionById(sessionId: Long): AttendanceSession?
    suspend fun findSession(classId: Long, scheduleId: Long, date: LocalDate): AttendanceSession?

    suspend fun saveAttendance(
        classId: Long,
        scheduleId: Long,
        date: LocalDate,
        lessonNotes: String?,
        records: List<AttendanceRecord>
    ): RepositoryResult<Long>

    suspend fun deleteSession(sessionId: Long): RepositoryResult<Unit>
}
