package com.uteacher.attenote.domain.model

data class AttendanceRecord(
    val recordId: Long,
    val sessionId: Long,
    val studentId: Long,
    val isPresent: Boolean,
    val status: AttendanceStatus = if (isPresent) AttendanceStatus.PRESENT else AttendanceStatus.ABSENT
)
