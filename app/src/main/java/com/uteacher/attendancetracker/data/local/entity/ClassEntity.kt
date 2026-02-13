package com.uteacher.attendancetracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "classes",
    indices = [
        Index(
            value = ["instituteName", "session", "department", "semester", "subject", "section"],
            unique = true
        )
    ]
)
data class ClassEntity(
    @PrimaryKey(autoGenerate = true)
    val classId: Long = 0L,
    val instituteName: String,
    val session: String,
    val department: String,
    val semester: String,
    val section: String = "",
    val subject: String,
    val className: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val isOpen: Boolean = true,
    val createdAt: LocalDate = LocalDate.now()
)
