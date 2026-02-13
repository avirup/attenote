package com.uteacher.attendancetracker.ui.screen.managestudents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uteacher.attendancetracker.data.repository.StudentRepository
import com.uteacher.attendancetracker.data.repository.internal.RepositoryResult
import com.uteacher.attendancetracker.domain.model.Student
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val InternalWhitespaceRegex = Regex("\\s+")

class ManageStudentsViewModel(
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageStudentsUiState())
    val uiState: StateFlow<ManageStudentsUiState> = _uiState.asStateFlow()

    init {
        loadStudents()
    }

    fun onErrorShown() {
        _uiState.update { it.copy(error = null) }
    }

    private fun loadStudents() {
        viewModelScope.launch {
            try {
                studentRepository.observeAllStudents().collect { students ->
                    val sorted = students.sortedBy { it.name.lowercase() }
                    _uiState.update { state ->
                        state.copy(
                            allStudents = sorted,
                            filteredStudents = filterStudents(sorted, state.searchQuery),
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load students: ${e.message ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    private fun filterStudents(students: List<Student>, query: String): List<Student> {
        if (query.isBlank()) {
            return students
        }
        val normalizedQuery = normalize(query).lowercase()
        return students.filter { student ->
            student.name.lowercase().contains(normalizedQuery) ||
                student.registrationNumber.lowercase().contains(normalizedQuery) ||
                (student.rollNumber?.lowercase()?.contains(normalizedQuery) == true) ||
                (student.email?.lowercase()?.contains(normalizedQuery) == true) ||
                (student.phone?.lowercase()?.contains(normalizedQuery) == true)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredStudents = filterStudents(state.allStudents, query)
            )
        }
    }

    fun onToggleStudentActive(studentId: Long, isActive: Boolean) {
        val previousAllStudents = _uiState.value.allStudents
        val optimisticStudents = previousAllStudents.map { student ->
            if (student.studentId == studentId) {
                student.copy(isActive = isActive)
            } else {
                student
            }
        }

        _uiState.update { state ->
            state.copy(
                allStudents = optimisticStudents,
                filteredStudents = filterStudents(optimisticStudents, state.searchQuery)
            )
        }

        viewModelScope.launch {
            when (val result = studentRepository.updateStudentActiveState(studentId, isActive)) {
                is RepositoryResult.Success -> Unit
                is RepositoryResult.Error -> {
                    _uiState.update { state ->
                        state.copy(
                            allStudents = previousAllStudents,
                            filteredStudents = filterStudents(previousAllStudents, state.searchQuery),
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    fun onShowAddDialog() {
        _uiState.update {
            it.copy(
                showEditorDialog = true,
                editMode = StudentEditMode.ADD,
                editingStudent = null,
                editorName = "",
                editorRegistrationNumber = "",
                editorRollNumber = "",
                editorEmail = "",
                editorPhone = "",
                editorIsActive = true,
                editorNameError = null,
                editorRegError = null,
                editorError = null
            )
        }
    }

    fun onShowEditDialog(student: Student) {
        _uiState.update {
            it.copy(
                showEditorDialog = true,
                editMode = StudentEditMode.EDIT,
                editingStudent = student,
                editorName = student.name,
                editorRegistrationNumber = student.registrationNumber,
                editorRollNumber = student.rollNumber.orEmpty(),
                editorEmail = student.email.orEmpty(),
                editorPhone = student.phone.orEmpty(),
                editorIsActive = student.isActive,
                editorNameError = null,
                editorRegError = null,
                editorError = null
            )
        }
    }

    fun onDismissEditorDialog() {
        _uiState.update {
            it.copy(
                showEditorDialog = false,
                editorNameError = null,
                editorRegError = null,
                editorError = null
            )
        }
    }

    fun onEditorNameChanged(value: String) {
        _uiState.update { it.copy(editorName = value, editorNameError = null, editorError = null) }
    }

    fun onEditorRegChanged(value: String) {
        _uiState.update {
            it.copy(
                editorRegistrationNumber = value,
                editorRegError = null,
                editorError = null
            )
        }
    }

    fun onEditorRollChanged(value: String) {
        _uiState.update { it.copy(editorRollNumber = value, editorError = null) }
    }

    fun onEditorEmailChanged(value: String) {
        _uiState.update { it.copy(editorEmail = value, editorError = null) }
    }

    fun onEditorPhoneChanged(value: String) {
        _uiState.update { it.copy(editorPhone = value, editorError = null) }
    }

    fun onEditorActiveToggled(isActive: Boolean) {
        _uiState.update { it.copy(editorIsActive = isActive) }
    }

    fun onSaveStudent() {
        val state = _uiState.value
        val normalizedName = normalize(state.editorName)
        val normalizedReg = normalize(state.editorRegistrationNumber)

        if (normalizedName.isBlank()) {
            _uiState.update { it.copy(editorNameError = "Name is required") }
            return
        }
        if (normalizedReg.isBlank()) {
            _uiState.update { it.copy(editorRegError = "Registration number is required") }
            return
        }

        when (state.editMode) {
            StudentEditMode.ADD -> {
                val hasIdentityDuplicate = state.allStudents.any { existing ->
                    normalize(existing.name).equals(normalizedName, ignoreCase = true) &&
                        normalize(existing.registrationNumber).equals(
                            normalizedReg,
                            ignoreCase = true
                        )
                }
                if (hasIdentityDuplicate) {
                    _uiState.update {
                        it.copy(
                            editorError = "Student with this name and registration number already exists"
                        )
                    }
                    return
                }

                val hasRegistrationDuplicate = state.allStudents.any { existing ->
                    normalize(existing.registrationNumber).equals(normalizedReg, ignoreCase = true)
                }
                if (hasRegistrationDuplicate) {
                    _uiState.update {
                        it.copy(editorError = "Registration number already exists")
                    }
                    return
                }
            }

            StudentEditMode.EDIT -> {
                val editingStudent = state.editingStudent
                if (editingStudent == null) {
                    _uiState.update {
                        it.copy(editorError = "Could not determine which student to edit")
                    }
                    return
                }
                val hasNameDuplicate = state.allStudents.any { existing ->
                    existing.studentId != editingStudent.studentId &&
                        normalize(existing.name).equals(normalizedName, ignoreCase = true)
                }
                if (hasNameDuplicate) {
                    _uiState.update {
                        it.copy(
                            editorError = "Student with this name and registration number already exists"
                        )
                    }
                    return
                }
            }
        }

        _uiState.update { it.copy(isSaving = true, editorError = null) }
        viewModelScope.launch {
            val currentState = _uiState.value
            val editingStudent = currentState.editingStudent
            val studentToSave = Student(
                studentId = editingStudent?.studentId ?: 0L,
                name = normalize(currentState.editorName),
                registrationNumber = if (currentState.editMode == StudentEditMode.EDIT) {
                    editingStudent?.registrationNumber.orEmpty()
                } else {
                    normalize(currentState.editorRegistrationNumber)
                },
                rollNumber = normalize(currentState.editorRollNumber).ifBlank { null },
                email = normalize(currentState.editorEmail).ifBlank { null },
                phone = normalize(currentState.editorPhone).ifBlank { null },
                isActive = currentState.editorIsActive,
                createdAt = editingStudent?.createdAt ?: LocalDate.now()
            )

            try {
                val result = if (currentState.editMode == StudentEditMode.ADD) {
                    studentRepository.createStudent(studentToSave)
                } else {
                    studentRepository.updateStudent(studentToSave)
                }

                when (result) {
                    is RepositoryResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                showEditorDialog = false,
                                editorNameError = null,
                                editorRegError = null,
                                editorError = null
                            )
                        }
                    }

                    is RepositoryResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                editorError = result.message
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        editorError = "Failed to save student: ${e.message ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    private fun normalize(value: String): String {
        return value.trim().replace(InternalWhitespaceRegex, " ")
    }
}
