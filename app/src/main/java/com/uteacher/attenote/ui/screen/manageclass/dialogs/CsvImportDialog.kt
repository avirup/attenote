package com.uteacher.attenote.ui.screen.manageclass.dialogs

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.uteacher.attenote.ui.screen.manageclass.CsvStudentRow

@Composable
fun CsvImportDialog(
    previewData: List<CsvStudentRow>,
    error: String?,
    onFileSelected: (content: String) -> Unit,
    onToggleSelection: (index: Int, selectedForImport: Boolean) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var localError by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedEligibleCount = previewData.count { it.canImport && it.selectedForImport }
    val eligibleCount = previewData.count { it.canImport }

    val csvPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
                reader.readText()
            } ?: ""
        }.onSuccess { content ->
            if (content.isBlank()) {
                localError = "Selected CSV file is empty"
            } else {
                localError = null
                onFileSelected(content)
            }
        }.onFailure { throwable ->
            localError = "Failed to read CSV file: ${throwable.message}"
        }
    }
    val csvTemplateDownloader = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { writer ->
                writer.write(buildTemplateCsv())
            } ?: error("Unable to open destination file")
        }.onSuccess {
            localError = null
        }.onFailure { throwable ->
            localError = "Failed to download template: ${throwable.message}"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "CSV Import") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = {
                            csvPicker.launch(arrayOf("text/csv", "text/*", "application/csv"))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Select CSV File")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            csvTemplateDownloader.launch("attenote_student_template.csv")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Download CSV template"
                        )
                    }
                }

                if (!error.isNullOrBlank()) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                if (!localError.isNullOrBlank()) {
                    Text(
                        text = localError!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (previewData.isNotEmpty()) {
                    Text(
                        text = "Preview (${previewData.size})",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = onSelectAll) {
                            Text(text = "Select All")
                        }
                        OutlinedButton(onClick = onDeselectAll) {
                            Text(text = "Deselect All")
                        }
                        Text(
                            text = "$selectedEligibleCount/$eligibleCount selected",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        previewData.forEachIndexed { index, row ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Checkbox(
                                        checked = row.selectedForImport,
                                        onCheckedChange = { checked ->
                                            onToggleSelection(index, checked)
                                        },
                                        enabled = row.canImport
                                    )
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Text(
                                            text = "Row ${row.sourceRowNumber}",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                        Text(
                                            text = "Name: ${row.name.orEmpty()}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "Reg: ${row.registrationNumber.orEmpty()}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        if (!row.department.isNullOrBlank()) {
                                            Text(
                                                text = "Department: ${row.department}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        if (!row.rollNumber.isNullOrBlank()) {
                                            Text(
                                                text = "Roll: ${row.rollNumber}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        if (!row.email.isNullOrBlank()) {
                                            Text(
                                                text = "Email: ${row.email}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        if (!row.phone.isNullOrBlank()) {
                                            Text(
                                                text = "Phone: ${row.phone}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        if (row.matchedExistingStudentInactive) {
                                            Text(
                                                text = "Matched existing student (inactive)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                        if (!row.canImport) {
                                            Text(
                                                text = row.eligibilityMessage ?: "Row is not import-eligible",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                    if (row.eligibilityMessage != null && row.canImport) {
                                        Text(
                                            text = row.eligibilityMessage,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = selectedEligibleCount > 0,
                onClick = onConfirm
            ) {
                Text(text = "Import")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }
    )
}

private fun buildTemplateCsv(): String {
    return "name,registration_number,department,roll_number,email,phone\n"
}
