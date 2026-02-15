package com.uteacher.attenote.ui.screen.viewnotesmedia

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.uteacher.attenote.ui.theme.component.AttenoteButton
import com.uteacher.attenote.ui.theme.component.AttenoteSectionCard
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ViewNotesMediaScreen(
    noteId: Long,
    onEditSelectedNote: (date: String, noteId: Long) -> Unit,
    viewModel: ViewNotesMediaViewModel = koinViewModel(parameters = { parametersOf(noteId) })
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

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val selectedNote = uiState.selectedNote

                    if (selectedNote != null) {
                        item(key = "edit-selected-note") {
                            AttenoteButton(
                                text = "Edit Selected Note",
                                onClick = {
                                    onEditSelectedNote(selectedNote.date.toString(), selectedNote.noteId)
                                }
                            )
                        }
                    }

                    if (uiState.groupedNotes.isEmpty()) {
                        item(key = "empty-notes") {
                            Text(
                                text = "No notes available in read-only viewer.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(
                            items = uiState.groupedNotes,
                            key = { group -> "group-${group.date}" }
                        ) { group ->
                            NotesDateGroupCard(
                                group = group,
                                selectedNoteId = uiState.selectedNoteId,
                                onSelectNote = viewModel::onNoteSelected
                            )
                        }
                    }

                    item(key = "media-gallery") {
                        MediaGallerySection(
                            selectedNote = uiState.selectedNote,
                            mediaItems = uiState.mediaItems,
                            onPreviewMedia = viewModel::onPreviewMediaRequested
                        )
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

    uiState.previewMediaPath?.let { previewPath ->
        MediaPreviewDialog(
            filePath = previewPath,
            onDismiss = viewModel::onPreviewDismissed
        )
    }
}

@Composable
private fun NotesDateGroupCard(
    group: ViewNotesMediaDateGroup,
    selectedNoteId: Long,
    onSelectNote: (Long) -> Unit
) {
    AttenoteSectionCard(
        title = group.date.format(DATE_LABEL_FORMAT)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            group.notes.forEach { note ->
                val isSelected = note.noteId == selectedNoteId
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectNote(note.noteId) },
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    },
                    shape = MaterialTheme.shapes.small,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = note.title,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = note.previewText.ifBlank { "No content" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Updated on ${note.updatedAt.format(METADATA_DATE_FORMAT)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaGallerySection(
    selectedNote: ViewNotesMediaNoteItem?,
    mediaItems: List<ViewNotesMediaAttachment>,
    onPreviewMedia: (String) -> Unit
) {
    AttenoteSectionCard(title = "Attached Media") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (selectedNote == null) {
                Text(
                    text = "Select a note to view attachments.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                return@Column
            }

            Text(
                text = "Selected: ${selectedNote.title}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (mediaItems.isEmpty()) {
                Text(
                    text = "No attachments for this note.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                return@Column
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(mediaItems, key = { it.mediaId }) { media ->
                    Surface(
                        modifier = Modifier
                            .size(112.dp)
                            .clickable(enabled = media.isAvailable) {
                                if (media.isAvailable) {
                                    onPreviewMedia(media.filePath)
                                }
                            },
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        if (media.isAvailable) {
                            AsyncImage(
                                model = File(media.filePath),
                                contentDescription = "Attached media",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Missing file",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            if (mediaItems.any { !it.isAvailable }) {
                Text(
                    text = "Some attachments are unavailable on this device.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MediaPreviewDialog(
    filePath: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.92f))
                .padding(16.dp)
        ) {
            AsyncImage(
                model = File(filePath),
                contentDescription = "Media preview",
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                contentScale = ContentScale.Fit
            )

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Text(
                    text = "Close",
                    color = Color.White
                )
            }
        }
    }
}

private val DATE_LABEL_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
private val METADATA_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
