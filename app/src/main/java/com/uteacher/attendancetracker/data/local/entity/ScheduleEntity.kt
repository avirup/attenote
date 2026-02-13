package com.uteacher.attendancetracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.LocalTime

@Entity(
    tableName = "schedules",
    foreignKeys = [
        ForeignKey(
            entity = ClassEntity::class,
            parentColumns = ["classId"],
            childColumns = ["classId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("classId"),
        Index(value = ["classId", "dayOfWeek"])
    ]
)
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val scheduleId: Long = 0L,
    val classId: Long,
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime
)
