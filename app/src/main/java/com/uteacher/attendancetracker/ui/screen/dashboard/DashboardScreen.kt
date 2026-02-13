package com.uteacher.attendancetracker.ui.screen.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.uteacher.attendancetracker.R
import com.uteacher.attendancetracker.domain.model.FabPosition
import com.uteacher.attendancetracker.ui.navigation.ActionBarPrimaryAction
import com.uteacher.attendancetracker.ui.screen.dashboard.components.CalendarSection
import com.uteacher.attendancetracker.ui.screen.dashboard.components.HamburgerFabMenu
import com.uteacher.attendancetracker.ui.screen.dashboard.components.NoteCard
import com.uteacher.attendancetracker.ui.screen.dashboard.components.ScheduledClassCard
import com.uteacher.attendancetracker.ui.theme.component.AttenoteSectionCard
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import org.koin.androidx.compose.koinViewModel
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(
    onNavigateToCreateClass: () -> Unit,
    onNavigateToManageClassList: () -> Unit,
    onNavigateToManageStudents: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDailySummary: () -> Unit,
    onNavigateToTakeAttendance: (classId: Long, scheduleId: Long, date: String) -> Unit,
    onNavigateToAddNote: (date: String, noteId: Long) -> Unit,
    onSetActionBarPrimaryAction: (ActionBarPrimaryAction?) -> Unit,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val listState = rememberLazyListState()
    val fabSwipeThresholdPx = with(LocalDensity.current) { 24.dp.toPx() }
    val calendarSwipeThresholdPx = with(LocalDensity.current) { 32.dp.toPx() }
    val contentBottomPadding = if (uiState.calendarExpanded) 408.dp else 228.dp
    val snackbarBottomPadding = if (uiState.calendarExpanded) 360.dp else 196.dp

    var fabAccumulatedDrag by remember { mutableFloatStateOf(0f) }
    var fabDragOffsetPx by remember { mutableFloatStateOf(0f) }

    var calendarAccumulatedDrag by remember { mutableFloatStateOf(0f) }
    var calendarDragOffsetPx by remember { mutableFloatStateOf(0f) }

    val summaryAction = remember(onNavigateToDailySummary) {
        ActionBarPrimaryAction(
            title = "Summary",
            iconResId = R.drawable.ic_daily_summary_24,
            contentDescription = "Open summary",
            onClick = onNavigateToDailySummary
        )
    }

    SideEffect {
        onSetActionBarPrimaryAction(summaryAction)
    }

    DisposableEffect(onSetActionBarPrimaryAction, lifecycleOwner, summaryAction) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onSetActionBarPrimaryAction(summaryAction)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            onSetActionBarPrimaryAction(null)
        }
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            viewModel.onContentScrolled()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp,
                bottom = contentBottomPadding
            ),
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

        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                .offset { IntOffset(0, calendarDragOffsetPx.roundToInt()) }
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta ->
                        calendarAccumulatedDrag += delta
                        calendarDragOffsetPx = (calendarDragOffsetPx + (delta * 0.35f))
                            .coerceIn(-32f, 32f)
                    },
                    onDragStopped = {
                        when {
                            calendarAccumulatedDrag <= -calendarSwipeThresholdPx -> {
                                viewModel.setCalendarExpanded(true)
                            }

                            calendarAccumulatedDrag >= calendarSwipeThresholdPx -> {
                                viewModel.setCalendarExpanded(false)
                            }
                        }
                        calendarAccumulatedDrag = 0f
                        calendarDragOffsetPx = 0f
                    }
                )
                .zIndex(1f)
        ) {
            CalendarSection(
                modifier = Modifier.fillMaxWidth(),
                expanded = uiState.calendarExpanded,
                selectedDate = uiState.selectedDate,
                currentMonth = uiState.currentMonth,
                datesWithContent = uiState.datesWithClasses + uiState.datesWithNotes,
                weekRange = viewModel.getWeekRange(uiState.selectedDate),
                onDateSelected = viewModel::onDateSelected,
                onPreviousDay = viewModel::onPreviousDayClicked,
                onNextDay = viewModel::onNextDayClicked,
                onPreviousMonth = viewModel::onPreviousMonthClicked,
                onNextMonth = viewModel::onNextMonthClicked,
                onToggleExpanded = viewModel::onToggleCalendar
            )
        }

        if (uiState.fabMenuExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(2f)
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f))
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
                .padding(start = 16.dp, end = 16.dp, bottom = 28.dp)
                .offset { IntOffset(fabDragOffsetPx.roundToInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        fabAccumulatedDrag += delta
                        fabDragOffsetPx = (fabDragOffsetPx + delta).coerceIn(-72f, 72f)
                    },
                    onDragStopped = {
                        when {
                            fabAccumulatedDrag <= -fabSwipeThresholdPx -> viewModel.onFabSwipedLeft()
                            fabAccumulatedDrag >= fabSwipeThresholdPx -> viewModel.onFabSwipedRight()
                        }
                        fabAccumulatedDrag = 0f
                        fabDragOffsetPx = 0f
                    }
                )
                .zIndex(3f)
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
                    .padding(bottom = snackbarBottomPadding, start = 16.dp, end = 16.dp)
            ) {
                Text(text = message)
            }
        }
    }
}

private fun formatDateHeader(date: LocalDate): String {
    val dayName = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val monthName = date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    return "$dayName, $monthName ${date.dayOfMonth}, ${date.year}"
}
