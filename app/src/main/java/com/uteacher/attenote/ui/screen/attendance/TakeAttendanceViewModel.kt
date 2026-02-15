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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

                val attendanceRecords = activeStudents
                    .map { student ->
                        val existing = existingRecordsByStudentId[student.studentId]
                        AttendanceRecordItem(
                            student = student,
                            isPresent = existing?.isPresent ?: true
                        )
                    }
                    .sortedBy { it.student.name.lowercase() }

                _uiState.update {
                    it.copy(
                        date = parsedDate,
                        classItem = classItem,
                        schedule = schedule,
                        attendanceRecords = attendanceRecords,
                        lessonNotes = existingSession?.lessonNotes.orEmpty(),
                        isLoading = false,
                        error = null,
                        shouldNavigateBack = false
                    )
                }
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

    fun onToggleStudentPresent(studentId: Long, isPresent: Boolean) {
        _uiState.update { state ->
            state.copy(
                attendanceRecords = state.attendanceRecords.map { record ->
                    if (record.student.studentId == studentId) {
                        record.copy(isPresent = isPresent)
                    } else {
                        record
                    }
                }
            )
        }
    }

    fun onLessonNotesChanged(notes: String) {
        _uiState.update { it.copy(lessonNotes = notes) }
    }

    fun onSaveClicked() {
        saveAttendance(markSuccess = true)
    }

    fun onAutoSaveExit() {
        saveAttendance(markSuccess = false)
    }

    private fun saveAttendance(markSuccess: Boolean) {
        val state = _uiState.value
        val date = state.date

        if (state.isLoading || state.isSaving || state.attendanceRecords.isEmpty()) {
            return
        }

        if (date == null) {
            if (markSuccess) {
                _uiState.update { it.copy(error = "Invalid date") }
            }
            return
        }

        _uiState.update {
            if (markSuccess) it.copy(isSaving = true, error = null)
            else it.copy(isSaving = true)
        }

        viewModelScope.launch {
            try {
                val records = state.attendanceRecords.map { item ->
                    AttendanceStatusInput(
                        studentId = item.student.studentId,
                        status = if (item.isPresent) AttendanceStatus.PRESENT else AttendanceStatus.ABSENT
                    )
                }

                when (
                    val result = attendanceRepository.saveAttendance(
                        classId = state.classId,
                        scheduleId = state.scheduleId,
                        date = date,
                        isClassTaken = true,
                        lessonNotes = state.lessonNotes.ifBlank { null },
                        records = records
                    )
                ) {
                    is RepositoryResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                saveSuccess = markSuccess
                            )
                        }
                    }

                    is RepositoryResult.Error -> {
                        _uiState.update { current ->
                            if (markSuccess) {
                                current.copy(
                                    isSaving = false,
                                    error = result.message
                                )
                            } else {
                                current.copy(isSaving = false)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { current ->
                    if (markSuccess) {
                        current.copy(
                            isSaving = false,
                            error = "Failed to save attendance: ${e.message ?: "Unknown error"}"
                        )
                    } else {
                        current.copy(isSaving = false)
                    }
                }
            }
        }
    }
}
