package com.uteacher.attenote.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance_records",
    foreignKeys = [
        ForeignKey(
            entity = AttendanceSessionEntity::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["studentId"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sessionId", "studentId"], unique = true),
        Index("sessionId"),
        Index("studentId")
    ]
)
data class AttendanceRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val recordId: Long = 0L,
    val sessionId: Long,
    val studentId: Long,
    val isPresent: Boolean = true,
    @ColumnInfo(defaultValue = "'PRESENT'")
    val status: String = "PRESENT"
)
