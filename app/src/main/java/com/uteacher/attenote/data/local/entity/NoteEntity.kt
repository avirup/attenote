package com.uteacher.attenote.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = ClassEntity::class,
            parentColumns = ["classId"],
            childColumns = ["classId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("date"), Index("classId")]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val noteId: Long = 0L,
    val title: String,
    val content: String,
    val date: LocalDate,
    val classId: Long? = null,
    val createdAt: LocalDate = LocalDate.now(),
    val updatedAt: LocalDate = LocalDate.now()
)
