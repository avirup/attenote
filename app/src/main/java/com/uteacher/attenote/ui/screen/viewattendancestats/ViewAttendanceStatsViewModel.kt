package com.uteacher.attenote.ui.screen.viewattendancestats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uteacher.attenote.data.repository.AttendanceRepository
import com.uteacher.attenote.data.repository.ClassRepository
import com.uteacher.attenote.data.repository.StudentRepository
import com.uteacher.attenote.domain.model.Schedule
import com.uteacher.attenote.ui.util.computeDurationMinutes
import com.uteacher.attenote.ui.util.formatDurationCompact
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ViewAttendanceStatsViewModel(
    private val sessionId: Long,
    private val attendanceRepository: AttendanceRepository,
    private val classRepository: ClassRepository,
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ViewAttendanceStatsUiState())
    val uiState: StateFlow<ViewAttendanceStatsUiState> = _uiState.asStateFlow()

    init {
        loadSummary()
    }

    fun onErrorShown() {
        _uiState.update { it.copy(error = null) }
    }

    private fun loadSummary() {
        if (sessionId <= 0L) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Invalid attendance viewer route parameters"
                )
            }
            return
        }

        viewModelScope.launch {
            runCatching {
                val session = attendanceRepository.getSessionById(sessionId)
                    ?: run {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Attendance session not found"
                            )
                        }
                        return@launch
                    }

                val classWithSchedules = classRepository.getClassWithSchedules(session.classId)
                val classItem = classWithSchedules?.first
                val schedule = classWithSchedules
                    ?.second
                    ?.firstOrNull { it.scheduleId == session.scheduleId }

                val records = attendanceRepository.getRecordsForSession(sessionId)
                val counters = attendanceRepository.getAttendanceCounters(sessionId)
                val studentsById = studentRepository
                    .observeAllStudents()
                    .first()
                    .associateBy { it.studentId }

                val mappedRecords = records
                    .map { record ->
                        val student = studentsById[record.studentId]
                        ViewAttendanceStudentRecord(
                            studentId = record.studentId,
                            name = student?.name ?: "Unknown student (#${record.studentId})",
                            registrationNumber = student?.registrationNumber ?: "-",
                            rollNumber = student?.rollNumber.orEmpty(),
                            status = record.status
                        )
                    }
                    .sortedBy { it.name.lowercase() }

                val summary = AttendanceSessionSummary(
                    sessionId = session.sessionId,
                    classId = session.classId,
                    scheduleId = session.scheduleId,
                    date = session.date,
                    className = classItem?.className ?: "Deleted class (#${session.classId})",
                    subjectLine = buildSubjectLine(classItem?.subject, classItem?.semester, classItem?.section),
                    scheduleLine = buildScheduleLine(schedule),
                    isClassTaken = session.isClassTaken,
                    presentCount = counters.present,
                    absentCount = counters.absent,
                    skippedCount = counters.skipped,
                    totalCount = counters.total,
                    lessonNotes = session.lessonNotes.orEmpty().trim(),
                    canEditAttendance = classItem != null && schedule != null
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = null,
                        summary = summary,
                        records = mappedRecords
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load attendance summary: ${throwable.message}",
                        summary = null,
                        records = emptyList()
                    )
                }
            }
        }
    }

    private fun buildSubjectLine(subject: String?, semester: String?, section: String?): String {
        val values = listOfNotNull(
            subject?.takeIf { it.isNotBlank() },
            semester?.takeIf { it.isNotBlank() },
            section?.takeIf { it.isNotBlank() }
        )
        return values.joinToString(" | ").ifBlank { "Class details unavailable" }
    }

    private fun buildScheduleLine(schedule: Schedule?): String {
        if (schedule == null) {
            return "Schedule details unavailable"
        }

        val dayLabel = schedule.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
        val durationMinutes = schedule.durationMinutes.takeIf { it > 0 }
            ?: computeDurationMinutes(schedule.startTime, schedule.endTime)
        val durationLabel = if (durationMinutes > 0) {
            " | ${formatDurationCompact(durationMinutes)}"
        } else {
            ""
        }

        return "$dayLabel | ${schedule.startTime.format(TIME_FORMATTER)} - ${schedule.endTime.format(TIME_FORMATTER)}$durationLabel"
    }

    private companion object {
        private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
    }
}
