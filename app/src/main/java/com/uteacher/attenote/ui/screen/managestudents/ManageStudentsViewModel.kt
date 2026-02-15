package com.uteacher.attenote.ui.screen.managestudents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uteacher.attenote.data.repository.StudentRepository
import com.uteacher.attenote.data.repository.internal.RepositoryResult
import com.uteacher.attenote.domain.model.Student
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
                    _uiState.update { state ->
                        val updatedEditingStudent = state.editingStudent?.let { editing ->
                            students.firstOrNull { it.studentId == editing.studentId }
                        }
                        val editorBecameStale =
                            state.showEditorDialog &&
                                state.editMode == StudentEditMode.EDIT &&
                                updatedEditingStudent == null

                        recomputeFilteredState(
                            state = state.copy(
                                allStudents = students,
                                editingStudent = updatedEditingStudent,
                                showEditorDialog = if (editorBecameStale) false else state.showEditorDialog,
                                showDeleteStudentConfirmation = if (editorBecameStale) {
                                    false
                                } else {
                                    state.showDeleteStudentConfirmation
                                },
                                showMergeConfirmation = if (editorBecameStale) {
                                    false
                                } else {
                                    state.showMergeConfirmation
                                },
                                mergeTargetStudent = if (editorBecameStale) {
                                    null
                                } else {
                                    state.mergeTargetStudent
                                },
                                isLoading = false,
                                isSaving = if (editorBecameStale) false else state.isSaving,
                                error = if (editorBecameStale) {
                                    "Student no longer exists"
                                } else {
                                    null
                                }
                            )
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

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            recomputeFilteredState(state.copy(searchQuery = query))
        }
    }

    fun onStatusFilterChanged(filter: StudentStatusFilter) {
        _uiState.update { state ->
            recomputeFilteredState(state.copy(selectedStatusFilter = filter))
        }
    }

    fun onDepartmentFilterChanged(department: String?) {
        _uiState.update { state ->
            recomputeFilteredState(state.copy(selectedDepartmentFilter = department))
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
            recomputeFilteredState(state.copy(allStudents = optimisticStudents))
        }

        viewModelScope.launch {
            when (val result = studentRepository.updateStudentActiveState(studentId, isActive)) {
                is RepositoryResult.Success -> Unit
                is RepositoryResult.Error -> {
                    _uiState.update { state ->
                        recomputeFilteredState(
                            state.copy(
                                allStudents = previousAllStudents,
                                error = result.message
                            )
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
                editorDepartment = "",
                editorRollNumber = "",
                editorEmail = "",
                editorPhone = "",
                editorNameError = null,
                editorRegError = null,
                showDeleteStudentConfirmation = false,
                showMergeConfirmation = false,
                mergeTargetStudent = null,
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
                editorDepartment = student.department,
                editorRollNumber = student.rollNumber.orEmpty(),
                editorEmail = student.email.orEmpty(),
                editorPhone = student.phone.orEmpty(),
                editorNameError = null,
                editorRegError = null,
                showDeleteStudentConfirmation = false,
                showMergeConfirmation = false,
                mergeTargetStudent = null,
                editorError = null
            )
        }
    }

    fun onDismissEditorDialog() {
        _uiState.update {
            it.copy(
                showEditorDialog = false,
                showDeleteStudentConfirmation = false,
                showMergeConfirmation = false,
                mergeTargetStudent = null,
                editorNameError = null,
                editorRegError = null,
                editorError = null,
                isSaving = false
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

    fun onEditorDepartmentChanged(value: String) {
        _uiState.update { it.copy(editorDepartment = value, editorError = null) }
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

    fun onDeleteStudentRequested() {
        val state = _uiState.value
        if (state.editMode != StudentEditMode.EDIT || state.editingStudent == null) {
            _uiState.update { it.copy(editorError = "Student not found") }
            return
        }
        _uiState.update { it.copy(showDeleteStudentConfirmation = true, editorError = null) }
    }

    fun onDismissDeleteStudentConfirmation() {
        _uiState.update { it.copy(showDeleteStudentConfirmation = false) }
    }

    fun onConfirmDeleteStudent() {
        val state = _uiState.value
        val editingStudent = state.editingStudent
        if (state.isSaving || editingStudent == null) {
            return
        }

        _uiState.update {
            it.copy(
                isSaving = true,
                showDeleteStudentConfirmation = false,
                editorError = null
            )
        }

        viewModelScope.launch {
            when (val result = studentRepository.deleteStudentPermanently(editingStudent.studentId)) {
                is RepositoryResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            showEditorDialog = false,
                            editingStudent = null,
                            showDeleteStudentConfirmation = false,
                            showMergeConfirmation = false,
                            mergeTargetStudent = null,
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
        }
    }

    fun onDismissMergeConfirmation() {
        _uiState.update {
            it.copy(
                showMergeConfirmation = false,
                mergeTargetStudent = null
            )
        }
    }

    fun onConfirmMergeStudent() {
        val state = _uiState.value
        val sourceStudent = state.editingStudent
        val targetStudent = state.mergeTargetStudent

        if (state.isSaving || sourceStudent == null || targetStudent == null) {
            return
        }

        _uiState.update {
            it.copy(
                isSaving = true,
                showMergeConfirmation = false,
                editorError = null
            )
        }

        viewModelScope.launch {
            when (
                val result = studentRepository.mergeStudentIntoExisting(
                    sourceStudentId = sourceStudent.studentId,
                    targetStudentId = targetStudent.studentId
                )
            ) {
                is RepositoryResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            showEditorDialog = false,
                            editingStudent = null,
                            mergeTargetStudent = null,
                            showMergeConfirmation = false,
                            showDeleteStudentConfirmation = false,
                            editorError = null
                        )
                    }
                }

                is RepositoryResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            mergeTargetStudent = targetStudent,
                            editorError = result.message
                        )
                    }
                }
            }
        }
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
            }

            StudentEditMode.EDIT -> {
                val editingStudent = state.editingStudent
                if (editingStudent == null) {
                    _uiState.update {
                        it.copy(editorError = "Could not determine which student to edit")
                    }
                    return
                }

                val conflictStudent = state.allStudents.firstOrNull { existing ->
                    existing.studentId != editingStudent.studentId &&
                        normalize(existing.name).equals(normalizedName, ignoreCase = true) &&
                        normalize(existing.registrationNumber).equals(normalizedReg, ignoreCase = true)
                }

                if (conflictStudent != null) {
                    val registrationChanged = normalize(editingStudent.registrationNumber)
                        .equals(normalizedReg, ignoreCase = true)
                        .not()
                    if (registrationChanged) {
                        _uiState.update {
                            it.copy(
                                showMergeConfirmation = true,
                                mergeTargetStudent = conflictStudent,
                                editorError = null
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                editorError = "Student with this name and registration number already exists"
                            )
                        }
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
                registrationNumber = normalize(currentState.editorRegistrationNumber),
                rollNumber = normalize(currentState.editorRollNumber).ifBlank { null },
                email = normalize(currentState.editorEmail).ifBlank { null },
                phone = normalize(currentState.editorPhone).ifBlank { null },
                department = normalize(currentState.editorDepartment).ifBlank { "" },
                isActive = editingStudent?.isActive ?: true,
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
                                showDeleteStudentConfirmation = false,
                                showMergeConfirmation = false,
                                mergeTargetStudent = null,
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

    private fun recomputeFilteredState(state: ManageStudentsUiState): ManageStudentsUiState {
        val availableDepartments = buildDepartmentOptions(state.allStudents)
        val selectedDepartment = state.selectedDepartmentFilter?.let { selected ->
            availableDepartments.firstOrNull { it.equals(selected, ignoreCase = true) }
        }

        return state.copy(
            availableDepartments = availableDepartments,
            selectedDepartmentFilter = selectedDepartment,
            filteredStudents = filterStudents(
                students = state.allStudents,
                query = state.searchQuery,
                departmentFilter = selectedDepartment,
                statusFilter = state.selectedStatusFilter
            )
        )
    }

    private fun filterStudents(
        students: List<Student>,
        query: String,
        departmentFilter: String?,
        statusFilter: StudentStatusFilter
    ): List<Student> {
        val normalizedQuery = normalize(query).lowercase()
        val normalizedDepartmentFilter = departmentFilter?.let(::normalize)?.lowercase()

        return students
            .asSequence()
            .filter { student -> normalize(student.department).isNotBlank() }
            .filter { student ->
                when (statusFilter) {
                    StudentStatusFilter.ALL -> true
                    StudentStatusFilter.ACTIVE -> student.isActive
                    StudentStatusFilter.INACTIVE -> !student.isActive
                }
            }
            .filter { student ->
                if (normalizedDepartmentFilter == null) {
                    true
                } else {
                    normalize(student.department).lowercase() == normalizedDepartmentFilter
                }
            }
            .filter { student ->
                if (normalizedQuery.isBlank()) {
                    true
                } else {
                    student.name.lowercase().contains(normalizedQuery) ||
                        student.registrationNumber.lowercase().contains(normalizedQuery) ||
                        student.rollNumber.orEmpty().lowercase().contains(normalizedQuery) ||
                        student.email.orEmpty().lowercase().contains(normalizedQuery) ||
                        student.phone.orEmpty().lowercase().contains(normalizedQuery) ||
                        student.department.lowercase().contains(normalizedQuery)
                }
            }
            .sortedWith(
                compareByDescending<Student> { it.isActive }
                    .thenBy { it.name.lowercase() }
                    .thenBy { it.registrationNumber.lowercase() }
            )
            .toList()
    }

    private fun buildDepartmentOptions(students: List<Student>): List<String> {
        val distinctDepartments = linkedMapOf<String, String>()
        students.forEach { student ->
            val normalizedDepartment = normalize(student.department)
            if (normalizedDepartment.isBlank()) {
                return@forEach
            }
            val key = normalizedDepartment.lowercase()
            if (key !in distinctDepartments) {
                distinctDepartments[key] = normalizedDepartment
            }
        }

        return distinctDepartments.values.sortedBy { it.lowercase() }
    }

    private fun normalize(value: String): String {
        return value.trim().replace(InternalWhitespaceRegex, " ")
    }
}
