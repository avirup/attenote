package com.uteacher.attendancetracker.ui.screen.manageclass

import com.uteacher.attendancetracker.domain.model.Class

data class ManageClassListUiState(
    val classes: List<Class> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
