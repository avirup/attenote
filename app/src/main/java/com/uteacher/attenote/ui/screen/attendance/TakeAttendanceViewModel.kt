package com.uteacher.attenote.ui.screen.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uteacher.attenote.data.repository.AttendanceRepository
import com.uteacher.attenote.data.repository.AttendanceStatusInput
import com.uteacher.attenote.data.repository.ClassRepository
import com.uteacher.attenote.data.repository.StudentRepository
import com.uteacher.attenote.data.repository.internal.RepositoryResult
import com.uteacher.attenote.domain.model.AttendanceStatus
import java.time.LocalDate
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class TakeAttendanceViewModel(
    private val classId: Long,
    private val scheduleId: Long,
    private val dateString: String,
    private val classRepository: ClassRepository,
    private val studentRepository: StudentRepository,
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        TakeAttendanceUiState(
            classId = classId,
            scheduleId = scheduleId
        )
    )
    val uiState: StateFlow<TakeAttendanceUiState> = _uiState.asStateFlow()
    private var persistedSnapshot: AttendanceDraftSnapshot? = null
    private var lessonNoteAutoSaveJob: Job? = null

    init {
        loadAttendanceContext()
    }

    fun onErrorShown() {
        _uiState.update { it.copy(error = null, shouldNavigateBack = false) }
    }

    private fun loadAttendanceContext() {
        viewModelScope.launch {
            try {
                if (classId <= 0 || scheduleId <= 0) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Invalid attendance route parameters",
                            shouldNavigateBack = true
                        )
                    }
                    return@launch
                }

                val parsedDate = try {
                    LocalDate.parse(dateString)
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Invalid date format in route",
                            shouldNavigateBack = true
                        )
                    }
                    return@launch
                }

                val classWithSchedules = classRepository.getClassWithSchedules(classId)
                if (classWithSchedules == null) {
                    _uiState.update {
                        it.copy(isLoading = false, date = parsedDate, error = "Class not found")
                    }
                    return@launch
                }

                val (classItem, schedules) = classWithSchedules
                val schedule = schedules.find { it.scheduleId == scheduleId }
                if (schedule == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            date = parsedDate,
                            classItem = classItem,
                            error = "Schedule not found",
                            shouldNavigateBack = true
                        )
                    }
                    return@launch
                }

                if (parsedDate < classItem.startDate || parsedDate > classItem.endDate) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            date = parsedDate,
                            classItem = classItem,
                            schedule = schedule,
                            error = "This class is not active on the selected date",
                            shouldNavigateBack = true
                        )
                    }
                    return@launch
                }

                if (parsedDate.dayOfWeek != schedule.dayOfWeek) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            date = parsedDate,
                            classItem = classItem,
                            schedule = schedule,
                            error = "This class is not scheduled for the selected date",
                            shouldNavigateBack = true
                        )
                    }
                    return@launch
                }

                val activeStudents = studentRepository
                    .observeActiveStudentsForClass(classId)
                    .first()

                if (activeStudents.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            date = parsedDate,
                            classItem = classItem,
                            schedule = schedule,
                            error = "No active students in this class"
                        )
                    }
                    return@launch
                }

                val existingSession = attendanceRepository.findSession(classId, scheduleId, parsedDate)
                val existingRecordsByStudentId = if (existingSession != null) {
                    attendanceRepository
                        .getRecordsForSession(existingSession.sessionId)
                        .associateBy { it.studentId }
                } else {
                    emptyMap()
                }
                val isClassTaken = existingSession?.isClassTaken ?: true

                val attendanceRecords = activeStudents
                    .map { student ->
                        val existing = existingRecordsByStudentId[student.studentId]
                        val resolvedStatus = if (isClassTaken) {
                            existing?.status ?: AttendanceStatus.PRESENT
                        } else {
                            AttendanceStatus.SKIPPED
                        }
                        AttendanceRecordItem(
                            student = student,
                            status = resolvedStatus
                        )
                    }
                    .sortedBy { it.student.name.lowercase() }

                val loadedState = _uiState.value.copy(
                    date = parsedDate,
                    classItem = classItem,
                    schedule = schedule,
                    isClassTaken = isClassTaken,
                    attendanceRecords = attendanceRecords,
                    lessonNotes = existingSession?.lessonNotes.orEmpty(),
                    isLoading = false,
                    isSaving = false,
                    isAutoSaving = false,
                    hasPendingChanges = false,
                    error = null,
                    shouldNavigateBack = false
                )
                persistedSnapshot = createSnapshot(loadedState)
                _uiState.value = loadedState
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load attendance: ${e.message ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(value: String) {
        _uiState.update { it.copy(searchQuery = value.trimStart()) }
    }

    fun onClassTakenChanged(isTaken: Boolean) {
        _uiState.update { state ->
            if (state.isSaving || state.isAutoSaving || state.isClassTaken == isTaken) {
                state
            } else {
                val normalizedStatus = if (isTaken) {
                    AttendanceStatus.PRESENT
                } else {
                    AttendanceStatus.SKIPPED
                }
                withPendingChanges(
                    state.copy(
                        isClassTaken = isTaken,
                        attendanceRecords = state.attendanceRecords.map {
                            it.copy(status = normalizedStatus)
                        }
                    )
                )
            }
        }
    }

    fun onToggleStudentPresent(studentId: Long, isPresent: Boolean) {
        _uiState.update { state ->
            if (!state.isClassTaken || state.isSaving || state.isAutoSaving) {
                state
            } else {
                withPendingChanges(
                    state.copy(
                        attendanceRecords = state.attendanceRecords.map { record ->
                            if (record.student.studentId == studentId) {
                                record.copy(
                                    status = if (isPresent) {
                                        AttendanceStatus.PRESENT
                                    } else {
                                        AttendanceStatus.ABSENT
                                    }
                                )
                            } else {
                                record
                            }
                        }
                    )
                )
            }
        }
    }

    fun onLessonNotesChanged(notes: String) {
        _uiState.update { state ->
            withPendingChanges(state.copy(lessonNotes = notes))
        }
        if (_uiState.value.hasPendingChanges) {
            scheduleLessonNoteAutoSave()
        }
    }

    fun onSaveClicked() {
        lessonNoteAutoSaveJob?.cancel()
        saveAttendance(markSuccess = true)
    }

    fun onAutoSaveBackground() {
        lessonNoteAutoSaveJob?.cancel()
        saveAttendance(markSuccess = false)
    }

    fun onAutoSaveExit() {
        lessonNoteAutoSaveJob?.cancel()
        saveAttendance(markSuccess = false)
    }

    override fun onCleared() {
        lessonNoteAutoSaveJob?.cancel()
        val state = _uiState.value
        if (state.hasPendingChanges && canPersistAttendance(state)) {
            runBlocking {
                persistAttendance(state)
            }
        }
        super.onCleared()
    }

    private fun scheduleLessonNoteAutoSave() {
        lessonNoteAutoSaveJob?.cancel()
        lessonNoteAutoSaveJob = viewModelScope.launch {
            delay(LESSON_NOTE_AUTOSAVE_DEBOUNCE_MS)
            saveAttendance(markSuccess = false)
        }
    }

    private fun saveAttendance(markSuccess: Boolean) {
        val state = _uiState.value

        if (
            state.isLoading ||
            state.isSaving ||
            state.isAutoSaving ||
            state.attendanceRecords.isEmpty()
        ) {
            return
        }

        if (!markSuccess && !state.hasPendingChanges) {
            return
        }

        if (!canPersistAttendance(state)) {
            if (markSuccess) {
                _uiState.update { it.copy(error = "Invalid date") }
            }
            return
        }

        val snapshotToPersist = createSnapshot(state)

        _uiState.update {
            if (markSuccess) {
                it.copy(
                    isSaving = true,
                    isAutoSaving = false,
                    saveSuccess = false,
                    error = null
                )
            } else {
                it.copy(isAutoSaving = true)
            }
        }

        viewModelScope.launch {
            try {
                when (
                    val result = persistAttendance(state)
                ) {
                    is RepositoryResult.Success -> {
                        persistedSnapshot = snapshotToPersist
                        _uiState.update {
                            val nextState = it.copy(
                                isSaving = false,
                                isAutoSaving = false,
                                saveSuccess = markSuccess
                            )
                            withPendingChanges(nextState)
                        }
                        if (!markSuccess && _uiState.value.hasPendingChanges) {
                            scheduleLessonNoteAutoSave()
                        }
                    }

                    is RepositoryResult.Error -> {
                        _uiState.update { current ->
                            if (markSuccess) {
                                current.copy(
                                    isSaving = false,
                                    isAutoSaving = false,
                                    error = result.message
                                )
                            } else {
                                current.copy(isAutoSaving = false)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { current ->
                    if (markSuccess) {
                        current.copy(
                            isSaving = false,
                            isAutoSaving = false,
                            error = "Failed to save attendance: ${e.message ?: "Unknown error"}"
                        )
                    } else {
                        current.copy(isAutoSaving = false)
                    }
                }
            }
        }
    }

    private suspend fun persistAttendance(state: TakeAttendanceUiState): RepositoryResult<Long> {
        val date = state.date ?: return RepositoryResult.Error("Invalid date")
        val records = state.attendanceRecords.map { item ->
            AttendanceStatusInput(
                studentId = item.student.studentId,
                status = if (state.isClassTaken) item.status else AttendanceStatus.SKIPPED
            )
        }
        return attendanceRepository.saveAttendance(
            classId = state.classId,
            scheduleId = state.scheduleId,
            date = date,
            isClassTaken = state.isClassTaken,
            lessonNotes = state.lessonNotes.ifBlank { null },
            records = records
        )
    }

    private fun canPersistAttendance(state: TakeAttendanceUiState): Boolean {
        return !state.isLoading &&
            state.date != null &&
            state.attendanceRecords.isNotEmpty()
    }

    private fun withPendingChanges(state: TakeAttendanceUiState): TakeAttendanceUiState {
        val baseline = persistedSnapshot
        val hasPendingChanges = baseline != null && createSnapshot(state) != baseline
        return state.copy(hasPendingChanges = hasPendingChanges)
    }

    private fun createSnapshot(state: TakeAttendanceUiState): AttendanceDraftSnapshot {
        val normalizedRecords = state.attendanceRecords
            .map { record ->
                record.student.studentId to if (state.isClassTaken) {
                    record.status
                } else {
                    AttendanceStatus.SKIPPED
                }
            }
            .sortedBy { it.first }
        return AttendanceDraftSnapshot(
            isClassTaken = state.isClassTaken,
            lessonNotes = state.lessonNotes.trimEnd(),
            normalizedRecords = normalizedRecords
        )
    }

    private data class AttendanceDraftSnapshot(
        val isClassTaken: Boolean,
        val lessonNotes: String,
        val normalizedRecords: List<Pair<Long, AttendanceStatus>>
    )

    private companion object {
        const val LESSON_NOTE_AUTOSAVE_DEBOUNCE_MS = 700L
    }
}
