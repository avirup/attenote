package com.uteacher.attenote.data.repository

import android.database.sqlite.SQLiteConstraintException
import androidx.room.withTransaction
import com.uteacher.attenote.data.local.AppDatabase
import com.uteacher.attenote.data.local.dao.AttendanceRecordDao
import com.uteacher.attenote.data.local.dao.AttendanceSessionDao
import com.uteacher.attenote.data.repository.internal.AttendanceSaveNormalizer
import com.uteacher.attenote.data.repository.internal.InputNormalizer
import com.uteacher.attenote.data.repository.internal.RepositoryResult
import com.uteacher.attenote.domain.mapper.toDomain
import com.uteacher.attenote.domain.mapper.toEntity
import com.uteacher.attenote.domain.model.AttendanceRecord
import com.uteacher.attenote.domain.model.AttendanceStatus
import com.uteacher.attenote.domain.model.AttendanceSession
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

    override suspend fun getAttendanceCounters(sessionId: Long): AttendanceCounters {
        val counters = recordDao.getAttendanceCountersForSession(sessionId)
        return AttendanceCounters(
            present = counters.presentCount,
            absent = counters.absentCount,
            skipped = counters.skippedCount,
            total = counters.totalCount
        )
    }

    override suspend fun saveAttendance(
        classId: Long,
        scheduleId: Long,
        date: LocalDate,
        isClassTaken: Boolean,
        lessonNotes: String?,
        records: List<AttendanceStatusInput>
    ): RepositoryResult<Long> {
        if (records.map { it.studentId }.distinct().size != records.size) {
            return RepositoryResult.Error("Duplicate student entries are not allowed in attendance save")
        }

        return try {
            val normalizedLessonNotes = lessonNotes
                ?.let(InputNormalizer::normalize)
                ?.ifBlank { null }
            val normalizedRecords = AttendanceSaveNormalizer.normalizeRecordsForSession(
                isClassTaken = isClassTaken,
                records = records
            )
            val dateKey = date.toString()

            val sessionId = db.withTransaction {
                val existingSession = sessionDao.findSession(classId, scheduleId, dateKey)

                val resolvedSessionId = if (existingSession != null) {
                    sessionDao.updateSession(
                        existingSession.copy(
                            isClassTaken = isClassTaken,
                            lessonNotes = normalizedLessonNotes
                        )
                    )
                    existingSession.sessionId
                } else {
                    try {
                        sessionDao.insertSession(
                            AttendanceSession(
                                sessionId = 0L,
                                classId = classId,
                                scheduleId = scheduleId,
                                date = date,
                                isClassTaken = isClassTaken,
                                lessonNotes = normalizedLessonNotes,
                                createdAt = LocalDate.now()
                            ).toEntity()
                        )
                    } catch (_: SQLiteConstraintException) {
                        // Another concurrent save created the unique session first.
                        val racedSession = sessionDao.findSession(classId, scheduleId, dateKey)
                            ?: throw IllegalStateException("Failed to resolve attendance session after conflict")
                        sessionDao.updateSession(
                            racedSession.copy(
                                isClassTaken = isClassTaken,
                                lessonNotes = normalizedLessonNotes
                            )
                        )
                        racedSession.sessionId
                    }
                }

                recordDao.deleteAllRecordsForSession(resolvedSessionId)
                if (normalizedRecords.isNotEmpty()) {
                    val recordsForSession = normalizedRecords.map { statusInput ->
                        AttendanceRecord(
                            recordId = 0L,
                            sessionId = resolvedSessionId,
                            studentId = statusInput.studentId,
                            isPresent = statusInput.status == AttendanceStatus.PRESENT,
                            status = statusInput.status
                        )
                    }
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
