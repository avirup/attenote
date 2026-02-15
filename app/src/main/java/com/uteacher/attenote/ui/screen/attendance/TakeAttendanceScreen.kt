package com.uteacher.attenote.ui.screen.attendance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.uteacher.attenote.ui.navigation.ActionBarPrimaryAction
import com.uteacher.attenote.ui.screen.attendance.components.AttendanceRecordCard
import com.uteacher.attenote.ui.theme.component.AttenoteSecondaryButton
import com.uteacher.attenote.ui.theme.component.AttenoteSectionCard
import com.uteacher.attenote.ui.theme.component.AttenoteTextField
import com.uteacher.attenote.ui.util.computeDurationMinutes
import com.uteacher.attenote.ui.util.formatDurationCompact
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

private val TimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

@Composable
fun TakeAttendanceScreen(
    classId: Long,
    scheduleId: Long,
    date: String,
    onNavigateBack: () -> Unit,
    onSetActionBarPrimaryAction: (ActionBarPrimaryAction?) -> Unit,
    viewModel: TakeAttendanceViewModel = koinViewModel(
        parameters = { parametersOf(classId, scheduleId, date) }
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredAttendanceRecords = remember(uiState.attendanceRecords, uiState.searchQuery) {
        filterAttendanceRecords(
            records = uiState.attendanceRecords,
            searchQuery = uiState.searchQuery
        )
    }
    val onSaveClick = remember(viewModel) { { viewModel.onSaveClicked() } }
    val latestIsLoading by rememberUpdatedState(uiState.isLoading)
    val latestIsSaving by rememberUpdatedState(uiState.isSaving)
    val latestIsAutoSaving by rememberUpdatedState(uiState.isAutoSaving)
    val latestShouldNavigateBack by rememberUpdatedState(uiState.shouldNavigateBack)
    val latestHasPendingChanges by rememberUpdatedState(uiState.hasPendingChanges)
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onNavigateBack()
        }
    }
    LaunchedEffect(uiState.shouldNavigateBack) {
        if (uiState.shouldNavigateBack) {
            delay(700)
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.error, uiState.attendanceRecords.isEmpty()) {
        if (
            !uiState.error.isNullOrBlank() &&
            uiState.attendanceRecords.isNotEmpty() &&
            !uiState.shouldNavigateBack
        ) {
            delay(2500)
            viewModel.onErrorShown()
        }
    }

    SideEffect {
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            onSetActionBarPrimaryAction(
                ActionBarPrimaryAction(
                    title = when {
                        uiState.isSaving -> "Saving..."
                        uiState.isAutoSaving -> "Saving draft..."
                        else -> "Save"
                    },
                    enabled = !uiState.isLoading && !uiState.isSaving && !uiState.isAutoSaving,
                    onClick = onSaveClick
                )
            )
        }
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onSetActionBarPrimaryAction(
                    ActionBarPrimaryAction(
                        title = "Save",
                        enabled = !latestIsAutoSaving,
                        onClick = onSaveClick
                    )
                )
            } else if (event == Lifecycle.Event.ON_STOP) {
                if (
                    !latestIsLoading &&
                    !latestIsSaving &&
                    !latestIsAutoSaving &&
                    !latestShouldNavigateBack &&
                    latestHasPendingChanges
                ) {
                    viewModel.onAutoSaveBackground()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            onSetActionBarPrimaryAction(null)
            if (
                !latestIsLoading &&
                !latestIsSaving &&
                !latestIsAutoSaving &&
                !latestShouldNavigateBack &&
                latestHasPendingChanges
            ) {
                viewModel.onAutoSaveExit()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            !uiState.error.isNullOrBlank() && uiState.attendanceRecords.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = uiState.error!!,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    AttenoteSecondaryButton(
                        text = "Go Back",
                        onClick = onNavigateBack,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }

            else -> {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        LessonNotesBottomBar(
                            lessonNotes = uiState.lessonNotes,
                            onLessonNotesChanged = viewModel::onLessonNotesChanged,
                            enabled = !uiState.isSaving,
                            isAutoSaving = uiState.isAutoSaving
                        )
                    }
                ) { innerPadding ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            top = 16.dp,
                            end = 16.dp,
                            bottom = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            AttenoteSectionCard(title = "Class Information") {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    uiState.classItem?.let { classItem ->
                                        Text(
                                            text = classItem.className,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = "${classItem.subject} • ${classItem.semester}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    uiState.schedule?.let { schedule ->
                                        val dayLabel = schedule.dayOfWeek.getDisplayName(
                                            TextStyle.FULL,
                                            Locale.getDefault()
                                        )
                                        val durationMinutes = schedule.durationMinutes.takeIf { it > 0 }
                                            ?: computeDurationMinutes(schedule.startTime, schedule.endTime)
                                        Text(
                                            text = "$dayLabel • ${
                                                schedule.startTime.format(TimeFormatter)
                                            } - ${schedule.endTime.format(TimeFormatter)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        if (durationMinutes > 0) {
                                            Text(
                                                text = "Duration: ${formatDurationCompact(durationMinutes)}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    uiState.date?.let { currentDate ->
                                        Text(
                                            text = "Date: $currentDate",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        FilterChip(
                                            selected = uiState.isClassTaken,
                                            onClick = { viewModel.onClassTakenChanged(true) },
                                            enabled = !uiState.isSaving,
                                            label = { Text("Taken") }
                                        )
                                        FilterChip(
                                            selected = !uiState.isClassTaken,
                                            onClick = { viewModel.onClassTakenChanged(false) },
                                            enabled = !uiState.isSaving,
                                            label = { Text("Not Taken") }
                                        )
                                    }

                                    Text(
                                        text = if (uiState.isClassTaken) {
                                            "Class is marked Taken."
                                        } else {
                                            "Class is marked Not Taken. Attendance is locked to Skipped."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Students",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                AttenoteTextField(
                                    value = uiState.searchQuery,
                                    onValueChange = viewModel::onSearchQueryChanged,
                                    label = "Search by name, registration, or roll",
                                    singleLine = true,
                                    enabled = !uiState.isSaving
                                )
                                Text(
                                    text = "Showing ${filteredAttendanceRecords.size} of ${uiState.attendanceRecords.size}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (filteredAttendanceRecords.isEmpty()) {
                            item {
                                Text(
                                    text = "No students match the current search.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            items(
                                items = filteredAttendanceRecords,
                                key = { it.student.studentId }
                            ) { record ->
                                AttendanceRecordCard(
                                    record = record,
                                    enabled = uiState.isClassTaken && !uiState.isSaving,
                                    onTogglePresent = { isPresent ->
                                        viewModel.onToggleStudentPresent(record.student.studentId, isPresent)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (!uiState.error.isNullOrBlank() && uiState.attendanceRecords.isNotEmpty()) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text(text = uiState.error!!)
            }
        }
    }
}

@Composable
private fun LessonNotesBottomBar(
    lessonNotes: String,
    onLessonNotesChanged: (String) -> Unit,
    enabled: Boolean,
    isAutoSaving: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Lesson Notes",
                style = MaterialTheme.typography.titleSmall
            )
            AttenoteTextField(
                value = lessonNotes,
                onValueChange = onLessonNotesChanged,
                label = "Lesson notes",
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                minLines = 2,
                maxLines = 4
            )
            Text(
                text = if (isAutoSaving) {
                    "Saving draft..."
                } else {
                    "Draft autosaves while typing"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun filterAttendanceRecords(
    records: List<AttendanceRecordItem>,
    searchQuery: String
): List<AttendanceRecordItem> {
    val query = searchQuery.trim()
    if (query.isBlank()) {
        return records
    }

    return records.filter { record ->
        record.student.name.contains(query, ignoreCase = true) ||
            record.student.registrationNumber.contains(query, ignoreCase = true) ||
            record.student.rollNumber.orEmpty().contains(query, ignoreCase = true)
    }
}
