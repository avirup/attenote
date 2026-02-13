package com.uteacher.attendancetracker.ui.screen.manageclass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.uteacher.attendancetracker.ui.components.AttenoteDatePickerDialog
import com.uteacher.attendancetracker.ui.screen.manageclass.components.StudentRosterCard
import com.uteacher.attendancetracker.ui.screen.manageclass.dialogs.AddStudentDialog
import com.uteacher.attendancetracker.ui.screen.manageclass.dialogs.CopyFromClassDialog
import com.uteacher.attendancetracker.ui.screen.manageclass.dialogs.CsvImportDialog
import com.uteacher.attendancetracker.ui.theme.component.AttenoteButton
import com.uteacher.attendancetracker.ui.theme.component.AttenoteSectionCard
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

private val DateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

@Composable
fun EditClassScreen(
    classId: Long,
    viewModel: EditClassViewModel = koinViewModel(parameters = { parametersOf(classId) })
) {
    val uiState by viewModel.uiState.collectAsState()
    val transientMessage = remember(uiState.saveError, uiState.operationMessage) {
        uiState.saveError ?: uiState.operationMessage
    }

    LaunchedEffect(transientMessage) {
        if (!transientMessage.isNullOrBlank()) {
            delay(2500)
            viewModel.onMessageShown()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    AttenoteSectionCard(title = "Class Information") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            uiState.classItem?.let { classItem ->
                                InfoRow("Class Name", classItem.className)
                                InfoRow("Subject", classItem.subject)
                                InfoRow("Institute", classItem.instituteName)
                                InfoRow("Session", classItem.session)
                                InfoRow("Department", classItem.department)
                                InfoRow("Semester", classItem.semester)
                                if (classItem.section.isNotBlank()) {
                                    InfoRow("Section", classItem.section)
                                }
                            }
                        }
                    }
                }

                item {
                    AttenoteSectionCard(title = "Date Range") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PickerDisplayField(
                                    value = uiState.startDate?.format(DateFormatter) ?: "",
                                    label = "Start Date",
                                    placeholder = "Pick date",
                                    icon = "\uD83D\uDCC5",
                                    modifier = Modifier.weight(1f),
                                    onClick = viewModel::onStartDatePickerRequested
                                )

                                PickerDisplayField(
                                    value = uiState.endDate?.format(DateFormatter) ?: "",
                                    label = "End Date",
                                    placeholder = "Pick date",
                                    icon = "\uD83D\uDCC5",
                                    modifier = Modifier.weight(1f),
                                    onClick = viewModel::onEndDatePickerRequested
                                )
                            }

                            if (!uiState.dateRangeError.isNullOrBlank()) {
                                Text(
                                    text = uiState.dateRangeError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            if (!uiState.outOfRangeWarning.isNullOrBlank()) {
                                Text(
                                    text = uiState.outOfRangeWarning!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            AttenoteButton(
                                text = if (uiState.isSaving) "Saving..." else "Save Date Range",
                                onClick = viewModel::onSaveDateRange,
                                enabled = !uiState.isSaving
                            )
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Student Roster (${uiState.students.size})",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            TextButton(
                                onClick = viewModel::onShowAddStudentDialog,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "+ Manual", maxLines = 1)
                            }
                            TextButton(
                                onClick = viewModel::onShowCsvImportDialog,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "CSV Import", maxLines = 1)
                            }
                            TextButton(
                                onClick = viewModel::onShowCopyFromClassDialog,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Copy Class", maxLines = 1)
                            }
                        }

                        AttenoteSectionCard {
                            if (uiState.students.isEmpty()) {
                                Text(
                                    text = "No students in this class",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    uiState.students.forEach { studentInClass ->
                                        StudentRosterCard(
                                            studentInClass = studentInClass,
                                            onActiveToggled = { isActive ->
                                                viewModel.onToggleStudentActiveInClass(
                                                    studentInClass.student.studentId,
                                                    isActive
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!transientMessage.isNullOrBlank()) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text(text = transientMessage)
            }
        }
    }

    if (uiState.showDatePicker) {
        val initialDate = when (uiState.datePickerTarget) {
            DatePickerTarget.START_DATE -> uiState.startDate
            DatePickerTarget.END_DATE -> uiState.endDate
            null -> null
        }
        AttenoteDatePickerDialog(
            initialDate = initialDate,
            onDateSelected = viewModel::onDateSelected,
            onDismiss = viewModel::onDatePickerDismissed
        )
    }

    if (uiState.showAddStudentDialog) {
        AddStudentDialog(
            onDismiss = viewModel::onDismissAddStudentDialog,
            onConfirm = { name, reg, roll ->
                viewModel.onAddStudent(name, reg, roll)
            }
        )
    }

    if (uiState.showCsvImportDialog) {
        CsvImportDialog(
            previewData = uiState.csvPreviewData,
            error = uiState.csvImportError,
            onFileSelected = viewModel::onCsvFileSelected,
            onConfirm = viewModel::onConfirmCsvImport,
            onDismiss = viewModel::onDismissCsvImportDialog
        )
    }

    if (uiState.showCopyFromClassDialog) {
        CopyFromClassDialog(
            availableClasses = uiState.availableClasses,
            onClassSelected = viewModel::onCopyFromClass,
            onDismiss = viewModel::onDismissCopyFromClassDialog
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.6f)
        )
    }
}

@Composable
private fun PickerDisplayField(
    value: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    icon: String? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 56.dp)
                .clickable(onClick = onClick),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (value.isBlank()) placeholder else value,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (value.isBlank()) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.weight(1f)
                )
                if (!icon.isNullOrBlank()) {
                    Text(
                        text = icon,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}
