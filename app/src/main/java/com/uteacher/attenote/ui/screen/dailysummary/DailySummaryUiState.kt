package com.uteacher.attenote.ui.screen.dailysummary

import java.time.LocalDate

data class DailySummaryUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedFilter: DailySummaryContentFilter = DailySummaryContentFilter.BOTH,
    val isNotesOnlyModeEnabled: Boolean = false,
    val dateCards: List<DailySummaryDateCard> = emptyList()
)

enum class DailySummaryContentFilter(val label: String) {
    BOTH("Both"),
    NOTES_ONLY("Notes only"),
    ATTENDANCE_ONLY("Attendance only")
}

data class DailySummaryDateCard(
    val date: LocalDate,
    val attendanceEntries: List<DailySummaryAttendanceEntry> = emptyList(),
    val noteEntries: List<DailySummaryNoteEntry> = emptyList()
)

data class DailySummaryAttendanceEntry(
    val sessionId: Long,
    val classId: Long,
    val scheduleId: Long,
    val className: String,
    val isClassTaken: Boolean,
    val presentCount: Int,
    val absentCount: Int,
    val skippedCount: Int
)

data class DailySummaryNoteEntry(
    val noteId: Long,
    val title: String,
    val previewLine: String
)
