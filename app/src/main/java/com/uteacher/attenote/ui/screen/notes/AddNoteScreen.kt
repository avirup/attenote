package com.uteacher.attenote.ui.screen.notes

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.uteacher.attenote.ui.components.AttenoteDatePickerDialog
import com.uteacher.attenote.ui.navigation.ActionBarPrimaryAction
import com.uteacher.attenote.ui.navigation.ActionBarSecondaryAction
import com.uteacher.attenote.ui.screen.notes.components.MediaThumbnail
import com.uteacher.attenote.ui.screen.notes.components.RichTextToolbar
import com.uteacher.attenote.ui.theme.component.AttenoteSectionCard
import com.uteacher.attenote.ui.theme.component.AttenoteTextField
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
    val lifecycleOwner = LocalLifecycleOwner.current
    val isEditMode = noteId > 0L

    val latestShouldAutoSave by rememberUpdatedState(
        uiState.hasUnsavedChanges && !uiState.isDeletingNote
    )
    val onSaveClick = remember(viewModel) { { viewModel.onSaveClicked() } }
    val onDeleteNoteClick = remember(viewModel) { { viewModel.onDeleteNoteRequested() } }

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
            delay(3000)
            viewModel.onErrorShown()
        }
    }

    val canMutate = !uiState.isLoading && !uiState.isSaving && !uiState.isDeletingNote
    val actionBarAction = remember(
        isEditMode,
        canMutate,
        uiState.isSaving,
        uiState.isDeletingNote,
        onSaveClick,
        onDeleteNoteClick
    ) {
        buildActionBarAction(
            isEditMode = isEditMode,
            canMutate = canMutate,
            isSaving = uiState.isSaving,
            isDeletingNote = uiState.isDeletingNote,
            onSaveClick = onSaveClick,
            onDeleteClick = onDeleteNoteClick
        )
    }

    SideEffect {
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            onSetActionBarPrimaryAction(actionBarAction)
        }
    }
    DisposableEffect(lifecycleOwner, actionBarAction) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onSetActionBarPrimaryAction(actionBarAction)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            onSetActionBarPrimaryAction(null)
            if (latestShouldAutoSave) {
                viewModel.onAutoSave()
            }
        }
    }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 3.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                RichTextToolbar(
                    state = uiState.richTextState,
                    enabled = canMutate,
                    canUndo = uiState.canUndo,
                    canRedo = uiState.canRedo,
                    onUndo = viewModel::onUndoClicked,
                    onRedo = viewModel::onRedoClicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (isEditMode) "Edit Note" else "Add Note",
                style = MaterialTheme.typography.titleLarge
            )

            Button(
                onClick = viewModel::onDatePickerRequested,
                modifier = Modifier.fillMaxWidth(),
                enabled = canMutate,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Select note date"
                    )
                    Text(text = uiState.date.toString())
                }
            }

            AttenoteTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChanged,
                label = "Title (optional)",
                enabled = canMutate
            )

            AttenoteSectionCard(title = "Content") {
                RichTextEditor(
                    state = uiState.richTextState,
                    enabled = canMutate,
                    singleLine = false,
                    maxLines = Int.MAX_VALUE,
                    minLines = 10,
                    maxLength = Int.MAX_VALUE,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 240.dp, max = 320.dp)
                )
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
                        enabled = canMutate,
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
                                    removable = canMutate
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
                                val isDeletingMedia = uiState.deletingMediaIds.contains(media.mediaId)
                                val removable = isEditMode && canMutate && !isDeletingMedia
                                MediaThumbnail(
                                    imagePath = media.filePath,
                                    onRemove = if (removable) {
                                        { viewModel.onDeleteSavedMediaRequested(media.mediaId) }
                                    } else {
                                        null
                                    },
                                    removable = removable
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
    }

    if (!uiState.error.isNullOrBlank()) {
        Box(
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

    if (uiState.showDeleteNoteConfirmation) {
        AlertDialog(
            onDismissRequest = viewModel::onDeleteNoteDismissed,
            title = { Text("Delete Note") },
            text = {
                Text("Delete this note and all attached media permanently? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = viewModel::onDeleteNoteConfirmed,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDeleteNoteDismissed) {
                    Text("Cancel")
                }
            }
        )
    }

    if (uiState.deleteMediaCandidateId != null) {
        AlertDialog(
            onDismissRequest = viewModel::onDeleteSavedMediaDismissed,
            title = { Text("Delete Attachment") },
            text = {
                Text("Delete this saved attachment permanently?")
            },
            confirmButton = {
                Button(
                    onClick = viewModel::onDeleteSavedMediaConfirmed,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDeleteSavedMediaDismissed) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun buildActionBarAction(
    isEditMode: Boolean,
    canMutate: Boolean,
    isSaving: Boolean,
    isDeletingNote: Boolean,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit
): ActionBarPrimaryAction {
    return ActionBarPrimaryAction(
        title = if (isSaving) {
            "Saving..."
        } else if (isDeletingNote) {
            "Deleting..."
        } else {
            "Save"
        },
        enabled = canMutate,
        secondaryAction = if (isEditMode) {
            ActionBarSecondaryAction(
                title = "Delete note",
                iconResId = android.R.drawable.ic_menu_delete,
                contentDescription = "Delete note",
                enabled = canMutate,
                onClick = onDeleteClick
            )
        } else {
            null
        },
        onClick = onSaveClick
    )
}
