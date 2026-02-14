package com.uteacher.attenote.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import java.time.LocalDate

@Entity(
    tableName = "class_student_cross_ref",
    primaryKeys = ["classId", "studentId"],
    foreignKeys = [
        ForeignKey(
            entity = ClassEntity::class,
            parentColumns = ["classId"],
            childColumns = ["classId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["studentId"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("classId"),
        Index("studentId")
    ]
)
data class ClassStudentCrossRef(
    val classId: Long,
    val studentId: Long,
    val isActiveInClass: Boolean = true,
    val addedAt: LocalDate = LocalDate.now()
)
