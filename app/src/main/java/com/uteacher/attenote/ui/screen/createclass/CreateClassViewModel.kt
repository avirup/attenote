package com.uteacher.attenote.ui.screen.createclass

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uteacher.attenote.data.repository.ClassRepository
import com.uteacher.attenote.data.repository.SessionFormat
import com.uteacher.attenote.data.repository.SettingsPreferencesRepository
import com.uteacher.attenote.data.repository.internal.RepositoryResult
import com.uteacher.attenote.domain.model.Class
import com.uteacher.attenote.domain.model.Schedule
import com.uteacher.attenote.ui.util.computeDurationMinutes
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateClassViewModel(
    private val classRepository: ClassRepository,
    private val settingsRepo: SettingsPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateClassUiState())
    val uiState: StateFlow<CreateClassUiState> = _uiState.asStateFlow()

    init {
        loadInitialFormDefaults()
    }

    private fun loadInitialFormDefaults() {
        viewModelScope.launch {
            val sessionFormat = settingsRepo.sessionFormat.first()
            val instituteFromSettings = settingsRepo.institute.first()
            _uiState.update { state ->
                val normalizedInstitute = instituteFromSettings.trim()
                val nextInstitute = if (state.instituteName.isBlank()) {
                    normalizedInstitute
                } else {
                    state.instituteName
                }
                state.copy(
                    instituteName = nextInstitute,
                    session = if (state.session.isBlank()) {
                        defaultSessionFor(sessionFormat)
                    } else {
                        state.session
                    },
                    className = if (state.classNameManuallyEdited) {
                        state.className
                    } else {
                        generateClassName(state.subject, nextInstitute)
                    }
                )
            }
        }
    }

    private fun defaultSessionFor(format: SessionFormat): String {
        val currentDate = LocalDate.now()
        val currentYear = currentDate.year
        return when (format) {
            SessionFormat.CURRENT_YEAR -> currentYear.toString()
            SessionFormat.ACADEMIC_YEAR -> {
                val cutoffDate = LocalDate.of(currentYear, 6, 30)
                if (currentDate.isAfter(cutoffDate)) {
                    "$currentYear-${currentYear + 1}"
                } else {
                    "${currentYear - 1}-$currentYear"
                }
            }
        }
    }

    fun onInstituteChanged(value: String) {
        _uiState.update { state ->
            state.copy(
                instituteName = value,
                instituteError = null,
                className = if (state.classNameManuallyEdited) {
                    state.className
                } else {
                    generateClassName(state.subject, value)
                }
            )
        }
    }

    fun onSessionChanged(value: String) {
        _uiState.update { it.copy(session = value, sessionError = null) }
    }

    fun onDepartmentChanged(value: String) {
        _uiState.update { it.copy(department = value, departmentError = null) }
    }

    fun onSemesterChanged(value: String) {
        _uiState.update { it.copy(semester = value, semesterError = null) }
    }

    fun onSectionChanged(value: String) {
        _uiState.update { it.copy(section = value) }
    }

    fun onSubjectChanged(value: String) {
        _uiState.update { state ->
            state.copy(
                subject = value,
                subjectError = null,
                className = if (state.classNameManuallyEdited) {
                    state.className
                } else {
                    generateClassName(value, state.instituteName)
                }
            )
        }
    }

    fun onClassNameChanged(value: String) {
        _uiState.update {
            it.copy(
                className = value,
                classNameError = null,
                classNameManuallyEdited = true
            )
        }
    }

    private fun generateClassName(subject: String, institute: String): String {
        return if (subject.isBlank() || institute.isBlank()) {
            ""
        } else {
            "${normalize(subject)} - ${normalize(institute)}"
        }
    }

    fun onStartDatePickerRequested() {
        _uiState.update {
            it.copy(showDatePicker = true, datePickerTarget = DatePickerTarget.START_DATE)
        }
    }

    fun onEndDatePickerRequested() {
        _uiState.update {
            it.copy(showDatePicker = true, datePickerTarget = DatePickerTarget.END_DATE)
        }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { state ->
            when (state.datePickerTarget) {
                DatePickerTarget.START_DATE -> state.copy(
                    startDate = date,
                    showDatePicker = false,
                    datePickerTarget = null,
                    dateRangeError = null
                )

                DatePickerTarget.END_DATE -> state.copy(
                    endDate = date,
                    showDatePicker = false,
                    datePickerTarget = null,
                    dateRangeError = null
                )

                null -> state.copy(showDatePicker = false)
            }
        }
    }

    fun onDatePickerDismissed() {
        _uiState.update { it.copy(showDatePicker = false, datePickerTarget = null) }
    }

    fun onSlotDayChanged(dayOfWeek: java.time.DayOfWeek) {
        _uiState.update {
            it.copy(
                currentSlot = it.currentSlot.copy(
                    dayOfWeek = dayOfWeek,
                    validationError = null
                )
            )
        }
    }

    fun onSlotStartTimePickerRequested() {
        _uiState.update {
            it.copy(showTimePicker = true, timePickerTarget = TimePickerTarget.SLOT_START)
        }
    }

    fun onSlotEndTimePickerRequested() {
        _uiState.update {
            it.copy(showTimePicker = true, timePickerTarget = TimePickerTarget.SLOT_END)
        }
    }

    fun onTimeSelected(time: LocalTime) {
        _uiState.update { state ->
            when (state.timePickerTarget) {
                TimePickerTarget.SLOT_START -> state.copy(
                    currentSlot = state.currentSlot.copy(startTime = time, validationError = null),
                    showTimePicker = false,
                    timePickerTarget = null
                )

                TimePickerTarget.SLOT_END -> state.copy(
                    currentSlot = state.currentSlot.copy(endTime = time, validationError = null),
                    showTimePicker = false,
                    timePickerTarget = null
                )

                null -> state.copy(showTimePicker = false)
            }
        }
    }

    fun onTimePickerDismissed() {
        _uiState.update { it.copy(showTimePicker = false, timePickerTarget = null) }
    }

    fun onAddScheduleSlot() {
        val state = _uiState.value
        val slot = state.currentSlot
        val durationMinutes = computeDurationMinutes(slot.startTime, slot.endTime)

        if (durationMinutes <= 0) {
            _uiState.update {
                it.copy(
                    currentSlot = slot.copy(
                        validationError = "Class duration must be greater than 0 minutes"
                    )
                )
            }
            return
        }

        val overlaps = state.schedules.any { existingSlot ->
            existingSlot.dayOfWeek == slot.dayOfWeek &&
                !(slot.endTime <= existingSlot.startTime || slot.startTime >= existingSlot.endTime)
        }

        if (overlaps) {
            val dayName = slot.dayOfWeek.name.lowercase()
                .replaceFirstChar { char -> char.titlecase() }
            _uiState.update {
                it.copy(
                    currentSlot = slot.copy(
                        validationError = "Schedule overlaps with existing slot on $dayName"
                    )
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                schedules = it.schedules + slot,
                currentSlot = ScheduleSlotDraft(),
                schedulesError = null
            )
        }
    }

    fun onDeleteScheduleSlot(index: Int) {
        _uiState.update { state ->
            state.copy(schedules = state.schedules.filterIndexed { i, _ -> i != index })
        }
    }

    fun onSaveClicked() {
        if (_uiState.value.isLoading) return
        val state = _uiState.value

        val errors = mutableMapOf<String, String?>()

        if (state.instituteName.trim().isEmpty()) errors["institute"] = "Institute name is required"
        if (state.session.trim().isEmpty()) errors["session"] = "Session is required"
        if (state.department.trim().isEmpty()) errors["department"] = "Department is required"
        if (state.semester.trim().isEmpty()) errors["semester"] = "Semester is required"
        if (state.subject.trim().isEmpty()) errors["subject"] = "Subject is required"
        if (state.className.trim().isEmpty()) errors["className"] = "Class name is required"

        if (state.startDate == null || state.endDate == null) {
            errors["dateRange"] = "Start and end dates are required"
        } else if (state.startDate.isAfter(state.endDate)) {
            errors["dateRange"] = "Start date must be before or equal to end date"
        }

        if (state.schedules.isEmpty()) {
            errors["schedules"] = "At least one schedule slot is required"
        }

        if (errors.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    instituteError = errors["institute"],
                    sessionError = errors["session"],
                    departmentError = errors["department"],
                    semesterError = errors["semester"],
                    subjectError = errors["subject"],
                    classNameError = errors["className"],
                    dateRangeError = errors["dateRange"],
                    schedulesError = errors["schedules"]
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, saveError = null) }

        viewModelScope.launch {
            val institute = normalize(state.instituteName)
            val session = normalize(state.session)
            val department = normalize(state.department)
            val semester = normalize(state.semester)
            val section = normalize(state.section)
            val subject = normalize(state.subject)
            val className = normalize(state.className)

            val classToCreate = Class(
                classId = 0L,
                instituteName = institute,
                session = session,
                department = department,
                semester = semester,
                section = section,
                subject = subject,
                className = className,
                startDate = state.startDate!!,
                endDate = state.endDate!!,
                isOpen = true,
                createdAt = LocalDate.now()
            )

            val schedules = state.schedules.map { slot ->
                Schedule(
                    scheduleId = 0L,
                    classId = 0L,
                    dayOfWeek = slot.dayOfWeek,
                    startTime = slot.startTime,
                    endTime = slot.endTime,
                    durationMinutes = computeDurationMinutes(slot.startTime, slot.endTime)
                )
            }

            when (val result = classRepository.createClass(classToCreate, schedules)) {
                is RepositoryResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, saveSuccess = true) }
                }

                is RepositoryResult.Error -> {
                    val message = if (result.message.contains("already exists", ignoreCase = true)) {
                        "Class with these details already exists"
                    } else {
                        result.message
                    }
                    _uiState.update { it.copy(isLoading = false, saveError = message) }
                }
            }
        }
    }

    private fun normalize(value: String): String {
        return value.trim().replace(Regex("\\s+"), " ")
    }
}
