package com.uteacher.attendancetracker.domain.model

import java.time.LocalDate

data class NoteMedia(
    val mediaId: Long,
    val noteId: Long,
    val filePath: String,
    val mimeType: String,
    val addedAt: LocalDate
)
