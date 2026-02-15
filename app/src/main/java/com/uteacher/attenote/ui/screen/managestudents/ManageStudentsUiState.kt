package com.uteacher.attenote.ui.screen.managestudents

import com.uteacher.attenote.domain.model.Student

data class ManageStudentsUiState(
    val allStudents: List<Student> = emptyList(),
    val filteredStudents: List<Student> = emptyList(),
    val availableDepartments: List<String> = emptyList(),
    val searchQuery: String = "",
    val selectedDepartmentFilter: String? = null,
    val selectedStatusFilter: StudentStatusFilter = StudentStatusFilter.ALL,
    val showEditorDialog: Boolean = false,
    val editMode: StudentEditMode = StudentEditMode.ADD,
    val editingStudent: Student? = null,
    val editorName: String = "",
    val editorRegistrationNumber: String = "",
    val editorDepartment: String = "",
    val editorRollNumber: String = "",
    val editorEmail: String = "",
    val editorPhone: String = "",
    val editorNameError: String? = null,
    val editorRegError: String? = null,
    val showDeleteStudentConfirmation: Boolean = false,
    val showMergeConfirmation: Boolean = false,
    val mergeTargetStudent: Student? = null,
    val editorError: String? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null
)

enum class StudentEditMode {
    ADD,
    EDIT
}

enum class StudentStatusFilter {
    ALL,
    ACTIVE,
    INACTIVE
}
