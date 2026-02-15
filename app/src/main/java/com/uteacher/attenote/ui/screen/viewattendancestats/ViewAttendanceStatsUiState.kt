package com.uteacher.attenote.ui.screen.viewattendancestats

import com.uteacher.attenote.domain.model.AttendanceStatus
import java.time.LocalDate

data class ViewAttendanceStatsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val summary: AttendanceSessionSummary? = null,
    val records: List<ViewAttendanceStudentRecord> = emptyList()
)

data class AttendanceSessionSummary(
    val sessionId: Long,
    val classId: Long,
    val scheduleId: Long,
    val date: LocalDate,
    val className: String,
    val subjectLine: String,
    val scheduleLine: String,
    val isClassTaken: Boolean,
    val presentCount: Int,
    val absentCount: Int,
    val skippedCount: Int,
    val totalCount: Int,
    val lessonNotes: String,
    val canEditAttendance: Boolean
)

data class ViewAttendanceStudentRecord(
    val studentId: Long,
    val name: String,
    val registrationNumber: String,
    val rollNumber: String,
    val status: AttendanceStatus
)
