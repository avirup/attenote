package com.uteacher.attendancetracker.ui.screen.dailysummary

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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.uteacher.attendancetracker.ui.theme.component.AttenoteSectionCard
import java.time.format.DateTimeFormatter
import org.koin.androidx.compose.koinViewModel

@Composable
fun DailySummaryScreen(
    onEditAttendance: (classId: Long, scheduleId: Long, date: String) -> Unit,
    onEditNote: (date: String, noteId: Long) -> Unit,
    viewModel: DailySummaryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "Notes and attendance",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (!uiState.isLoading && uiState.items.isEmpty()) {
                item {
                    AttenoteSectionCard {
                        Text(
                            text = "No notes or attendance captured yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(
                    items = uiState.items,
                    key = { item ->
                        when (item) {
                            is DailySummaryItem.Attendance -> "attendance-${item.sessionId}"
                            is DailySummaryItem.Note -> "note-${item.noteId}"
                        }
                    }
                ) { item ->
                    when (item) {
                        is DailySummaryItem.Attendance -> AttendanceSummaryItem(
                            item = item,
                            onEditAttendance = onEditAttendance
                        )

                        is DailySummaryItem.Note -> NoteSummaryItem(
                            item = item,
                            onEditNote = onEditNote
                        )
                    }
                }
            }
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
                    .padding(16.dp)
            ) {
                Text(text = message)
            }
        }
    }
}

@Composable
private fun AttendanceSummaryItem(
    item: DailySummaryItem.Attendance,
    onEditAttendance: (classId: Long, scheduleId: Long, date: String) -> Unit
) {
    AttenoteSectionCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = item.className,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = "Attendance · ${item.date.format(SUMMARY_DATE_FORMAT)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Present: ${item.presentCount}  Absent: ${item.absentCount}",
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        onEditAttendance(item.classId, item.scheduleId, item.date.toString())
                    }
                ) {
                    Text(text = "Edit")
                }
            }
        }
    }
}

@Composable
private fun NoteSummaryItem(
    item: DailySummaryItem.Note,
    onEditNote: (date: String, noteId: Long) -> Unit
) {
    AttenoteSectionCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Note · ${item.date.format(SUMMARY_DATE_FORMAT)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = item.previewLine.ifBlank { "No content" },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { onEditNote(item.date.toString(), item.noteId) }
                ) {
                    Text(text = "Edit")
                }
            }
        }
    }
}

private val SUMMARY_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
