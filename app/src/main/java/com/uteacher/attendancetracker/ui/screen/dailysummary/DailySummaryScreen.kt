package com.uteacher.attendancetracker.ui.screen.dailysummary

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.koin.androidx.compose.koinViewModel

@Composable
fun DailySummaryScreen(
    onEditAttendance: (classId: Long, scheduleId: Long, date: String) -> Unit,
    onEditNote: (date: String, noteId: Long) -> Unit,
    viewModel: DailySummaryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val groupedByDate = remember(uiState.items) { groupByDate(uiState.items) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (!uiState.isLoading && groupedByDate.isEmpty()) {
                item {
                    Text(
                        text = "No notes or attendance captured yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            groupedByDate.forEach { (date, entries) ->
                item(key = "date-$date") {
                    Text(
                        text = date.format(SECTION_DATE_FORMAT),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }

                items(
                    items = entries,
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
                    .padding(12.dp)
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = item.className,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "P ${item.presentCount}  A ${item.absentCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Text(
                text = "Edit",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    onEditAttendance(item.classId, item.scheduleId, item.date.toString())
                }
            )
        }
    }
}

@Composable
private fun NoteSummaryItem(
    item: DailySummaryItem.Note,
    onEditNote: (date: String, noteId: Long) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.previewLine.ifBlank { "No content" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = "Edit",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    onEditNote(item.date.toString(), item.noteId)
                }
            )
        }
    }
}

private fun groupByDate(items: List<DailySummaryItem>): List<Pair<LocalDate, List<DailySummaryItem>>> {
    val grouped = LinkedHashMap<LocalDate, MutableList<DailySummaryItem>>()
    items.forEach { item ->
        grouped.getOrPut(item.date) { mutableListOf() }.add(item)
    }
    return grouped.map { it.key to it.value }
}

private val SECTION_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
