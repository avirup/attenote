package com.uteacher.attenote.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class SessionWithRecords(
    @Embedded
    val session: AttendanceSessionEntity,
    @Relation(
        parentColumn = "sessionId",
        entityColumn = "sessionId"
    )
    val records: List<AttendanceRecordEntity>
)
