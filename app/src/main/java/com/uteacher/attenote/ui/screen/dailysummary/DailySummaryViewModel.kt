package com.uteacher.attenote.ui.screen.dailysummary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uteacher.attenote.data.repository.AttendanceRepository
import com.uteacher.attenote.data.repository.ClassRepository
import com.uteacher.attenote.data.repository.NoteRepository
import com.uteacher.attenote.data.repository.SettingsPreferencesRepository
import com.uteacher.attenote.data.repository.StudentRepository
import com.uteacher.attenote.domain.model.AttendanceRecord
import com.uteacher.attenote.domain.model.AttendanceSession
import com.uteacher.attenote.domain.model.AttendanceStatus
import com.uteacher.attenote.domain.model.Class
import com.uteacher.attenote.domain.model.Note
import com.uteacher.attenote.domain.model.Student
import java.time.LocalDate
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
    private val studentRepository: StudentRepository,
    private val settingsRepository: SettingsPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailySummaryUiState())
    val uiState: StateFlow<DailySummaryUiState> = _uiState.asStateFlow()

    private var indexedEntries: List<SearchIndexedSummaryEntry> = emptyList()
    private var requestedFilter: DailySummaryContentFilter = DailySummaryContentFilter.BOTH
    private var notesOnlyModeEnabled: Boolean = false

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
                    studentRepository.observeAllStudents(),
                    settingsRepository.notesOnlyModeEnabled
                ) { notes, sessions, classes, students, isNotesOnlyModeEnabled ->
                    SummarySnapshot(
                        notes = notes,
                        sessions = sessions,
                        classes = classes,
                        students = students,
                        isNotesOnlyModeEnabled = isNotesOnlyModeEnabled
                    )
                }.collect { snapshot ->
                    notesOnlyModeEnabled = snapshot.isNotesOnlyModeEnabled
                    indexedEntries = buildSearchIndex(snapshot)
                    emitUiState()
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load summary: ${throwable.message}",
                        dateCards = emptyList()
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(value: String) {
        _uiState.update { it.copy(searchQuery = value.trimStart()) }
        emitUiState()
    }

    fun onFilterSelected(filter: DailySummaryContentFilter) {
        requestedFilter = filter
        emitUiState()
    }

    private fun emitUiState() {
        val currentState = _uiState.value
        val effectiveFilter = resolveEffectiveFilter()
        val filteredEntries = filterAndRank(
            indexedItems = indexedEntries,
            query = currentState.searchQuery,
            filter = effectiveFilter
        )

        _uiState.update {
            it.copy(
                isLoading = false,
                error = null,
                selectedFilter = effectiveFilter,
                isNotesOnlyModeEnabled = notesOnlyModeEnabled,
                dateCards = buildDateCards(filteredEntries)
            )
        }
    }

    private suspend fun buildSearchIndex(snapshot: SummarySnapshot): List<SearchIndexedSummaryEntry> {
        val classNameById = snapshot.classes.associate { it.classId to it.className }
        val studentNameById = snapshot.students.associate { it.studentId to it.name }

        val attendanceIndexed = snapshot.sessions.map { session ->
            val records = attendanceRepository.getRecordsForSession(session.sessionId)
            val attendance = DailySummaryAttendanceEntry(
                sessionId = session.sessionId,
                classId = session.classId,
                scheduleId = session.scheduleId,
                className = classNameById[session.classId] ?: "Unknown class",
                isClassTaken = session.isClassTaken,
                presentCount = records.count { it.status == AttendanceStatus.PRESENT },
                absentCount = records.count { it.status == AttendanceStatus.ABSENT },
                skippedCount = records.count { it.status == AttendanceStatus.SKIPPED }
            )
            val entry = DailySummaryListEntry.Attendance(
                date = session.date,
                value = attendance
            )
            SearchIndexedSummaryEntry(
                entry = entry,
                searchableText = buildAttendanceSearchText(
                    item = attendance,
                    session = session,
                    records = records,
                    studentNameById = studentNameById
                ),
                secondarySortId = attendance.sessionId
            )
        }

        val notesIndexed = snapshot.notes.map { note ->
            val noteEntry = DailySummaryNoteEntry(
                noteId = note.noteId,
                title = note.title.ifBlank { "Untitled note" },
                previewLine = buildPreviewLine(note.content)
            )
            val entry = DailySummaryListEntry.Note(
                date = note.date,
                value = noteEntry
            )
            SearchIndexedSummaryEntry(
                entry = entry,
                searchableText = buildNoteSearchText(note),
                secondarySortId = noteEntry.noteId
            )
        }

        return attendanceIndexed + notesIndexed
    }

    private fun filterAndRank(
        indexedItems: List<SearchIndexedSummaryEntry>,
        query: String,
        filter: DailySummaryContentFilter
    ): List<DailySummaryListEntry> {
        val contentFiltered = indexedItems.filter { entry ->
            when (filter) {
                DailySummaryContentFilter.BOTH -> true
                DailySummaryContentFilter.NOTES_ONLY -> entry.entry is DailySummaryListEntry.Note
                DailySummaryContentFilter.ATTENDANCE_ONLY -> {
                    entry.entry is DailySummaryListEntry.Attendance
                }
            }
        }

        if (query.isBlank()) {
            return contentFiltered
                .sortedWith(baseSortComparator())
                .map { it.entry }
        }

        val normalizedQuery = query.lowercase()
        val queryTokens = tokenize(normalizedQuery)
        return contentFiltered
            .mapNotNull { entry ->
                val score = scoreBestMatch(normalizedQuery, queryTokens, entry.searchableText)
                if (score <= 0) {
                    null
                } else {
                    entry to score
                }
            }
            .sortedWith(
                compareByDescending<Pair<SearchIndexedSummaryEntry, Int>> { it.second }
                    .thenByDescending { it.first.entry.date }
                    .thenByDescending { it.first.secondarySortId }
            )
            .map { it.first.entry }
    }

    private fun baseSortComparator(): Comparator<SearchIndexedSummaryEntry> {
        return compareByDescending<SearchIndexedSummaryEntry> { it.entry.date }
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

    private fun buildDateCards(entries: List<DailySummaryListEntry>): List<DailySummaryDateCard> {
        val grouped = LinkedHashMap<LocalDate, MutableDailySummaryDateCard>()

        entries.forEach { entry ->
            val dayBucket = grouped.getOrPut(entry.date) {
                MutableDailySummaryDateCard(
                    date = entry.date,
                    attendanceEntries = mutableListOf(),
                    noteEntries = mutableListOf()
                )
            }
            when (entry) {
                is DailySummaryListEntry.Attendance -> dayBucket.attendanceEntries += entry.value
                is DailySummaryListEntry.Note -> dayBucket.noteEntries += entry.value
            }
        }

        return grouped.values.map { day ->
            DailySummaryDateCard(
                date = day.date,
                attendanceEntries = day.attendanceEntries,
                noteEntries = day.noteEntries
            )
        }
    }

    private fun resolveEffectiveFilter(): DailySummaryContentFilter {
        return if (notesOnlyModeEnabled) {
            DailySummaryContentFilter.NOTES_ONLY
        } else {
            requestedFilter
        }
    }

    private fun buildAttendanceSearchText(
        item: DailySummaryAttendanceEntry,
        session: AttendanceSession,
        records: List<AttendanceRecord>,
        studentNameById: Map<Long, String>
    ): String {
        val studentNames = records
            .mapNotNull { studentNameById[it.studentId] }
            .joinToString(" ")
        val lessonNotes = sanitizeText(session.lessonNotes.orEmpty())
        val sessionStateText = if (item.isClassTaken) "taken" else "not taken"

        return listOf(
            item.className,
            sessionStateText,
            "present ${item.presentCount}",
            "absent ${item.absentCount}",
            "skipped ${item.skippedCount}",
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

private sealed interface DailySummaryListEntry {
    val date: LocalDate

    data class Attendance(
        override val date: LocalDate,
        val value: DailySummaryAttendanceEntry
    ) : DailySummaryListEntry

    data class Note(
        override val date: LocalDate,
        val value: DailySummaryNoteEntry
    ) : DailySummaryListEntry
}

private data class SearchIndexedSummaryEntry(
    val entry: DailySummaryListEntry,
    val searchableText: String,
    val secondarySortId: Long
)

private data class SummarySnapshot(
    val notes: List<Note>,
    val sessions: List<AttendanceSession>,
    val classes: List<Class>,
    val students: List<Student>,
    val isNotesOnlyModeEnabled: Boolean
)

private data class MutableDailySummaryDateCard(
    val date: LocalDate,
    val attendanceEntries: MutableList<DailySummaryAttendanceEntry>,
    val noteEntries: MutableList<DailySummaryNoteEntry>
)

private fun tokenize(input: String): List<String> {
    return input.split(Regex("\\s+"))
        .map { it.trim() }
        .filter { it.isNotBlank() }
}
