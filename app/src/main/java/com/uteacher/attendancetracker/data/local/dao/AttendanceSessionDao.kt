package com.uteacher.attendancetracker.data.local.dao
import kotlin.jvm.JvmSuppressWildcards

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.uteacher.attendancetracker.data.local.entity.AttendanceSessionEntity
import com.uteacher.attendancetracker.data.local.entity.SessionWithRecords
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface AttendanceSessionDao {

    @Query("SELECT * FROM attendance_sessions WHERE classId = :classId ORDER BY date DESC")
    fun observeSessionsForClass(classId: Long): Flow<List<AttendanceSessionEntity>>

    @Query("SELECT * FROM attendance_sessions WHERE sessionId = :id")
    suspend fun getSessionById(id: Long): AttendanceSessionEntity?

    @Query(
        """
        SELECT * FROM attendance_sessions
        WHERE classId = :classId AND scheduleId = :scheduleId AND date = :date
        """
    )
    suspend fun findSession(classId: Long, scheduleId: Long, date: String): AttendanceSessionEntity?

    @Transaction
    @Query("SELECT * FROM attendance_sessions WHERE sessionId = :sessionId")
    suspend fun getSessionWithRecords(sessionId: Long): SessionWithRecords?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSession(session: AttendanceSessionEntity): Long

    @Update
    suspend fun updateSession(session: AttendanceSessionEntity): Int

    @Query("DELETE FROM attendance_sessions WHERE sessionId = :sessionId")
    suspend fun deleteSession(sessionId: Long): Int
}
