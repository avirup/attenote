package com.uteacher.attendancetracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "students",
    indices = [
        Index(value = ["name", "registrationNumber"], unique = true)
    ]
)
data class StudentEntity(
    @PrimaryKey(autoGenerate = true)
    val studentId: Long = 0L,
    val name: String,
    val registrationNumber: String,
    val rollNumber: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val isActive: Boolean = true,
    val createdAt: LocalDate = LocalDate.now()
)
