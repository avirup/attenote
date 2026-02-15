package com.uteacher.attenote.data.repository

import com.uteacher.attenote.data.repository.internal.RepositoryResult
import com.uteacher.attenote.domain.model.AttendanceRecord
import com.uteacher.attenote.domain.model.AttendanceStatus
import com.uteacher.attenote.domain.model.AttendanceSession
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

data class AttendanceStatusInput(
    val studentId: Long,
    val status: AttendanceStatus
)

data class AttendanceCounters(
    val present: Int,
    val absent: Int,
    val skipped: Int,
    val total: Int
)

interface AttendanceRepository {
    fun observeAllSessions(): Flow<List<AttendanceSession>>
    fun observeSessionsForClass(classId: Long): Flow<List<AttendanceSession>>

    suspend fun getSessionById(sessionId: Long): AttendanceSession?
    suspend fun getRecordsForSession(sessionId: Long): List<AttendanceRecord>
    suspend fun findSession(classId: Long, scheduleId: Long, date: LocalDate): AttendanceSession?
    suspend fun getAttendanceCounters(sessionId: Long): AttendanceCounters

    suspend fun saveAttendance(
        classId: Long,
        scheduleId: Long,
        date: LocalDate,
        isClassTaken: Boolean,
        lessonNotes: String?,
        records: List<AttendanceStatusInput>
    ): RepositoryResult<Long>

    suspend fun countSessionsOutsideDateRange(
        classId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Int

    suspend fun deleteSession(sessionId: Long): RepositoryResult<Unit>
}
