package com.uteacher.attendancetracker.data.repository.internal

import com.uteacher.attendancetracker.domain.model.Schedule
import java.time.LocalTime

object ScheduleValidation {

    fun validateTimeOrder(startTime: LocalTime, endTime: LocalTime): String? {
        return if (endTime <= startTime) {
            "End time must be after start time"
        } else {
            null
        }
    }

    /**
     * Two schedules overlap when: start1 < end2 && start2 < end1
     */
    fun validateNoOverlap(
        schedules: List<Schedule>,
        newSchedule: Schedule
    ): String? {
        val conflicting = schedules.any { existing ->
            existing !== newSchedule &&
                existing.classId == newSchedule.classId &&
                existing.dayOfWeek == newSchedule.dayOfWeek &&
                existing.startTime < newSchedule.endTime &&
                newSchedule.startTime < existing.endTime
        }

        return if (conflicting) {
            "Schedule overlaps with existing schedule on ${newSchedule.dayOfWeek}"
        } else {
            null
        }
    }
}
