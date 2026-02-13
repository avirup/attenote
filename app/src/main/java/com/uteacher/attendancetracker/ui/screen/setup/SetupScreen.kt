package com.uteacher.attendancetracker.ui.screen.setup

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.uteacher.attendancetracker.ui.theme.component.AttenoteButton
import com.uteacher.attendancetracker.ui.theme.component.AttenoteSectionCard
import com.uteacher.attendancetracker.ui.theme.component.AttenoteTextField
import java.io.File
import java.io.FileOutputStream
import org.koin.androidx.compose.koinViewModel

@Composable
fun SetupScreen(
    onNavigateToDashboard: () -> Unit,
    viewModel: SetupViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val oldPath = uiState.profileImagePath
            val internalPath = copyImageToInternalStorage(context, it)
            if (internalPath != null) {
                if (!oldPath.isNullOrBlank() && oldPath != internalPath) {
                    runCatching { File(oldPath).delete() }
                }
                viewModel.onProfileImageSelected(internalPath)
            }
        }
    }

    LaunchedEffect(uiState.isSaveComplete) {
        if (uiState.isSaveComplete) {
            onNavigateToDashboard()
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AttenoteTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChanged,
                label = "Name *",
                enabled = !uiState.isLoading
            )

            AttenoteTextField(
                value = uiState.institute,
                onValueChange = viewModel::onInstituteChanged,
                label = "Institute (optional)",
                enabled = !uiState.isLoading
            )

            AttenoteSectionCard(title = "Profile Picture") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val imagePath = uiState.profileImagePath
                    if (!imagePath.isNullOrBlank()) {
                        val bitmap = BitmapFactory.decodeFile(imagePath)
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Profile picture",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                        TextButton(
                            onClick = {
                                runCatching { File(imagePath).delete() }
                                viewModel.onProfileImageSelected(null)
                            },
                            enabled = !uiState.isLoading
                        ) {
                            Text(text = "Remove")
                        }
                    } else {
                        AttenoteButton(
                            text = "Select Image",
                            onClick = {
                                imagePickerLauncher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            enabled = !uiState.isLoading
                        )
                    }
                }
            }

            AttenoteSectionCard(title = "Security") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Enable Biometric Lock",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Switch(
                            checked = uiState.biometricEnabled,
                            onCheckedChange = viewModel::onBiometricToggled,
                            enabled = uiState.isDeviceSecure && !uiState.isLoading
                        )
                    }
                    if (!uiState.isDeviceSecure) {
                        Text(
                            text = "Set up a device lock screen to enable biometric lock.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            AttenoteButton(
                text = "Save",
                onClick = viewModel::onSaveClicked,
                enabled = !uiState.isLoading
            )

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

private fun copyImageToInternalStorage(context: Context, sourceUri: Uri): String? {
    return try {
        val imagesDir = File(context.filesDir, "app_images")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }

        val timestamp = System.currentTimeMillis()
        val destinationFile = File(imagesDir, "profile_$timestamp.jpg")

        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            val bitmap = BitmapFactory.decodeStream(input) ?: return null
            FileOutputStream(destinationFile).use { output ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, output)
            }
        } ?: return null

        destinationFile.absolutePath
    } catch (e: Exception) {
        Log.e("SetupScreen", "Failed to copy selected image", e)
        null
    }
}
