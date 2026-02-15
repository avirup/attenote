package com.uteacher.attenote.ui.screen.manageclass

import com.uteacher.attenote.domain.model.Class
import com.uteacher.attenote.domain.model.Student
import java.time.LocalDate

data class EditClassUiState(
    val classItem: Class? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val dateRangeError: String? = null,
    val outOfRangeWarning: String? = null,
    val students: List<StudentInClass> = emptyList(),
    val allStudents: List<Student> = emptyList(),
    val showAddStudentDialog: Boolean = false,
    val showCsvImportDialog: Boolean = false,
    val showCopyFromClassDialog: Boolean = false,
    val csvPreviewData: List<CsvStudentRow> = emptyList(),
    val csvImportError: String? = null,
    val availableClasses: List<Class> = emptyList(),
    val showDatePicker: Boolean = false,
    val datePickerTarget: DatePickerTarget? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val operationMessage: String? = null
)

data class StudentInClass(
    val student: Student,
    val isActiveInClass: Boolean
)

data class CsvStudentRow(
    val sourceRowNumber: Int,
    val name: String?,
    val registrationNumber: String?,
    val department: String?,
    val rollNumber: String?,
    val email: String?,
    val phone: String?,
    val matchedExistingStudentId: Long? = null,
    val matchedExistingStudentInactive: Boolean = false,
    val canImport: Boolean = false,
    val selectedForImport: Boolean = false,
    val eligibilityMessage: String? = null
)

enum class DatePickerTarget {
    START_DATE,
    END_DATE
}
