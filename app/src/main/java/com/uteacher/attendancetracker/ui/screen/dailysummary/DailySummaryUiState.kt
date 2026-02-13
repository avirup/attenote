package com.uteacher.attendancetracker.ui.screen.dailysummary

import java.time.LocalDate

data class DailySummaryUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val searchQuery: String = "",
    val items: List<DailySummaryItem> = emptyList()
)

sealed interface DailySummaryItem {
    val date: LocalDate

    data class Attendance(
        override val date: LocalDate,
        val sessionId: Long,
        val classId: Long,
        val scheduleId: Long,
        val className: String,
        val presentCount: Int,
        val absentCount: Int
    ) : DailySummaryItem

    data class Note(
        override val date: LocalDate,
        val noteId: Long,
        val title: String,
        val previewLine: String
    ) : DailySummaryItem
}
