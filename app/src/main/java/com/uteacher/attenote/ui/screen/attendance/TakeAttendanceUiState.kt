package com.uteacher.attenote.ui.screen.attendance

import com.uteacher.attenote.domain.model.Class
import com.uteacher.attenote.domain.model.Schedule
import java.time.LocalDate

data class TakeAttendanceUiState(
    val classId: Long = 0L,
    val scheduleId: Long = 0L,
    val date: LocalDate? = null,
    val classItem: Class? = null,
    val schedule: Schedule? = null,
    val attendanceRecords: List<AttendanceRecordItem> = emptyList(),
    val lessonNotes: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null,
    val shouldNavigateBack: Boolean = false
)
