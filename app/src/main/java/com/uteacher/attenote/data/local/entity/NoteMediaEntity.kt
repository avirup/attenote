package com.uteacher.attenote.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "note_media",
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["noteId"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("noteId")
    ]
)
data class NoteMediaEntity(
    @PrimaryKey(autoGenerate = true)
    val mediaId: Long = 0L,
    val noteId: Long,
    val filePath: String,
    val mimeType: String,
    val addedAt: LocalDate = LocalDate.now()
)
