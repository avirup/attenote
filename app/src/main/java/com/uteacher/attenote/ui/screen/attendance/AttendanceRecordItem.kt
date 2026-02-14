package com.uteacher.attenote.ui.screen.attendance

import com.uteacher.attenote.domain.model.Student

data class AttendanceRecordItem(
    val student: Student,
    val isPresent: Boolean = true
)
