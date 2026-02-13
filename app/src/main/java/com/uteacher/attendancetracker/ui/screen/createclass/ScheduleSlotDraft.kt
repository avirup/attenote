package com.uteacher.attendancetracker.ui.screen.createclass

import java.time.DayOfWeek
import java.time.LocalTime

data class ScheduleSlotDraft(
    val dayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
    val startTime: LocalTime = LocalTime.of(9, 0),
    val endTime: LocalTime = LocalTime.of(10, 30),
    val validationError: String? = null
)
