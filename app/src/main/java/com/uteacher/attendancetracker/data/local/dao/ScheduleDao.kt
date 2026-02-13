package com.uteacher.attendancetracker.data.local.dao
import kotlin.jvm.JvmSuppressWildcards

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.uteacher.attendancetracker.data.local.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface ScheduleDao {

    @Query(
        """
        SELECT * FROM schedules
        WHERE classId = :classId
        ORDER BY dayOfWeek ASC, startTime ASC
        """
    )
    fun observeSchedulesForClass(classId: Long): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules WHERE scheduleId = :id")
    suspend fun getScheduleById(id: Long): ScheduleEntity?

    @Query(
        """
        SELECT * FROM schedules
        WHERE classId = :classId AND dayOfWeek = :dayOfWeek
        ORDER BY startTime ASC
        """
    )
    suspend fun getSchedulesForDay(classId: Long, dayOfWeek: Int): List<ScheduleEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSchedule(schedule: ScheduleEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSchedules(schedules: List<ScheduleEntity>): List<Long>

    @Update
    suspend fun updateSchedule(schedule: ScheduleEntity): Int

    @Query("DELETE FROM schedules WHERE scheduleId = :scheduleId")
    suspend fun deleteSchedule(scheduleId: Long): Int

    @Query("DELETE FROM schedules WHERE classId = :classId")
    suspend fun deleteAllSchedulesForClass(classId: Long): Int
}
