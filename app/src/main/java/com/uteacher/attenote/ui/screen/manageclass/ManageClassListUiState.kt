package com.uteacher.attenote.ui.screen.manageclass

import com.uteacher.attenote.domain.model.Class

data class ManageClassListUiState(
    val classes: List<Class> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
