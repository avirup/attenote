package com.uteacher.attenote.ui.screen.viewattendancestats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.uteacher.attenote.domain.model.AttendanceStatus
import com.uteacher.attenote.ui.theme.component.AttenoteButton
import com.uteacher.attenote.ui.theme.component.AttenoteSectionCard
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ViewAttendanceStatsScreen(
    sessionId: Long,
    onEditAttendance: (classId: Long, scheduleId: Long, date: String) -> Unit,
    viewModel: ViewAttendanceStatsViewModel = koinViewModel(parameters = { parametersOf(sessionId) })
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.error) {
        if (!uiState.error.isNullOrBlank()) {
            delay(3000)
            viewModel.onErrorShown()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            uiState.summary == null -> {
                Text(
                    text = uiState.error ?: "Attendance summary unavailable.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }

            else -> {
                val summary = requireNotNull(uiState.summary)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(key = "session-info") {
                        AttenoteSectionCard(title = "Session Details") {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = summary.className,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = summary.subjectLine,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = summary.scheduleLine,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Date: ${summary.date.format(DATE_FORMATTER)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (summary.isClassTaken) {
                                        "Class status: Taken"
                                    } else {
                                        "Class status: Not Taken"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (summary.isClassTaken) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.error
                                    }
                                )
                            }
                        }
                    }

                    item(key = "attendance-counters") {
                        AttenoteSectionCard(title = "Attendance Stats") {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                StatLine(label = "Present", value = summary.presentCount)
                                StatLine(label = "Absent", value = summary.absentCount)
                                StatLine(label = "Skipped", value = summary.skippedCount)
                                StatLine(label = "Total", value = summary.totalCount)
                            }
                        }
                    }

                    item(key = "lesson-note") {
                        AttenoteSectionCard(title = "Lesson Note") {
                            Text(
                                text = summary.lessonNotes.ifBlank {
                                    "No lesson note linked to this attendance session."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    item(key = "student-records") {
                        AttenoteSectionCard(title = "Student Records") {
                            if (uiState.records.isEmpty()) {
                                Text(
                                    text = "No student records captured for this session.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    uiState.records.forEach { record ->
                                        StudentRecordRow(record = record)
                                    }
                                }
                            }
                        }
                    }

                    item(key = "edit-attendance") {
                        AttenoteButton(
                            text = "Edit Attendance",
                            onClick = {
                                onEditAttendance(
                                    summary.classId,
                                    summary.scheduleId,
                                    summary.date.toString()
                                )
                            },
                            enabled = summary.canEditAttendance
                        )
                    }

                    if (!summary.canEditAttendance) {
                        item(key = "edit-disabled") {
                            Text(
                                text = "Edit shortcut unavailable because class or schedule was removed.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        uiState.error?.let { message ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text(message)
            }
        }
    }
}

@Composable
private fun StatLine(
    label: String,
    value: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun StudentRecordRow(
    record: ViewAttendanceStudentRecord
) {
    val statusLabel = when (record.status) {
        AttendanceStatus.PRESENT -> "Present"
        AttendanceStatus.ABSENT -> "Absent"
        AttendanceStatus.SKIPPED -> "Skipped"
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = record.name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "Reg: ${record.registrationNumber}${record.rollNumber.takeIf { it.isNotBlank() }?.let { " | Roll: $it" } ?: ""}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = statusLabel,
            style = MaterialTheme.typography.labelSmall,
            color = when (record.status) {
                AttendanceStatus.PRESENT -> MaterialTheme.colorScheme.primary
                AttendanceStatus.ABSENT -> MaterialTheme.colorScheme.error
                AttendanceStatus.SKIPPED -> MaterialTheme.colorScheme.tertiary
            }
        )
    }
}

private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
