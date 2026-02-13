package com.uteacher.attendancetracker.ui.screen.dashboard.components

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarSection(
    expanded: Boolean,
    selectedDate: LocalDate,
    currentMonth: YearMonth,
    datesWithContent: Set<LocalDate>,
    weekRange: List<LocalDate>,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        if (expanded) {
            MonthCalendarView(
                selectedDate = selectedDate,
                currentMonth = currentMonth,
                datesWithContent = datesWithContent,
                onDateSelected = onDateSelected,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onCollapse = onToggleExpanded
            )
        } else {
            WeekCalendarView(
                selectedDate = selectedDate,
                weekRange = weekRange,
                datesWithContent = datesWithContent,
                onDateSelected = onDateSelected,
                onPreviousDay = onPreviousDay,
                onNextDay = onNextDay,
                onExpand = onToggleExpanded
            )
        }
    }
}
