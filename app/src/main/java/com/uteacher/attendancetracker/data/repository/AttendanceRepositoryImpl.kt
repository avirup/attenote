package com.uteacher.attendancetracker.data.repository

import androidx.room.withTransaction
import com.uteacher.attendancetracker.data.local.AppDatabase
import com.uteacher.attendancetracker.data.local.dao.AttendanceRecordDao
import com.uteacher.attendancetracker.data.local.dao.AttendanceSessionDao
import com.uteacher.attendancetracker.data.repository.internal.InputNormalizer
import com.uteacher.attendancetracker.data.repository.internal.RepositoryResult
import com.uteacher.attendancetracker.domain.mapper.toDomain
import com.uteacher.attendancetracker.domain.mapper.toEntity
import com.uteacher.attendancetracker.domain.model.AttendanceRecord
import com.uteacher.attendancetracker.domain.model.AttendanceSession
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AttendanceRepositoryImpl(
    private val sessionDao: AttendanceSessionDao,
    private val recordDao: AttendanceRecordDao,
    private val db: AppDatabase
) : AttendanceRepository {

    override fun observeAllSessions(): Flow<List<AttendanceSession>> =
        sessionDao.observeAllSessions().map { it.toDomain() }

    override fun observeSessionsForClass(classId: Long): Flow<List<AttendanceSession>> =
        sessionDao.observeSessionsForClass(classId).map { it.toDomain() }

    override suspend fun getSessionById(sessionId: Long): AttendanceSession? =
        sessionDao.getSessionById(sessionId)?.toDomain()

    override suspend fun getRecordsForSession(sessionId: Long): List<AttendanceRecord> =
        recordDao.observeRecordsForSession(sessionId).first().toDomain()

    override suspend fun findSession(
        classId: Long,
        scheduleId: Long,
        date: LocalDate
    ): AttendanceSession? {
        return sessionDao.findSession(classId, scheduleId, date.toString())?.toDomain()
    }

    override suspend fun saveAttendance(
        classId: Long,
        scheduleId: Long,
        date: LocalDate,
        lessonNotes: String?,
        records: List<AttendanceRecord>
    ): RepositoryResult<Long> {
        return try {
            val normalizedLessonNotes = lessonNotes
                ?.let(InputNormalizer::normalize)
                ?.ifBlank { null }

            val sessionId = db.withTransaction {
                val existingSession = sessionDao.findSession(classId, scheduleId, date.toString())

                val resolvedSessionId = if (existingSession != null) {
                    sessionDao.updateSession(existingSession.copy(lessonNotes = normalizedLessonNotes))
                    existingSession.sessionId
                } else {
                    sessionDao.insertSession(
                        AttendanceSession(
                            sessionId = 0L,
                            classId = classId,
                            scheduleId = scheduleId,
                            date = date,
                            lessonNotes = normalizedLessonNotes,
                            createdAt = LocalDate.now()
                        ).toEntity()
                    )
                }

                recordDao.deleteAllRecordsForSession(resolvedSessionId)
                if (records.isNotEmpty()) {
                    val recordsForSession = records.map { it.copy(sessionId = resolvedSessionId) }
                    recordDao.insertRecords(recordsForSession.toEntity())
                }

                resolvedSessionId
            }

            RepositoryResult.Success(sessionId)
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to save attendance: ${e.message}")
        }
    }

    override suspend fun deleteSession(sessionId: Long): RepositoryResult<Unit> {
        return try {
            val deletedRows = sessionDao.deleteSession(sessionId)
            if (deletedRows == 0) {
                RepositoryResult.Error("Attendance session not found")
            } else {
                RepositoryResult.Success(Unit)
            }
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to delete session: ${e.message}")
        }
    }

    override suspend fun countSessionsOutsideDateRange(
        classId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Int {
        return sessionDao.countSessionsOutsideDateRange(
            classId = classId,
            startDate = startDate,
            endDate = endDate
        )
    }
}
