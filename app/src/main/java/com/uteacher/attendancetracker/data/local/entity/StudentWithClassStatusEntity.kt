package com.uteacher.attendancetracker.data.local.entity

import androidx.room.Embedded

data class StudentWithClassStatusEntity(
    @Embedded val student: StudentEntity,
    val isActiveInClass: Boolean
)
