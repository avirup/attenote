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
    private val undoHistory = ArrayDeque<String>()
    private val redoHistory = ArrayDeque<String>()
    private var lastRecordedHtml: String = ""

    init {
        lastRecordedHtml = _uiState.value.richTextState.toHtml()
        loadNote()
    }

    fun onErrorShown() {
        _uiState.update { it.copy(error = null, shouldNavigateBack = false) }
    }

    private fun loadNote() {
        viewModelScope.launch {
            try {
                if (noteId < -1L) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Invalid note route parameters",
                            shouldNavigateBack = true
                        )
                    }
                    return@launch
                }

                val initialDate = runCatching { LocalDate.parse(dateString) }
                    .getOrElse {
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                error = "Invalid date format in route",
                                shouldNavigateBack = true
                            )
                        }
                        return@launch
                    }

                if (noteId > 0) {
                    val noteWithMedia = noteRepository.getNoteWithMedia(noteId)
                    if (noteWithMedia == null) {
                        _uiState.update {
                            it.copy(
                                initialDate = initialDate,
                                date = initialDate,
                                isLoading = false,
                                error = "Note not found",
                                shouldNavigateBack = true
                            )
                        }
                        return@launch
                    }

                    val (note, media) = noteWithMedia
                    val richTextState = _uiState.value.richTextState
                    richTextState.setHtml(note.content)
                    resetHistory(note.content)

                    baselineTitle = note.title
                    baselineHtml = note.content.normalizeHtml()
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
                            canUndo = false,
                            canRedo = false,
                            hasUnsavedChanges = false,
                            error = null,
                            shouldNavigateBack = false
                        )
                    }
                } else {
                    baselineTitle = ""
                    baselineHtml = "".normalizeHtml()
                    baselineDate = initialDate
                    baselineCreatedAt = LocalDate.now()
                    resetHistory(_uiState.value.richTextState.toHtml())

                    _uiState.update {
                        it.copy(
                            initialDate = initialDate,
                            date = initialDate,
                            isLoading = false,
                            canUndo = false,
                            canRedo = false,
                            hasUnsavedChanges = false,
                            error = null,
                            shouldNavigateBack = false
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
            if (html != lastRecordedHtml) {
                pushUndoState(lastRecordedHtml)
                redoHistory.clear()
                lastRecordedHtml = html
            }
            state.copy(
                hasUnsavedChanges = detectChanges(state.title, html, state.date, state.pendingMedia),
                canUndo = undoHistory.isNotEmpty(),
                canRedo = redoHistory.isNotEmpty()
            )
        }
    }

    fun onUndoClicked() {
        val state = _uiState.value
        if (undoHistory.isEmpty()) return

        val currentHtml = state.richTextState.toHtml()
        val previousHtml = undoHistory.removeLast()
        pushRedoState(currentHtml)
        state.richTextState.setHtml(previousHtml)
        lastRecordedHtml = previousHtml

        _uiState.update {
            it.copy(
                hasUnsavedChanges = detectChanges(
                    title = it.title,
                    html = previousHtml,
                    date = it.date,
                    pendingMedia = it.pendingMedia
                ),
                canUndo = undoHistory.isNotEmpty(),
                canRedo = redoHistory.isNotEmpty()
            )
        }
    }

    fun onRedoClicked() {
        val state = _uiState.value
        if (redoHistory.isEmpty()) return

        val currentHtml = state.richTextState.toHtml()
        val nextHtml = redoHistory.removeLast()
        pushUndoState(currentHtml)
        state.richTextState.setHtml(nextHtml)
        lastRecordedHtml = nextHtml

        _uiState.update {
            it.copy(
                hasUnsavedChanges = detectChanges(
                    title = it.title,
                    html = nextHtml,
                    date = it.date,
                    pendingMedia = it.pendingMedia
                ),
                canUndo = undoHistory.isNotEmpty(),
                canRedo = redoHistory.isNotEmpty()
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
                        baselineHtml = html.normalizeHtml()
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
            html.normalizeHtml() != baselineHtml.normalizeHtml() ||
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

    private fun String.normalizeHtml(): String {
        return this
            .replace("<p></p>", "")
            .replace("<p><br></p>", "")
            .replace("&nbsp;", " ")
            .trim()
    }

    private fun resetHistory(initialHtml: String) {
        undoHistory.clear()
        redoHistory.clear()
        lastRecordedHtml = initialHtml
    }

    private fun pushUndoState(html: String) {
        if (undoHistory.lastOrNull() == html) return
        undoHistory.addLast(html)
        if (undoHistory.size > MAX_HISTORY_ENTRIES) {
            undoHistory.removeFirst()
        }
    }

    private fun pushRedoState(html: String) {
        if (redoHistory.lastOrNull() == html) return
        redoHistory.addLast(html)
        if (redoHistory.size > MAX_HISTORY_ENTRIES) {
            redoHistory.removeFirst()
        }
    }

    private companion object {
        const val MAX_HISTORY_ENTRIES = 100
    }
}
