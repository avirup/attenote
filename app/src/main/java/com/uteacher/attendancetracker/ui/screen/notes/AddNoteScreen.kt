package com.uteacher.attendancetracker.ui.screen.notes

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.uteacher.attendancetracker.ui.components.AttenoteDatePickerDialog
import com.uteacher.attendancetracker.ui.navigation.ActionBarPrimaryAction
import com.uteacher.attendancetracker.ui.screen.notes.components.MediaThumbnail
import com.uteacher.attendancetracker.ui.screen.notes.components.RichTextToolbar
import com.uteacher.attendancetracker.ui.theme.component.AttenoteSectionCard
import com.uteacher.attendancetracker.ui.theme.component.AttenoteTextField
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AddNoteScreen(
    date: String,
    noteId: Long,
    onNavigateBack: () -> Unit,
    onSetActionBarPrimaryAction: (ActionBarPrimaryAction?) -> Unit,
    viewModel: AddNoteViewModel = koinViewModel(parameters = { parametersOf(noteId, date) })
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val latestHasUnsavedChanges by rememberUpdatedState(uiState.hasUnsavedChanges)
    val onSaveClick = remember(viewModel) { { viewModel.onSaveClicked() } }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.onAddMedia(uri, context)
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onNavigateBack()
        }
    }
    LaunchedEffect(uiState.shouldNavigateBack) {
        if (uiState.shouldNavigateBack) {
            delay(700)
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.richTextState.annotatedString) {
        if (!uiState.isLoading) {
            viewModel.onRichTextChanged()
        }
    }

    LaunchedEffect(uiState.error) {
        if (!uiState.error.isNullOrBlank()) {
            delay(2500)
            viewModel.onErrorShown()
        }
    }

    SideEffect {
        onSetActionBarPrimaryAction(
            ActionBarPrimaryAction(
                title = if (uiState.isSaving) "Saving..." else "Save",
                enabled = !uiState.isLoading && !uiState.isSaving,
                onClick = onSaveClick
            )
        )
    }
    DisposableEffect(onSetActionBarPrimaryAction) {
        onDispose {
            onSetActionBarPrimaryAction(null)
            if (latestHasUnsavedChanges) {
                viewModel.onAutoSave()
            }
        }
    }

    if (uiState.isLoading) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (noteId > 0) "Edit Note" else "Add Note",
            style = MaterialTheme.typography.titleLarge
        )

        OutlinedButton(
            onClick = viewModel::onDatePickerRequested,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSaving
        ) {
            Text(text = "Date: ${uiState.date}")
        }

        AttenoteTextField(
            value = uiState.title,
            onValueChange = viewModel::onTitleChanged,
            label = "Title (optional)",
            enabled = !uiState.isSaving
        )

        AttenoteSectionCard(title = "Content") {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RichTextEditor(
                    state = uiState.richTextState,
                    singleLine = false,
                    maxLines = Int.MAX_VALUE,
                    minLines = 10,
                    maxLength = Int.MAX_VALUE,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 300.dp)
                )
                RichTextToolbar(
                    state = uiState.richTextState,
                    enabled = !uiState.isSaving,
                    canUndo = uiState.canUndo,
                    canRedo = uiState.canRedo,
                    onUndo = viewModel::onUndoClicked,
                    onRedo = viewModel::onRedoClicked
                )
            }
        }

        AttenoteSectionCard(title = "Attachments") {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    enabled = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Image")
                }

                if (uiState.pendingMedia.isNotEmpty()) {
                    Text(
                        text = "Pending (${uiState.pendingMedia.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(uiState.pendingMedia) { index, pending ->
                            MediaThumbnail(
                                imagePath = pending.localPath,
                                onRemove = { viewModel.onRemovePendingMedia(index) },
                                removable = true
                            )
                        }
                    }
                }

                if (uiState.savedMedia.isNotEmpty()) {
                    Text(
                        text = "Saved (${uiState.savedMedia.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.savedMedia, key = { it.mediaId }) { media ->
                            MediaThumbnail(
                                imagePath = media.filePath,
                                onRemove = null,
                                removable = false
                            )
                        }
                    }
                }
            }
        }

        if (uiState.hasUnsavedChanges) {
            Text(
                text = "Unsaved changes",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }

    if (!uiState.error.isNullOrBlank()) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Snackbar(modifier = Modifier.padding(16.dp)) {
                Text(uiState.error!!)
            }
        }
    }

    if (uiState.showDatePicker) {
        AttenoteDatePickerDialog(
            initialDate = uiState.date,
            onDateSelected = viewModel::onDateSelected,
            onDismiss = viewModel::onDatePickerDismissed
        )
    }
}
