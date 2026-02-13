package com.uteacher.attendancetracker.domain.model

import java.time.LocalDate

data class AttendanceSession(
    val sessionId: Long,
    val classId: Long,
    val scheduleId: Long,
    val date: LocalDate,
    val lessonNotes: String?,
    val createdAt: LocalDate
)
