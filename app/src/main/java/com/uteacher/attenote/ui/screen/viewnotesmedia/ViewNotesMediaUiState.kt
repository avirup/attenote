package com.uteacher.attenote.ui.screen.viewnotesmedia

import java.time.LocalDate

data class ViewNotesMediaUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val selectedNoteId: Long = 0L,
    val groupedNotes: List<ViewNotesMediaDateGroup> = emptyList(),
    val selectedNote: ViewNotesMediaNoteItem? = null,
    val mediaItems: List<ViewNotesMediaAttachment> = emptyList(),
    val previewMediaPath: String? = null
)

data class ViewNotesMediaDateGroup(
    val date: LocalDate,
    val notes: List<ViewNotesMediaNoteItem>
)

data class ViewNotesMediaNoteItem(
    val noteId: Long,
    val date: LocalDate,
    val title: String,
    val previewText: String,
    val updatedAt: LocalDate
)

data class ViewNotesMediaAttachment(
    val mediaId: Long,
    val filePath: String,
    val isAvailable: Boolean
)
