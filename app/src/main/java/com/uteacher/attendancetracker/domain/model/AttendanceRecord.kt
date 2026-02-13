package com.uteacher.attendancetracker.domain.model

data class AttendanceRecord(
    val recordId: Long,
    val sessionId: Long,
    val studentId: Long,
    val isPresent: Boolean
)
