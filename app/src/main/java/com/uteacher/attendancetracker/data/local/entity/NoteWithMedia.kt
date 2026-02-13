package com.uteacher.attendancetracker.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class NoteWithMedia(
    @Embedded
    val note: NoteEntity,
    @Relation(
        parentColumn = "noteId",
        entityColumn = "noteId"
    )
    val media: List<NoteMediaEntity>
)
