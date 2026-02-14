package com.uteacher.attenote.domain.model

import java.time.DayOfWeek
import java.time.LocalTime

data class Schedule(
    val scheduleId: Long,
    val classId: Long,
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime
)
