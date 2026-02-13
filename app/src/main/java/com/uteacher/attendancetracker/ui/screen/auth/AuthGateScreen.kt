package com.uteacher.attendancetracker.ui.screen.auth

import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.uteacher.attendancetracker.ui.theme.component.AttenoteButton
import com.uteacher.attendancetracker.util.BiometricHelper
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun AuthGateScreen(
    onNavigateToDashboard: () -> Unit,
    viewModel: AuthGateViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val biometricHelper: BiometricHelper = koinInject()
    val activity = LocalContext.current as FragmentActivity

    LaunchedEffect(uiState.authState) {
        when (uiState.authState) {
            AuthState.IDLE -> {
                showBiometricPrompt(
                    activity = activity,
                    biometricHelper = biometricHelper,
                    onSuccess = viewModel::onAuthSuccess,
                    onFailure = { viewModel.onAuthFailure(it) },
                    onError = { viewModel.onAuthError(it) }
                )
            }

            AuthState.SUCCESS -> onNavigateToDashboard()
            AuthState.AUTHENTICATING, AuthState.FAILURE, AuthState.ERROR -> Unit
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Lock",
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Authentication Required",
                style = MaterialTheme.typography.headlineSmall
            )
            if (uiState.authState == AuthState.FAILURE || uiState.authState == AuthState.ERROR) {
                uiState.errorMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                AttenoteButton(
                    text = "Retry",
                    onClick = viewModel::onRetryClicked
                )
            }
        }
    }
}

private fun showBiometricPrompt(
    activity: FragmentActivity,
    biometricHelper: BiometricHelper,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit,
    onError: (String) -> Unit
) {
    val executor = ContextCompat.getMainExecutor(activity)
    val biometricPrompt = BiometricPrompt(
        activity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFailure("Authentication failed. Try again.")
            }
        }
    )
    biometricPrompt.authenticate(
        biometricHelper.createPromptInfo(
            title = "Unlock attenote",
            description = "Authenticate to access your data"
        )
    )
}
