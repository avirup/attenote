package com.uteacher.attendancetracker.ui.screen.notes

import com.mohamedrejeb.richeditor.model.RichTextState
import com.uteacher.attendancetracker.domain.model.NoteMedia
import java.time.LocalDate

data class AddNoteUiState(
    val noteId: Long = -1L,
    val initialDate: LocalDate? = null,
    val title: String = "",
    val date: LocalDate = LocalDate.now(),
    val richTextState: RichTextState = RichTextState(),
    val pendingMedia: List<PendingMedia> = emptyList(),
    val savedMedia: List<NoteMedia> = emptyList(),
    val showDatePicker: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null,
    val hasUnsavedChanges: Boolean = false
)
