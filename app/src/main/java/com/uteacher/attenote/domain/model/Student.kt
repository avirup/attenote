package com.uteacher.attenote.domain.model

import java.time.LocalDate

data class Student(
    val studentId: Long,
    val name: String,
    val registrationNumber: String,
    val rollNumber: String?,
    val email: String?,
    val phone: String?,
    val department: String = "",
    val isActive: Boolean,
    val createdAt: LocalDate
)
