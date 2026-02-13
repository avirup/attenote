package com.uteacher.attendancetracker.ui.screen.notes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uteacher.attendancetracker.data.repository.NoteRepository
import com.uteacher.attendancetracker.data.repository.internal.RepositoryResult
import com.uteacher.attendancetracker.domain.model.Note
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddNoteViewModel(
    private val noteId: Long,
    private val dateString: String,
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddNoteUiState(noteId = noteId))
    val uiState: StateFlow<AddNoteUiState> = _uiState.asStateFlow()

    private var baselineTitle: String = ""
    private var baselineHtml: String = ""
    private var baselineDate: LocalDate = LocalDate.now()
    private var baselineCreatedAt: LocalDate = LocalDate.now()

    init {
        loadNote()
    }

    fun onErrorShown() {
        _uiState.update { it.copy(error = null) }
    }

    private fun loadNote() {
        viewModelScope.launch {
            try {
                val initialDate = runCatching { LocalDate.parse(dateString) }
                    .getOrDefault(LocalDate.now())

                if (noteId > 0) {
                    val noteWithMedia = noteRepository.getNoteWithMedia(noteId)
                    if (noteWithMedia == null) {
                        _uiState.update {
                            it.copy(
                                initialDate = initialDate,
                                date = initialDate,
                                isLoading = false,
                                error = "Note not found"
                            )
                        }
                        return@launch
                    }

                    val (note, media) = noteWithMedia
                    val richTextState = _uiState.value.richTextState
                    richTextState.setHtml(note.content)

                    baselineTitle = note.title
                    baselineHtml = note.content
                    baselineDate = note.date
                    baselineCreatedAt = note.createdAt

                    _uiState.update {
                        it.copy(
                            title = note.title,
                            date = note.date,
                            initialDate = initialDate,
                            richTextState = richTextState,
                            savedMedia = media,
                            isLoading = false,
                            hasUnsavedChanges = false,
                            error = null
                        )
                    }
                } else {
                    baselineTitle = ""
                    baselineHtml = ""
                    baselineDate = initialDate
                    baselineCreatedAt = LocalDate.now()

                    _uiState.update {
                        it.copy(
                            initialDate = initialDate,
                            date = initialDate,
                            isLoading = false,
                            hasUnsavedChanges = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load note: ${e.message ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun onTitleChanged(title: String) {
        _uiState.update { state ->
            val html = state.richTextState.toHtml()
            state.copy(
                title = title,
                hasUnsavedChanges = detectChanges(title, html, state.date, state.pendingMedia)
            )
        }
    }

    fun onRichTextChanged() {
        _uiState.update { state ->
            val html = state.richTextState.toHtml()
            state.copy(
                hasUnsavedChanges = detectChanges(state.title, html, state.date, state.pendingMedia)
            )
        }
    }

    fun onDatePickerRequested() {
        _uiState.update { it.copy(showDatePicker = true) }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { state ->
            val html = state.richTextState.toHtml()
            state.copy(
                date = date,
                showDatePicker = false,
                hasUnsavedChanges = detectChanges(state.title, html, date, state.pendingMedia)
            )
        }
    }

    fun onDatePickerDismissed() {
        _uiState.update { it.copy(showDatePicker = false) }
    }

    fun onAddMedia(uri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                val mediaDir = File(context.filesDir, "note_media")
                if (!mediaDir.exists()) {
                    mediaDir.mkdirs()
                }

                val filename = "note_media_${System.currentTimeMillis()}.jpg"
                val target = File(mediaDir, filename)

                val decodedBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                } ?: throw IllegalStateException("Selected file could not be read")

                FileOutputStream(target).use { output ->
                    if (!decodedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)) {
                        throw IllegalStateException("Failed to encode selected image")
                    }
                }
                decodedBitmap.recycle()

                _uiState.update { state ->
                    state.copy(
                        pendingMedia = state.pendingMedia + PendingMedia(uri = uri, localPath = target.absolutePath),
                        hasUnsavedChanges = true,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to add media: ${e.message ?: "Unknown error"}")
                }
            }
        }
    }

    fun onRemovePendingMedia(index: Int) {
        val mediaToRemove = _uiState.value.pendingMedia.getOrNull(index)
        mediaToRemove?.localPath?.let { path ->
            runCatching { File(path).delete() }
        }

        _uiState.update { state ->
            state.copy(
                pendingMedia = state.pendingMedia.filterIndexed { i, _ -> i != index },
                hasUnsavedChanges = detectChanges(
                    title = state.title,
                    html = state.richTextState.toHtml(),
                    date = state.date,
                    pendingMedia = state.pendingMedia.filterIndexed { i, _ -> i != index }
                )
            )
        }
    }

    fun onSaveClicked() {
        saveNote(navigateAfterSave = true)
    }

    fun onAutoSave() {
        val state = _uiState.value
        if (state.isSaving) return
        if (!state.hasUnsavedChanges) return

        val html = state.richTextState.toHtml()
        if (
            state.title.isBlank() &&
            !isMeaningfulHtml(html) &&
            state.pendingMedia.isEmpty() &&
            noteId <= 0
        ) {
            return
        }

        saveNote(navigateAfterSave = false)
    }

    private fun saveNote(navigateAfterSave: Boolean) {
        val state = _uiState.value
        if (state.isSaving) return

        val html = state.richTextState.toHtml()
        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                val note = Note(
                    noteId = if (noteId > 0) noteId else 0L,
                    title = state.title.trim(),
                    content = html,
                    date = state.date,
                    createdAt = baselineCreatedAt,
                    updatedAt = LocalDate.now()
                )

                val mediaPaths = state.pendingMedia.mapNotNull { it.localPath }
                val result = if (noteId > 0) {
                    when (val updateResult = noteRepository.updateNote(note)) {
                        is RepositoryResult.Error -> updateResult
                        is RepositoryResult.Success -> {
                            if (mediaPaths.isEmpty()) {
                                RepositoryResult.Success(Unit)
                            } else {
                                noteRepository.addMediaToNote(noteId, mediaPaths)
                            }
                        }
                    }
                } else {
                    noteRepository.createNote(note, mediaPaths)
                }

                when (result) {
                    is RepositoryResult.Success -> {
                        baselineTitle = state.title.trim()
                        baselineHtml = html
                        baselineDate = state.date
                        if (noteId <= 0) {
                            baselineCreatedAt = LocalDate.now()
                        }

                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                saveSuccess = navigateAfterSave,
                                hasUnsavedChanges = false,
                                pendingMedia = emptyList(),
                                error = null
                            )
                        }
                    }

                    is RepositoryResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                error = result.message
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = "Failed to save note: ${e.message ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    private fun detectChanges(
        title: String,
        html: String,
        date: LocalDate,
        pendingMedia: List<PendingMedia>
    ): Boolean {
        return title.trim() != baselineTitle.trim() ||
            html != baselineHtml ||
            date != baselineDate ||
            pendingMedia.isNotEmpty()
    }

    private fun isMeaningfulHtml(html: String): Boolean {
        val plain = html
            .replace(Regex("<[^>]*>"), " ")
            .replace("&nbsp;", " ")
            .trim()
        return plain.isNotBlank()
    }
}
