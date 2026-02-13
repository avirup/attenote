package com.uteacher.attendancetracker.ui.screen.manageclass

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uteacher.attendancetracker.data.repository.ClassRepository
import com.uteacher.attendancetracker.data.repository.internal.RepositoryResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ManageClassListViewModel(
    private val classRepository: ClassRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageClassListUiState())
    val uiState: StateFlow<ManageClassListUiState> = _uiState.asStateFlow()

    init {
        loadClasses()
    }

    private fun loadClasses() {
        viewModelScope.launch {
            classRepository.observeAllClasses()
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to load classes: ${throwable.message}"
                        )
                    }
                }
                .collect { classes ->
                    _uiState.update {
                        it.copy(
                            classes = classes.sortedByDescending { classItem -> classItem.createdAt },
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    fun onOpenClosedToggled(classId: Long, isOpen: Boolean) {
        viewModelScope.launch {
            when (val result = classRepository.updateClassOpenState(classId, isOpen)) {
                is RepositoryResult.Success -> Unit
                is RepositoryResult.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                }
            }
        }
    }

    fun onErrorShown() {
        _uiState.update { it.copy(error = null) }
    }
}
