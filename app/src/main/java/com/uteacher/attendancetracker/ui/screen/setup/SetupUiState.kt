package com.uteacher.attendancetracker.ui.screen.setup

data class SetupUiState(
    val name: String = "",
    val institute: String = "",
    val profileImagePath: String? = null,
    val biometricEnabled: Boolean = false,
    val isDeviceSecure: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaveComplete: Boolean = false
)
