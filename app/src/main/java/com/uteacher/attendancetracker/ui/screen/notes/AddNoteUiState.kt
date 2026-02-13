package com.uteacher.attendancetracker.ui.screen.notes

import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.paragraph.type.OrderedListStyleType
import com.mohamedrejeb.richeditor.paragraph.type.UnorderedListStyleType
import com.uteacher.attendancetracker.domain.model.NoteMedia
import java.time.LocalDate

data class AddNoteUiState(
    val noteId: Long = -1L,
    val initialDate: LocalDate? = null,
    val title: String = "",
    val date: LocalDate = LocalDate.now(),
    val richTextState: RichTextState = createRichTextState(),
    val pendingMedia: List<PendingMedia> = emptyList(),
    val savedMedia: List<NoteMedia> = emptyList(),
    val showDatePicker: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val error: String? = null,
    val hasUnsavedChanges: Boolean = false,
    val shouldNavigateBack: Boolean = false
)

private fun createRichTextState(): RichTextState {
    return RichTextState().apply {
        config.orderedListStyleType = OrderedListStyleType.Decimal
        config.unorderedListStyleType = UnorderedListStyleType.Companion.from("•")
        config.preserveStyleOnEmptyLine = true
        config.exitListOnEmptyItem = false
    }
}
