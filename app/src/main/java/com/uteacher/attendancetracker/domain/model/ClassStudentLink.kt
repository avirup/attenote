package com.uteacher.attendancetracker.domain.model

import java.time.LocalDate

data class ClassStudentLink(
    val classId: Long,
    val studentId: Long,
    val isActiveInClass: Boolean,
    val addedAt: LocalDate
)
