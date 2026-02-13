package com.uteacher.attendancetracker.ui.screen.dashboard

import com.uteacher.attendancetracker.domain.model.FabPosition
import com.uteacher.attendancetracker.domain.model.Note
import com.uteacher.attendancetracker.domain.model.ScheduledClassItem
import java.time.LocalDate
import java.time.YearMonth

data class DashboardUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val currentMonth: YearMonth = YearMonth.now(),
    val calendarExpanded: Boolean = false,
    val scheduledClasses: List<ScheduledClassItem> = emptyList(),
    val notes: List<Note> = emptyList(),
    val datesWithClasses: Set<LocalDate> = emptySet(),
    val datesWithNotes: Set<LocalDate> = emptySet(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val fabMenuExpanded: Boolean = false,
    val fabPosition: FabPosition = FabPosition.RIGHT
)
