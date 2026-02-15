package com.uteacher.attenote.data.local.dao
import kotlin.jvm.JvmSuppressWildcards

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.uteacher.attenote.data.local.entity.AttendanceRecordEntity
import kotlinx.coroutines.flow.Flow

data class AttendanceCountersProjection(
    val presentCount: Int,
    val absentCount: Int,
    val skippedCount: Int,
    val totalCount: Int
)

@Dao
@JvmSuppressWildcards
interface AttendanceRecordDao {

    @Query("SELECT * FROM attendance_records WHERE sessionId = :sessionId ORDER BY studentId ASC")
    fun observeRecordsForSession(sessionId: Long): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records WHERE recordId = :id")
    suspend fun getRecordById(id: Long): AttendanceRecordEntity?

    @Query(
        """
        SELECT * FROM attendance_records
        WHERE sessionId = :sessionId AND studentId = :studentId
        """
    )
    suspend fun getRecord(sessionId: Long, studentId: Long): AttendanceRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AttendanceRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<AttendanceRecordEntity>): List<Long>

    @Update
    suspend fun updateRecord(record: AttendanceRecordEntity): Int

    @Query("DELETE FROM attendance_records WHERE recordId = :recordId")
    suspend fun deleteRecord(recordId: Long): Int

    @Query("DELETE FROM attendance_records WHERE sessionId = :sessionId")
    suspend fun deleteAllRecordsForSession(sessionId: Long): Int

    @Query("DELETE FROM attendance_records WHERE studentId = :studentId")
    suspend fun deleteAllRecordsForStudent(studentId: Long): Int

    @Query(
        """
        DELETE FROM attendance_records
        WHERE sessionId IN (
            SELECT sessionId FROM attendance_sessions WHERE classId = :classId
        )
        """
    )
    suspend fun deleteAllRecordsForClass(classId: Long): Int

    @Query(
        """
        SELECT
            COALESCE(SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END), 0) AS presentCount,
            COALESCE(SUM(CASE WHEN status = 'ABSENT' THEN 1 ELSE 0 END), 0) AS absentCount,
            COALESCE(SUM(CASE WHEN status = 'SKIPPED' THEN 1 ELSE 0 END), 0) AS skippedCount,
            COUNT(*) AS totalCount
        FROM attendance_records
        WHERE sessionId = :sessionId
        """
    )
    suspend fun getAttendanceCountersForSession(sessionId: Long): AttendanceCountersProjection
}
