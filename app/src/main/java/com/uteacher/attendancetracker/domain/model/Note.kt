package com.uteacher.attendancetracker.domain.model

import java.time.LocalDate

data class Note(
    val noteId: Long,
    val title: String,
    val content: String,
    val date: LocalDate,
    val createdAt: LocalDate,
    val updatedAt: LocalDate
)
