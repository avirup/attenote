package com.uteacher.attenote.ui.screen.managestudents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.uteacher.attenote.ui.screen.managestudents.components.StudentCard
import com.uteacher.attenote.ui.screen.managestudents.dialogs.StudentEditorDialog
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun ManageStudentsScreen(
    viewModel: ManageStudentsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.error) {
        if (!uiState.error.isNullOrBlank()) {
            delay(2500)
            viewModel.onErrorShown()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.fillMaxSize()
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                label = { Text("Search students...") },
                singleLine = true,
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Text(text = "X")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.filteredStudents.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uiState.searchQuery.isNotBlank()) {
                                "No students found for \"${uiState.searchQuery}\""
                            } else {
                                "No students.\nTap + to add a student."
                            },
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 12.dp, top = 0.dp, end = 12.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        item {
                            val count = uiState.filteredStudents.size
                            Text(
                                text = "$count student${if (count == 1) "" else "s"}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        items(
                            items = uiState.filteredStudents,
                            key = { student -> student.studentId }
                        ) { student ->
                            StudentCard(
                                student = student,
                                onActiveToggled = { isActive ->
                                    viewModel.onToggleStudentActive(student.studentId, isActive)
                                },
                                onEditClicked = { viewModel.onShowEditDialog(student) }
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = viewModel::onShowAddDialog,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Text(text = "+")
        }

        if (!uiState.error.isNullOrBlank()) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 16.dp, end = 16.dp, bottom = 84.dp)
            ) {
                Text(text = uiState.error!!)
            }
        }
    }

    if (uiState.showEditorDialog) {
        StudentEditorDialog(
            mode = uiState.editMode,
            name = uiState.editorName,
            registrationNumber = uiState.editorRegistrationNumber,
            rollNumber = uiState.editorRollNumber,
            email = uiState.editorEmail,
            phone = uiState.editorPhone,
            isActive = uiState.editorIsActive,
            nameError = uiState.editorNameError,
            regError = uiState.editorRegError,
            error = uiState.editorError,
            isSaving = uiState.isSaving,
            onNameChanged = viewModel::onEditorNameChanged,
            onRegChanged = viewModel::onEditorRegChanged,
            onRollChanged = viewModel::onEditorRollChanged,
            onEmailChanged = viewModel::onEditorEmailChanged,
            onPhoneChanged = viewModel::onEditorPhoneChanged,
            onActiveToggled = viewModel::onEditorActiveToggled,
            onSave = viewModel::onSaveStudent,
            onDismiss = viewModel::onDismissEditorDialog
        )
    }
}
