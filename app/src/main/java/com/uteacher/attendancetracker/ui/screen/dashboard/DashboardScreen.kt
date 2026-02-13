package com.uteacher.attendancetracker.ui.screen.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.uteacher.attendancetracker.domain.model.FabPosition
import com.uteacher.attendancetracker.ui.screen.dashboard.components.CalendarSection
import com.uteacher.attendancetracker.ui.screen.dashboard.components.HamburgerFabMenu
import com.uteacher.attendancetracker.ui.screen.dashboard.components.NoteCard
import com.uteacher.attendancetracker.ui.screen.dashboard.components.ScheduledClassCard
import com.uteacher.attendancetracker.ui.theme.component.AttenoteSectionCard
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import org.koin.androidx.compose.koinViewModel

@Composable
fun DashboardScreen(
    onNavigateToCreateClass: () -> Unit,
    onNavigateToManageClassList: () -> Unit,
    onNavigateToManageStudents: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToTakeAttendance: (classId: Long, scheduleId: Long, date: String) -> Unit,
    onNavigateToAddNote: (date: String, noteId: Long) -> Unit,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            viewModel.onContentScrolled()
        }
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = formatDateHeader(uiState.selectedDate),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                item {
                    AttenoteSectionCard(title = "Scheduled Classes") {
                        if (uiState.scheduledClasses.isEmpty()) {
                            Text(
                                text = "No classes scheduled for this date",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(8.dp)
                            )
                        } else {
                            androidx.compose.foundation.layout.Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                uiState.scheduledClasses.forEach { scheduledClass ->
                                    ScheduledClassCard(
                                        scheduledClass = scheduledClass,
                                        onTakeAttendance = {
                                            onNavigateToTakeAttendance(
                                                scheduledClass.classId,
                                                scheduledClass.scheduleId,
                                                uiState.selectedDate.toString()
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    androidx.compose.foundation.layout.Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Notes",
                                style = MaterialTheme.typography.titleMedium
                            )
                            TextButton(
                                onClick = {
                                    onNavigateToAddNote(uiState.selectedDate.toString(), -1L)
                                }
                            ) {
                                Text(text = "+ New Note")
                            }
                        }

                        AttenoteSectionCard {
                            if (uiState.notes.isEmpty()) {
                                Text(
                                    text = "No notes for this date",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(8.dp)
                                )
                            } else {
                                androidx.compose.foundation.layout.Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    uiState.notes.forEach { note ->
                                        NoteCard(
                                            note = note,
                                            onOpenNote = {
                                                onNavigateToAddNote(note.date.toString(), note.noteId)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    CalendarSection(
                        modifier = Modifier.fillMaxWidth(),
                        expanded = uiState.calendarExpanded,
                        selectedDate = uiState.selectedDate,
                        currentMonth = uiState.currentMonth,
                        datesWithContent = uiState.datesWithClasses + uiState.datesWithNotes,
                        weekRange = viewModel.getWeekRange(uiState.selectedDate),
                        onDateSelected = viewModel::onDateSelected,
                        onPreviousWeek = viewModel::onPreviousWeekClicked,
                        onNextWeek = viewModel::onNextWeekClicked,
                        onPreviousMonth = viewModel::onPreviousMonthClicked,
                        onNextMonth = viewModel::onNextMonthClicked,
                        onToggleExpanded = viewModel::onToggleCalendar
                    )
                }
            }

            if (uiState.fabMenuExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = viewModel::onDismissFabMenu
                        )
                )
            }

            Box(
                modifier = Modifier
                    .align(
                        when (uiState.fabPosition) {
                            FabPosition.LEFT -> Alignment.BottomStart
                            FabPosition.RIGHT -> Alignment.BottomEnd
                        }
                    )
                    .padding(16.dp)
            ) {
                HamburgerFabMenu(
                    expanded = uiState.fabMenuExpanded,
                    onToggle = viewModel::onToggleFabMenu,
                    onCreateClass = {
                        onNavigateToCreateClass()
                        viewModel.onDismissFabMenu()
                    },
                    onManageClasses = {
                        onNavigateToManageClassList()
                        viewModel.onDismissFabMenu()
                    },
                    onManageStudents = {
                        onNavigateToManageStudents()
                        viewModel.onDismissFabMenu()
                    },
                    onSettings = {
                        onNavigateToSettings()
                        viewModel.onDismissFabMenu()
                    }
                )
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error?.let { message ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 84.dp, start = 16.dp, end = 16.dp)
                ) {
                    Text(text = message)
                }
            }
        }
    }
}

private fun formatDateHeader(date: LocalDate): String {
    val dayName = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val monthName = date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    return "$dayName, $monthName ${date.dayOfMonth}, ${date.year}"
}
