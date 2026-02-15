package com.uteacher.attenote.ui.screen.viewnotesmedia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uteacher.attenote.data.repository.NoteRepository
import com.uteacher.attenote.domain.model.Note
import java.io.File
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ViewNotesMediaViewModel(
    private val noteId: Long,
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val selectedNoteId = MutableStateFlow(noteId)

    private val _uiState = MutableStateFlow(
        ViewNotesMediaUiState(
            selectedNoteId = noteId
        )
    )
    val uiState: StateFlow<ViewNotesMediaUiState> = _uiState.asStateFlow()

    init {
        observeNotes()
    }

    fun onNoteSelected(noteId: Long) {
        if (noteId <= 0L || noteId == selectedNoteId.value) {
            return
        }
        selectedNoteId.value = noteId
    }

    fun onPreviewMediaRequested(path: String) {
        if (path.isBlank()) {
            return
        }
        _uiState.update { it.copy(previewMediaPath = path) }
    }

    fun onPreviewDismissed() {
        _uiState.update { it.copy(previewMediaPath = null) }
    }

    fun onErrorShown() {
        _uiState.update { it.copy(error = null) }
    }

    private fun observeNotes() {
        if (noteId <= 0L) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Invalid notes viewer route parameters"
                )
            }
            return
        }

        viewModelScope.launch {
            runCatching {
                combine(
                    noteRepository.observeAllNotes(),
                    selectedNoteId
                ) { notes, selectedId ->
                    notes to selectedId
                }.collectLatest { (notes, selectedId) ->
                    val orderedNotes = notes
                        .sortedWith(
                            compareByDescending<Note> { it.date }
                                .thenByDescending { it.updatedAt }
                                .thenByDescending { it.noteId }
                        )

                    if (orderedNotes.isEmpty()) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "No notes available to display",
                                groupedNotes = emptyList(),
                                selectedNote = null,
                                mediaItems = emptyList(),
                                previewMediaPath = null
                            )
                        }
                        return@collectLatest
                    }

                    val resolvedSelected = orderedNotes
                        .firstOrNull { it.noteId == selectedId }
                        ?: orderedNotes.first()

                    val selectedWithMedia = noteRepository.getNoteWithMedia(resolvedSelected.noteId)
                    val selectedMedia = selectedWithMedia
                        ?.second
                        .orEmpty()
                        .map { media ->
                            ViewNotesMediaAttachment(
                                mediaId = media.mediaId,
                                filePath = media.filePath,
                                isAvailable = media.filePath.isNotBlank() && File(media.filePath).exists()
                            )
                        }

                    val nextPreviewPath = _uiState.value.previewMediaPath?.takeIf { previewPath ->
                        selectedMedia.any { media -> media.filePath == previewPath && media.isAvailable }
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            selectedNoteId = resolvedSelected.noteId,
                            groupedNotes = buildDateGroups(orderedNotes),
                            selectedNote = resolvedSelected.toUiItem(),
                            mediaItems = selectedMedia,
                            previewMediaPath = nextPreviewPath
                        )
                    }
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load notes viewer: ${throwable.message}",
                        groupedNotes = emptyList(),
                        selectedNote = null,
                        mediaItems = emptyList(),
                        previewMediaPath = null
                    )
                }
            }
        }
    }

    private fun buildDateGroups(notes: List<Note>): List<ViewNotesMediaDateGroup> {
        val grouped = LinkedHashMap<LocalDate, MutableList<ViewNotesMediaNoteItem>>()
        notes.forEach { note ->
            grouped
                .getOrPut(note.date) { mutableListOf() }
                .add(note.toUiItem())
        }

        return grouped
            .map { (date, groupedNotes) ->
                ViewNotesMediaDateGroup(
                    date = date,
                    notes = groupedNotes
                )
            }
    }

    private fun Note.toUiItem(): ViewNotesMediaNoteItem {
        return ViewNotesMediaNoteItem(
            noteId = noteId,
            date = date,
            title = title.ifBlank { "Untitled note" },
            previewText = buildPreviewLine(content),
            updatedAt = updatedAt
        )
    }

    private fun buildPreviewLine(content: String): String {
        val plainText = content
            .replace(HTML_TAG_REGEX, " ")
            .replace(MULTI_SPACE_REGEX, " ")
            .trim()

        return plainText
            .lineSequence()
            .map(String::trim)
            .firstOrNull { it.isNotBlank() }
            .orEmpty()
            .take(120)
    }

    private companion object {
        private val HTML_TAG_REGEX = Regex("<[^>]+>")
        private val MULTI_SPACE_REGEX = Regex("\\s+")
    }
}
