package com.uteacher.attendancetracker.ui.screen.attendance

import com.uteacher.attendancetracker.domain.model.Student

data class AttendanceRecordItem(
    val student: Student,
    val isPresent: Boolean = true
)
