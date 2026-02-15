package com.uteacher.attenote.ui.util

import java.time.LocalTime
import java.time.temporal.ChronoUnit

fun computeDurationMinutes(startTime: LocalTime, endTime: LocalTime): Int {
    return ChronoUnit.MINUTES.between(startTime, endTime).toInt()
}

fun formatDurationCompact(durationMinutes: Int): String {
    if (durationMinutes <= 0) {
        return "0m"
    }

    val hours = durationMinutes / 60
    val minutes = durationMinutes % 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}
