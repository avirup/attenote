package com.uteacher.attenote.ui.screen.dailysummary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uteacher.attenote.data.repository.AttendanceRepository
import com.uteacher.attenote.data.repository.ClassRepository
import com.uteacher.attenote.data.repository.NoteRepository
import com.uteacher.attenote.data.repository.StudentRepository
import com.uteacher.attenote.domain.model.AttendanceRecord
import com.uteacher.attenote.domain.model.AttendanceSession
import com.uteacher.attenote.domain.model.Class
import com.uteacher.attenote.domain.model.Note
import com.uteacher.attenote.domain.model.Student
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DailySummaryViewModel(
    private val noteRepository: NoteRepository,
    private val attendanceRepository: AttendanceRepository,
    private val classRepository: ClassRepository,
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailySummaryUiState())
    val uiState: StateFlow<DailySummaryUiState> = _uiState.asStateFlow()
    private var searchIndexedItems: List<SearchIndexedItem> = emptyList()

    init {
        observeSummary()
    }

    private fun observeSummary() {
        viewModelScope.launch {
            runCatching {
                combine(
                    noteRepository.observeAllNotes(),
                    attendanceRepository.observeAllSessions(),
                    classRepository.observeAllClasses(),
                    studentRepository.observeAllStudents()
                ) { notes, sessions, classes, students ->
                    SummarySnapshot(
                        notes = notes,
                        sessions = sessions,
                        classes = classes,
                        students = students
                    )
                }.collect { snapshot ->
                    searchIndexedItems = buildSearchIndex(snapshot)
                    val query = _uiState.value.searchQuery
                    val filtered = filterAndRank(searchIndexedItems, query)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            items = filtered
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

    fun onSearchQueryChanged(value: String) {
        val query = value.trimStart()
        _uiState.update { it.copy(searchQuery = query) }
        val filtered = filterAndRank(searchIndexedItems, query)
        _uiState.update { it.copy(items = filtered) }
    }

    private suspend fun buildSearchIndex(snapshot: SummarySnapshot): List<SearchIndexedItem> {
        val classNameById = snapshot.classes.associate { it.classId to it.className }
        val studentNameById = snapshot.students.associate { it.studentId to it.name }

        val attendanceIndexed = snapshot.sessions.map { session ->
            val records = attendanceRepository.getRecordsForSession(session.sessionId)
            val item = DailySummaryItem.Attendance(
                date = session.date,
                sessionId = session.sessionId,
                classId = session.classId,
                scheduleId = session.scheduleId,
                className = classNameById[session.classId] ?: "Unknown Class",
                presentCount = records.count { it.isPresent },
                absentCount = records.count { !it.isPresent }
            )
            SearchIndexedItem(
                item = item,
                searchableText = buildAttendanceSearchText(item, session, records, studentNameById),
                secondarySortId = item.sessionId
            )
        }

        val notesIndexed = snapshot.notes.map { note ->
            val item = DailySummaryItem.Note(
                date = note.date,
                noteId = note.noteId,
                title = note.title.ifBlank { "Untitled note" },
                previewLine = buildPreviewLine(note.content)
            )
            SearchIndexedItem(
                item = item,
                searchableText = buildNoteSearchText(note),
                secondarySortId = item.noteId
            )
        }

        return (attendanceIndexed + notesIndexed)
    }

    private fun filterAndRank(
        indexedItems: List<SearchIndexedItem>,
        query: String
    ): List<DailySummaryItem> {
        if (query.isBlank()) {
            return indexedItems
                .sortedWith(baseSortComparator())
                .map { it.item }
        }

        val normalizedQuery = query.lowercase()
        val queryTokens = tokenize(normalizedQuery)
        return indexedItems
            .mapNotNull { entry ->
                val score = scoreBestMatch(normalizedQuery, queryTokens, entry.searchableText)
                if (score <= 0) null else entry to score
            }
            .sortedWith(
                compareByDescending<Pair<SearchIndexedItem, Int>> { it.second }
                    .thenByDescending { it.first.item.date }
                    .thenByDescending { it.first.secondarySortId }
            )
            .map { it.first.item }
    }

    private fun baseSortComparator(): Comparator<SearchIndexedItem> {
        return compareByDescending<SearchIndexedItem> { it.item.date }
            .thenByDescending { it.secondarySortId }
    }

    private fun scoreBestMatch(
        normalizedQuery: String,
        queryTokens: List<String>,
        searchableText: String
    ): Int {
        if (normalizedQuery.isBlank()) return 0
        var score = 0
        if (searchableText.contains(normalizedQuery)) {
            score += 140
        }
        queryTokens.forEach { token ->
            if (token.isBlank()) return@forEach
            if (searchableText.contains(token)) {
                score += 45
            }
            if (searchableText.contains(" $token")) {
                score += 12
            }
        }
        return score
    }

    private fun buildAttendanceSearchText(
        item: DailySummaryItem.Attendance,
        session: AttendanceSession,
        records: List<AttendanceRecord>,
        studentNameById: Map<Long, String>
    ): String {
        val studentNames = records
            .mapNotNull { studentNameById[it.studentId] }
            .joinToString(" ")
        val lessonNotes = sanitizeText(session.lessonNotes.orEmpty())
        return listOf(
            item.className,
            "present ${item.presentCount}",
            "absent ${item.absentCount}",
            lessonNotes,
            studentNames,
            session.date.toString()
        ).joinToString(" ").lowercase()
    }

    private fun buildNoteSearchText(note: Note): String {
        return listOf(
            note.title,
            sanitizeText(note.content),
            note.date.toString()
        ).joinToString(" ").lowercase()
    }

    private fun sanitizeText(value: String): String {
        return value
            .replace(HTML_TAG_REGEX, " ")
            .replace(MULTI_SPACE_REGEX, " ")
            .trim()
    }

    private fun buildPreviewLine(content: String): String {
        val plainText = sanitizeText(content)
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

private data class SearchIndexedItem(
    val item: DailySummaryItem,
    val searchableText: String,
    val secondarySortId: Long
)

private data class SummarySnapshot(
    val notes: List<Note>,
    val sessions: List<AttendanceSession>,
    val classes: List<Class>,
    val students: List<Student>
)

private fun tokenize(input: String): List<String> {
    return input.split(Regex("\\s+"))
        .map { it.trim() }
        .filter { it.isNotBlank() }
}
