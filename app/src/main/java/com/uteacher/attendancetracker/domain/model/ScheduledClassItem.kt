package com.uteacher.attendancetracker.domain.model

import java.time.DayOfWeek
import java.time.LocalTime

data class ScheduledClassItem(
    val classId: Long,
    val className: String,
    val subject: String,
    val scheduleId: Long,
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val instituteName: String,
    val session: String,
    val department: String,
    val semester: String,
    val section: String
)
