package com.uteacher.attenote.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "attendance_sessions",
    foreignKeys = [
        ForeignKey(
            entity = ClassEntity::class,
            parentColumns = ["classId"],
            childColumns = ["classId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ScheduleEntity::class,
            parentColumns = ["scheduleId"],
            childColumns = ["scheduleId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["classId", "scheduleId", "date"], unique = true),
        Index("classId"),
        Index("scheduleId"),
        Index("date")
    ]
)
data class AttendanceSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val sessionId: Long = 0L,
    val classId: Long,
    val scheduleId: Long,
    val date: LocalDate,
    @ColumnInfo(defaultValue = "1")
    val isClassTaken: Boolean = true,
    val lessonNotes: String? = null,
    val createdAt: LocalDate = LocalDate.now()
)
