package com.uteacher.attenote.ui.screen.managestudents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
    var departmentMenuExpanded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.error) {
        if (!uiState.error.isNullOrBlank()) {
            delay(2500)
            viewModel.onErrorShown()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
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
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Status",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.selectedStatusFilter == StudentStatusFilter.ALL,
                        onClick = { viewModel.onStatusFilterChanged(StudentStatusFilter.ALL) },
                        label = { Text("All") }
                    )
                    FilterChip(
                        selected = uiState.selectedStatusFilter == StudentStatusFilter.ACTIVE,
                        onClick = { viewModel.onStatusFilterChanged(StudentStatusFilter.ACTIVE) },
                        label = { Text("Active") }
                    )
                    FilterChip(
                        selected = uiState.selectedStatusFilter == StudentStatusFilter.INACTIVE,
                        onClick = { viewModel.onStatusFilterChanged(StudentStatusFilter.INACTIVE) },
                        label = { Text("Inactive") }
                    )
                }

                Box {
                    OutlinedButton(
                        onClick = { departmentMenuExpanded = true }
                    ) {
                        Text(
                            text = "Department: ${uiState.selectedDepartmentFilter ?: "All"}"
                        )
                    }
                    DropdownMenu(
                        expanded = departmentMenuExpanded,
                        onDismissRequest = { departmentMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Departments") },
                            onClick = {
                                departmentMenuExpanded = false
                                viewModel.onDepartmentFilterChanged(null)
                            }
                        )
                        uiState.availableDepartments.forEach { department ->
                            DropdownMenuItem(
                                text = { Text(department) },
                                onClick = {
                                    departmentMenuExpanded = false
                                    viewModel.onDepartmentFilterChanged(department)
                                }
                            )
                        }
                    }
                }

                TextButton(onClick = viewModel::onShowAddDialog) {
                    Text(text = "+ Add Student")
                }
            }

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
                                "No students found for selected filters."
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
                        contentPadding = PaddingValues(
                            start = 12.dp,
                            top = 0.dp,
                            end = 12.dp,
                            bottom = 88.dp
                        ),
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
            department = uiState.editorDepartment,
            rollNumber = uiState.editorRollNumber,
            email = uiState.editorEmail,
            phone = uiState.editorPhone,
            nameError = uiState.editorNameError,
            regError = uiState.editorRegError,
            error = uiState.editorError,
            isSaving = uiState.isSaving,
            onNameChanged = viewModel::onEditorNameChanged,
            onRegChanged = viewModel::onEditorRegChanged,
            onDepartmentChanged = viewModel::onEditorDepartmentChanged,
            onRollChanged = viewModel::onEditorRollChanged,
            onEmailChanged = viewModel::onEditorEmailChanged,
            onPhoneChanged = viewModel::onEditorPhoneChanged,
            onDeleteRequested = viewModel::onDeleteStudentRequested,
            onSave = viewModel::onSaveStudent,
            onDismiss = viewModel::onDismissEditorDialog
        )
    }

    if (uiState.showDeleteStudentConfirmation) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissDeleteStudentConfirmation,
            title = { Text("Delete Student") },
            text = {
                Text(
                    text = "This will permanently delete the student and ALL their attendance records. This action cannot be undone."
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::onConfirmDeleteStudent) {
                    Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissDeleteStudentConfirmation) {
                    Text("Cancel")
                }
            }
        )
    }

    if (uiState.showMergeConfirmation && uiState.mergeTargetStudent != null) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissMergeConfirmation,
            title = { Text("Merge Students") },
            text = {
                Text(
                    text = "A student with this updated registration number and name already exists (${uiState.mergeTargetStudent!!.name}). Confirming will relink class links and attendance records to the existing student and permanently delete the current student."
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::onConfirmMergeStudent) {
                    Text("Merge")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissMergeConfirmation) {
                    Text("Cancel")
                }
            }
        )
    }
}
