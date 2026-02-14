package com.uteacher.attenote.ui.screen.auth

enum class AuthState {
    IDLE,
    AUTHENTICATING,
    SUCCESS,
    FAILURE,
    ERROR
}

data class AuthGateUiState(
    val authState: AuthState = AuthState.IDLE,
    val errorMessage: String? = null
)
