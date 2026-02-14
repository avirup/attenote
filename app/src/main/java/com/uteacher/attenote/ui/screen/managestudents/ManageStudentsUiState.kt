package com.uteacher.attenote.ui.screen.managestudents

import com.uteacher.attenote.domain.model.Student

data class ManageStudentsUiState(
    val allStudents: List<Student> = emptyList(),
    val filteredStudents: List<Student> = emptyList(),
    val searchQuery: String = "",
    val showEditorDialog: Boolean = false,
    val editMode: StudentEditMode = StudentEditMode.ADD,
    val editingStudent: Student? = null,
    val editorName: String = "",
    val editorRegistrationNumber: String = "",
    val editorRollNumber: String = "",
    val editorEmail: String = "",
    val editorPhone: String = "",
    val editorIsActive: Boolean = true,
    val editorNameError: String? = null,
    val editorRegError: String? = null,
    val editorError: String? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null
)

enum class StudentEditMode {
    ADD,
    EDIT
}
