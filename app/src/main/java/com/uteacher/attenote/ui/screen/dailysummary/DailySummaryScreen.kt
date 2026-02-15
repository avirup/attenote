package com.uteacher.attenote.ui.screen.dailysummary

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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.format.DateTimeFormatter
import org.koin.androidx.compose.koinViewModel

@Composable
fun DailySummaryScreen(
    onOpenAttendanceStats: (sessionId: Long) -> Unit,
    onOpenNotesMedia: (noteId: Long) -> Unit,
    viewModel: DailySummaryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(key = "summary-controls") {
                SummaryControls(
                    searchQuery = uiState.searchQuery,
                    selectedFilter = uiState.selectedFilter,
                    isNotesOnlyModeEnabled = uiState.isNotesOnlyModeEnabled,
                    onSearchQueryChanged = viewModel::onSearchQueryChanged,
                    onFilterSelected = viewModel::onFilterSelected
                )
            }

            if (!uiState.isLoading && uiState.dateCards.isEmpty()) {
                item {
                    Text(
                        text = if (uiState.searchQuery.isBlank()) {
                            "No notes or attendance captured yet."
                        } else {
                            "No matching summary entries."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(
                items = uiState.dateCards,
                key = { "date-card-${it.date}" }
            ) { dateCard ->
                DateSummaryCard(
                    card = dateCard,
                    onOpenAttendanceStats = onOpenAttendanceStats,
                    onOpenNotesMedia = onOpenNotesMedia
                )
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
private fun SummaryControls(
    searchQuery: String,
    selectedFilter: DailySummaryContentFilter,
    isNotesOnlyModeEnabled: Boolean,
    onSearchQueryChanged: (String) -> Unit,
    onFilterSelected: (DailySummaryContentFilter) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Search notes and attendance") }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DailySummaryContentFilter.entries.forEach { option ->
                FilterChip(
                    selected = selectedFilter == option,
                    onClick = { onFilterSelected(option) },
                    label = { Text(option.label) },
                    enabled = !isNotesOnlyModeEnabled || option == DailySummaryContentFilter.NOTES_ONLY
                )
            }
        }

        if (isNotesOnlyModeEnabled) {
            Text(
                text = "Notes Only Mode is enabled. Attendance entries are hidden.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DateSummaryCard(
    card: DailySummaryDateCard,
    onOpenAttendanceStats: (sessionId: Long) -> Unit,
    onOpenNotesMedia: (noteId: Long) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = card.date.format(SECTION_DATE_FORMAT),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            if (card.attendanceEntries.isNotEmpty()) {
                Text(
                    text = "Attendance",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                card.attendanceEntries.forEach { attendance ->
                    AttendanceEntryRow(
                        item = attendance,
                        onOpenAttendanceStats = onOpenAttendanceStats
                    )
                }
            }

            if (card.noteEntries.isNotEmpty()) {
                Text(
                    text = "Notes",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                card.noteEntries.forEach { note ->
                    NoteEntryRow(
                        item = note,
                        onOpenNotesMedia = onOpenNotesMedia
                    )
                }
            }
        }
    }
}

@Composable
private fun AttendanceEntryRow(
    item: DailySummaryAttendanceEntry,
    onOpenAttendanceStats: (sessionId: Long) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenAttendanceStats(item.sessionId) },
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
        shape = MaterialTheme.shapes.small,
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
                    text = if (item.isClassTaken) {
                        "Taken | P ${item.presentCount} | A ${item.absentCount}"
                    } else {
                        "Not Taken | Skipped ${item.skippedCount}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = "Open",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun NoteEntryRow(
    item: DailySummaryNoteEntry,
    onOpenNotesMedia: (noteId: Long) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenNotesMedia(item.noteId) },
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
        shape = MaterialTheme.shapes.small,
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
                text = "Open",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private val SECTION_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
