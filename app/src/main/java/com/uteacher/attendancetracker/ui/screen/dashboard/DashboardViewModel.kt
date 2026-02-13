package com.uteacher.attendancetracker.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uteacher.attendancetracker.data.repository.ClassRepository
import com.uteacher.attendancetracker.data.repository.NoteRepository
import com.uteacher.attendancetracker.data.repository.SettingsPreferencesRepository
import com.uteacher.attendancetracker.domain.model.Class
import com.uteacher.attendancetracker.domain.model.FabPosition
import com.uteacher.attendancetracker.domain.model.Note
import com.uteacher.attendancetracker.domain.model.Schedule
import com.uteacher.attendancetracker.domain.model.ScheduledClassItem
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val classRepository: ClassRepository,
    private val noteRepository: NoteRepository,
    private val settingsRepo: SettingsPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var cachedClassesWithSchedules: List<Pair<Class, List<Schedule>>> = emptyList()
    private var cachedNotes: List<Note> = emptyList()

    init {
        loadDashboardData()
        loadFabPosition()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            runCatching {
                combine(
                    classRepository.observeClasses(isOpen = true),
                    noteRepository.observeAllNotes()
                ) { classes, allNotes ->
                    classes to allNotes
                }.collect { (classes, allNotes) ->
                    cachedClassesWithSchedules = classes.mapNotNull { classItem ->
                        classRepository.getClassWithSchedules(classItem.classId)?.let { relation ->
                            relation.first to relation.second
                        }
                    }
                    cachedNotes = allNotes.sortedByDescending { it.updatedAt }
                    refreshContentForSelectedDate()
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load dashboard: ${throwable.message}"
                    )
                }
            }
        }
    }

    private fun loadFabPosition() {
        viewModelScope.launch {
            settingsRepo.fabPosition.collect { position ->
                _uiState.update { it.copy(fabPosition = position) }
            }
        }
    }

    private fun refreshContentForSelectedDate() {
        val selectedDate = _uiState.value.selectedDate
        val datesWithClasses = calculateDatesWithClasses(cachedClassesWithSchedules)
        val datesWithNotes = cachedNotes.map { it.date }.toSet()

        _uiState.update {
            it.copy(
                scheduledClasses = expandSchedulesForDate(cachedClassesWithSchedules, selectedDate),
                notes = cachedNotes
                    .filter { note -> note.date == selectedDate }
                    .sortedByDescending { note -> note.updatedAt },
                datesWithClasses = datesWithClasses,
                datesWithNotes = datesWithNotes,
                isLoading = false,
                error = null
            )
        }
    }

    private fun expandSchedulesForDate(
        classesWithSchedules: List<Pair<Class, List<Schedule>>>,
        targetDate: LocalDate
    ): List<ScheduledClassItem> {
        return classesWithSchedules.flatMap { (classItem, schedules) ->
            if (targetDate < classItem.startDate || targetDate > classItem.endDate) {
                return@flatMap emptyList()
            }

            schedules
                .asSequence()
                .filter { schedule -> schedule.dayOfWeek == targetDate.dayOfWeek }
                .map { schedule ->
                    ScheduledClassItem(
                        classId = classItem.classId,
                        className = classItem.className,
                        subject = classItem.subject,
                        scheduleId = schedule.scheduleId,
                        dayOfWeek = schedule.dayOfWeek,
                        startTime = schedule.startTime,
                        endTime = schedule.endTime,
                        instituteName = classItem.instituteName,
                        session = classItem.session,
                        department = classItem.department,
                        semester = classItem.semester,
                        section = classItem.section
                    )
                }
                .toList()
        }.sortedBy { it.startTime }
    }

    private fun calculateDatesWithClasses(
        classesWithSchedules: List<Pair<Class, List<Schedule>>>
    ): Set<LocalDate> {
        return classesWithSchedules.flatMap { (classItem, schedules) ->
            if (schedules.isEmpty()) {
                return@flatMap emptyList()
            }

            val rangeDates = mutableListOf<LocalDate>()
            var currentDate = classItem.startDate
            while (!currentDate.isAfter(classItem.endDate)) {
                if (schedules.any { schedule -> schedule.dayOfWeek == currentDate.dayOfWeek }) {
                    rangeDates.add(currentDate)
                }
                currentDate = currentDate.plusDays(1)
            }
            rangeDates
        }.toSet()
    }

    fun getWeekRange(date: LocalDate): List<LocalDate> {
        val dayOfWeekIndex = date.dayOfWeek.value
        val startOfWeek = date.minusDays((dayOfWeekIndex - 1).toLong())
        return (0..6).map { offset -> startOfWeek.plusDays(offset.toLong()) }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update {
            it.copy(
                selectedDate = date,
                currentMonth = java.time.YearMonth.from(date),
                calendarExpanded = false,
                fabMenuExpanded = false
            )
        }
        refreshContentForSelectedDate()
    }

    fun onPreviousDayClicked() {
        val newDate = _uiState.value.selectedDate.minusDays(1)
        _uiState.update {
            it.copy(
                selectedDate = newDate,
                currentMonth = java.time.YearMonth.from(newDate)
            )
        }
        refreshContentForSelectedDate()
    }

    fun onNextDayClicked() {
        val newDate = _uiState.value.selectedDate.plusDays(1)
        _uiState.update {
            it.copy(
                selectedDate = newDate,
                currentMonth = java.time.YearMonth.from(newDate)
            )
        }
        refreshContentForSelectedDate()
    }

    fun onPreviousMonthClicked() {
        _uiState.update { it.copy(currentMonth = it.currentMonth.minusMonths(1)) }
    }

    fun onNextMonthClicked() {
        _uiState.update { it.copy(currentMonth = it.currentMonth.plusMonths(1)) }
    }

    fun onToggleCalendar() {
        setCalendarExpanded(!_uiState.value.calendarExpanded)
    }

    fun setCalendarExpanded(expanded: Boolean) {
        _uiState.update { current ->
            if (current.calendarExpanded == expanded) {
                current
            } else {
                current.copy(calendarExpanded = expanded)
            }
        }
    }

    fun onToggleFabMenu() {
        _uiState.update { it.copy(fabMenuExpanded = !it.fabMenuExpanded) }
    }

    fun onDismissFabMenu() {
        _uiState.update { it.copy(fabMenuExpanded = false) }
    }

    fun onContentScrolled() {
        if (_uiState.value.fabMenuExpanded) {
            _uiState.update { it.copy(fabMenuExpanded = false) }
        }
    }

    fun onFabSwipedLeft() {
        if (_uiState.value.fabPosition != FabPosition.LEFT) {
            persistFabPosition(FabPosition.LEFT)
        }
    }

    fun onFabSwipedRight() {
        if (_uiState.value.fabPosition != FabPosition.RIGHT) {
            persistFabPosition(FabPosition.RIGHT)
        }
    }

    private fun persistFabPosition(position: FabPosition) {
        viewModelScope.launch {
            settingsRepo.setFabPosition(position)
        }
    }
}
