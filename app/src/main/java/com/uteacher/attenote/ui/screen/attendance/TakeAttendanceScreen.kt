package com.uteacher.attenote.ui.screen.attendance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
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
    val onSaveClick = remember(viewModel) { { viewModel.onSaveClicked() } }
    val latestIsLoading by rememberUpdatedState(uiState.isLoading)
    val latestIsSaving by rememberUpdatedState(uiState.isSaving)
    val latestShouldNavigateBack by rememberUpdatedState(uiState.shouldNavigateBack)
    val latestHasRecords by rememberUpdatedState(uiState.attendanceRecords.isNotEmpty())
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
                    title = if (uiState.isSaving) "Saving..." else "Save",
                    enabled = !uiState.isLoading && !uiState.isSaving,
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
                        enabled = true,
                        onClick = onSaveClick
                    )
                )
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            onSetActionBarPrimaryAction(null)
            if (!latestIsLoading && !latestIsSaving && !latestShouldNavigateBack && latestHasRecords) {
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 24.dp),
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
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Students (${uiState.attendanceRecords.size})",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    items(
                        items = uiState.attendanceRecords,
                        key = { it.student.studentId }
                    ) { record ->
                        AttendanceRecordCard(
                            record = record,
                            onTogglePresent = { isPresent ->
                                viewModel.onToggleStudentPresent(record.student.studentId, isPresent)
                            }
                        )
                    }

                    item {
                        AttenoteSectionCard(title = "Lesson Notes (optional)") {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                AttenoteTextField(
                                    value = uiState.lessonNotes,
                                    onValueChange = viewModel::onLessonNotesChanged,
                                    label = "Lesson notes",
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !uiState.isSaving,
                                    minLines = 4,
                                    maxLines = 8
                                )
                                Text(
                                    text = "Quick notes about today's lesson (plain text)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
