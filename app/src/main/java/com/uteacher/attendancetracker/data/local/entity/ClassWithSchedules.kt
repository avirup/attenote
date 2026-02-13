package com.uteacher.attendancetracker.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ClassWithSchedules(
    @Embedded
    val classEntity: ClassEntity,
    @Relation(
        parentColumn = "classId",
        entityColumn = "classId"
    )
    val schedules: List<ScheduleEntity>
)
