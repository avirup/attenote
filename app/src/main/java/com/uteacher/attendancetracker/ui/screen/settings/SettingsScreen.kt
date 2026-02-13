package com.uteacher.attendancetracker.ui.screen.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.uteacher.attendancetracker.data.repository.SessionFormat
import com.uteacher.attendancetracker.domain.model.FabPosition
import com.uteacher.attendancetracker.ui.navigation.ActionBarPrimaryAction
import com.uteacher.attendancetracker.ui.theme.component.AttenoteButton
import com.uteacher.attendancetracker.ui.theme.component.AttenoteSectionCard
import com.uteacher.attendancetracker.ui.theme.component.AttenoteTextField
import java.io.File
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    onSetActionBarPrimaryAction: (ActionBarPrimaryAction?) -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val onSaveClick = remember(viewModel) { { viewModel.onSaveProfileClicked() } }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let(viewModel::onProfileImageSelected)
        if (uri == null) {
            viewModel.onImagePickerDismissed()
        }
    }

    val importPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let(viewModel::onImportFileSelected)
    }

    LaunchedEffect(uiState.showImagePicker) {
        if (uiState.showImagePicker) {
            imagePickerLauncher.launch("image/*")
        }
    }

    LaunchedEffect(uiState.importSuccess) {
        if (uiState.importSuccess) {
            delay(1500)
            viewModel.onImportSuccessAcknowledged()
            restartApp(context)
        }
    }

    LaunchedEffect(uiState.exportError, uiState.importError, uiState.profileSaveError) {
        if (uiState.exportError != null || uiState.importError != null || uiState.profileSaveError != null) {
            delay(3000)
            viewModel.onErrorDismissed()
        }
    }

    SideEffect {
        onSetActionBarPrimaryAction(
            ActionBarPrimaryAction(
                title = if (uiState.isLoading) "Saving..." else "Save",
                enabled = !uiState.isLoading,
                onClick = onSaveClick
            )
        )
    }
    DisposableEffect(onSetActionBarPrimaryAction) {
        onDispose { onSetActionBarPrimaryAction(null) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AttenoteSectionCard(title = "Profile") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    val profilePath = uiState.profileImagePath
                    if (!profilePath.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(File(profilePath))
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profile picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Default profile",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(24.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AttenoteButton(
                        text = "Change Photo",
                        onClick = viewModel::onProfileImagePickRequested,
                        enabled = !uiState.isLoading,
                        modifier = Modifier.weight(1f)
                    )

                    if (!uiState.profileImagePath.isNullOrBlank()) {
                        OutlinedButton(
                            onClick = viewModel::onProfileImageRemoved,
                            enabled = !uiState.isLoading,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Remove")
                        }
                    }
                }

                AttenoteTextField(
                    value = uiState.userName,
                    onValueChange = viewModel::onUserNameChanged,
                    label = "Name",
                    enabled = !uiState.isLoading
                )

                AttenoteTextField(
                    value = uiState.instituteName,
                    onValueChange = viewModel::onInstituteNameChanged,
                    label = "Institute",
                    enabled = !uiState.isLoading
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Biometric Lock",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (!uiState.canEnableBiometric) {
                            Text(
                                text = "Set up a screen lock in device settings to enable this feature",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = uiState.biometricEnabled,
                        onCheckedChange = viewModel::onBiometricToggled,
                        enabled = uiState.canEnableBiometric && !uiState.isLoading
                    )
                }

                if (uiState.profileSaveSuccess) {
                    Text(
                        text = "Profile saved successfully",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        AttenoteSectionCard(title = "Default Session Format") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Preview: ${uiState.sessionPreview}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                SessionFormat.values().forEach { format ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = uiState.sessionFormat == format,
                                onClick = { viewModel.onSessionFormatChanged(format) }
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = uiState.sessionFormat == format,
                            onClick = { viewModel.onSessionFormatChanged(format) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = when (format) {
                                    SessionFormat.CURRENT_YEAR -> "Current Year"
                                    SessionFormat.ACADEMIC_YEAR -> "Academic Year"
                                },
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = when (format) {
                                    SessionFormat.CURRENT_YEAR -> "e.g., 2026"
                                    SessionFormat.ACADEMIC_YEAR -> "e.g., 2025-2026 / 2026-2027"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        AttenoteSectionCard(title = "Dashboard Menu Position") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FabPosition.values().forEach { position ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = uiState.fabPosition == position,
                                onClick = { viewModel.onFabPositionChanged(position) }
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = uiState.fabPosition == position,
                            onClick = { viewModel.onFabPositionChanged(position) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (position == FabPosition.LEFT) "Left" else "Right",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        AttenoteSectionCard(title = "Data Management") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AttenoteButton(
                    text = if (uiState.isExporting) "Exporting..." else "Export Backup",
                    onClick = viewModel::onExportBackupRequested,
                    enabled = !uiState.isExporting && !uiState.isImporting,
                    modifier = Modifier.fillMaxWidth()
                )

                if (uiState.exportSuccess && !uiState.exportedFilePath.isNullOrBlank()) {
                    Text(
                        text = "Backup exported to:\n${uiState.exportedFilePath}",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedButton(
                    onClick = {
                        viewModel.onImportBackupRequested()
                        importPickerLauncher.launch("application/zip")
                    },
                    enabled = !uiState.isExporting && !uiState.isImporting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (uiState.isImporting) "Importing..." else "Import Backup")
                }

                if (uiState.importSuccess) {
                    Text(
                        text = "Backup imported successfully. Restarting...",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Text(
                    text = "Importing will replace all existing data",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        val errorMessage = uiState.exportError ?: uiState.importError ?: uiState.profileSaveError
        if (!errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    if (uiState.showExportConfirmDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onExportCancelled,
            title = { Text("Export Backup") },
            text = { Text("Export all app data to a ZIP file?") },
            confirmButton = {
                TextButton(onClick = viewModel::onExportConfirmed) {
                    Text("Export")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onExportCancelled) {
                    Text("Cancel")
                }
            }
        )
    }

    if (uiState.showImportConfirmDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onImportCancelled,
            title = { Text("Import Backup") },
            text = { Text("Importing will replace all existing data. Continue?") },
            confirmButton = {
                TextButton(onClick = viewModel::onImportConfirmed) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onImportCancelled) {
                    Text("Cancel")
                }
            }
        )
    }

    if (uiState.showBiometricDisabledWarningDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onBiometricDisabledWarningDismissed,
            title = { Text("Biometric Lock Disabled") },
            text = {
                Text(
                    "Device lock screen has been removed. Biometric lock was automatically disabled."
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::onBiometricDisabledWarningDismissed) {
                    Text("OK")
                }
            }
        )
    }
}

private fun restartApp(context: android.content.Context) {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        ?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    if (launchIntent != null) {
        context.startActivity(launchIntent)
    }
    (context as? Activity)?.finishAffinity()
}
