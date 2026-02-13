package com.uteacher.attendancetracker.ui.screen.manageclass.dialogs

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.uteacher.attendancetracker.ui.screen.manageclass.CsvStudentRow
import com.uteacher.attendancetracker.ui.theme.component.AttenoteSecondaryButton

@Composable
fun CsvImportDialog(
    previewData: List<CsvStudentRow>,
    error: String?,
    onFileSelected: (content: String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var localError by rememberSaveable { mutableStateOf<String?>(null) }

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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "CSV Import") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AttenoteSecondaryButton(
                    text = "Select CSV File",
                    onClick = {
                        csvPicker.launch(arrayOf("text/csv", "text/*", "application/csv"))
                    }
                )

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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        previewData.forEach { row ->
                            val warnings = buildList {
                                if (row.isDuplicate) add("Duplicate")
                                if (row.alreadyExists) add("Already exists")
                                if (row.hasWarning && !row.isDuplicate) add("Contains placeholders")
                            }.joinToString(" • ")

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(
                                        text = "Name: ${row.name}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Reg: ${row.registrationNumber}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
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
                                    if (warnings.isNotBlank()) {
                                        Text(
                                            text = warnings,
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
                enabled = previewData.isNotEmpty(),
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
