package com.uteacher.attendancetracker.ui.screen.dailysummary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uteacher.attendancetracker.data.repository.AttendanceRepository
import com.uteacher.attendancetracker.data.repository.ClassRepository
import com.uteacher.attendancetracker.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DailySummaryViewModel(
    private val noteRepository: NoteRepository,
    private val attendanceRepository: AttendanceRepository,
    private val classRepository: ClassRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailySummaryUiState())
    val uiState: StateFlow<DailySummaryUiState> = _uiState.asStateFlow()

    init {
        observeSummary()
    }

    private fun observeSummary() {
        viewModelScope.launch {
            runCatching {
                combine(
                    noteRepository.observeAllNotes(),
                    attendanceRepository.observeAllSessions(),
                    classRepository.observeAllClasses()
                ) { notes, sessions, classes ->
                    Triple(notes, sessions, classes)
                }.collect { (notes, sessions, classes) ->
                    val classNameById = classes.associate { it.classId to it.className }

                    val attendanceItems = sessions.map { session ->
                        val records = attendanceRepository.getRecordsForSession(session.sessionId)
                        DailySummaryItem.Attendance(
                            date = session.date,
                            sessionId = session.sessionId,
                            classId = session.classId,
                            scheduleId = session.scheduleId,
                            className = classNameById[session.classId] ?: "Unknown Class",
                            presentCount = records.count { it.isPresent },
                            absentCount = records.count { !it.isPresent }
                        )
                    }

                    val noteItems = notes.map { note ->
                        DailySummaryItem.Note(
                            date = note.date,
                            noteId = note.noteId,
                            title = note.title.ifBlank { "Untitled note" },
                            previewLine = buildPreviewLine(note.content)
                        )
                    }

                    val combined = (attendanceItems + noteItems)
                        .sortedWith(
                            compareByDescending<DailySummaryItem> { it.date }
                                .thenByDescending {
                                    when (it) {
                                        is DailySummaryItem.Note -> it.noteId
                                        is DailySummaryItem.Attendance -> it.sessionId
                                    }
                                }
                        )

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            items = combined
                        )
                    }
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load summary: ${throwable.message}",
                        items = emptyList()
                    )
                }
            }
        }
    }

    private fun buildPreviewLine(content: String): String {
        val plainText = content
            .replace(HTML_TAG_REGEX, " ")
            .replace(MULTI_SPACE_REGEX, " ")
            .trim()
        val preview = plainText
            .lineSequence()
            .map(String::trim)
            .firstOrNull { it.isNotBlank() }
            .orEmpty()
        return preview.take(100)
    }

    private companion object {
        private val HTML_TAG_REGEX = Regex("<[^>]+>")
        private val MULTI_SPACE_REGEX = Regex("\\s+")
    }
}
