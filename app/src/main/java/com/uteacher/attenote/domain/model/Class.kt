package com.uteacher.attenote.domain.model

import java.time.LocalDate

data class Class(
    val classId: Long,
    val instituteName: String,
    val session: String,
    val department: String,
    val semester: String,
    val section: String,
    val subject: String,
    val className: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val isOpen: Boolean,
    val createdAt: LocalDate
)
