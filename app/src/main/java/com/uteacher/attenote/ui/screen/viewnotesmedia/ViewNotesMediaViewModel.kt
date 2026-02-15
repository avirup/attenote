package com.uteacher.attenote.ui.screen.viewnotesmedia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uteacher.attenote.data.repository.NoteRepository
import com.uteacher.attenote.domain.model.Note
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ViewNotesMediaViewModel(
    private val noteId: Long,
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ViewNotesMediaUiState())
    val uiState: StateFlow<ViewNotesMediaUiState> = _uiState.asStateFlow()

    init {
        observeSelectedNote()
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

    private fun observeSelectedNote() {
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
                noteRepository.observeAllNotes().collectLatest { notes ->
                    val selected = notes.firstOrNull { it.noteId == noteId }
                    if (selected == null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Note not found",
                                viewedNote = null,
                                mediaItems = emptyList(),
                                previewMediaPath = null
                            )
                        }
                        return@collectLatest
                    }

                    val selectedWithMedia = noteRepository.getNoteWithMedia(noteId)
                    val mediaItems = selectedWithMedia
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
                        mediaItems.any { media -> media.filePath == previewPath && media.isAvailable }
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            viewedNote = selected.toUiItem(),
                            mediaItems = mediaItems,
                            previewMediaPath = nextPreviewPath
                        )
                    }
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load notes viewer: ${throwable.message}",
                        viewedNote = null,
                        mediaItems = emptyList(),
                        previewMediaPath = null
                    )
                }
            }
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
            .take(180)
    }

    private companion object {
        private val HTML_TAG_REGEX = Regex("<[^>]+>")
        private val MULTI_SPACE_REGEX = Regex("\\s+")
    }
}
