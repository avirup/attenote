package com.uteacher.attenote.ui.screen.createclass

import java.time.LocalDate

data class CreateClassUiState(
    val instituteName: String = "",
    val session: String = "",
    val department: String = "",
    val semester: String = "",
    val section: String = "",
    val subject: String = "",
    val className: String = "",
    val classNameManuallyEdited: Boolean = false,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val schedules: List<ScheduleSlotDraft> = emptyList(),
    val currentSlot: ScheduleSlotDraft = ScheduleSlotDraft(),
    val instituteError: String? = null,
    val sessionError: String? = null,
    val departmentError: String? = null,
    val semesterError: String? = null,
    val subjectError: String? = null,
    val classNameError: String? = null,
    val dateRangeError: String? = null,
    val schedulesError: String? = null,
    val showDatePicker: Boolean = false,
    val datePickerTarget: DatePickerTarget? = null,
    val showTimePicker: Boolean = false,
    val timePickerTarget: TimePickerTarget? = null,
    val isLoading: Boolean = false,
    val saveSuccess: Boolean = false,
    val saveError: String? = null
)

enum class DatePickerTarget {
    START_DATE,
    END_DATE
}

enum class TimePickerTarget {
    SLOT_START,
    SLOT_END
}
