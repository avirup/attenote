package com.uteacher.attenote.ui.screen.manageclass.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddStudentDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, registrationNumber: String, rollNumber: String?) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var registrationNumber by rememberSaveable { mutableStateOf("") }
    var rollNumber by rememberSaveable { mutableStateOf("") }
    var nameError by rememberSaveable { mutableStateOf<String?>(null) }
    var registrationError by rememberSaveable { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Add Student") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                    },
                    label = { Text("Name *") },
                    isError = !nameError.isNullOrBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                if (!nameError.isNullOrBlank()) {
                    Text(
                        text = nameError!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                OutlinedTextField(
                    value = registrationNumber,
                    onValueChange = {
                        registrationNumber = it
                        registrationError = null
                    },
                    label = { Text("Registration Number *") },
                    isError = !registrationError.isNullOrBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                if (!registrationError.isNullOrBlank()) {
                    Text(
                        text = registrationError!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                OutlinedTextField(
                    value = rollNumber,
                    onValueChange = { rollNumber = it },
                    label = { Text("Roll Number (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmedName = name.trim()
                    val trimmedReg = registrationNumber.trim()
                    val hasNameError = trimmedName.isEmpty()
                    val hasRegError = trimmedReg.isEmpty()

                    nameError = if (hasNameError) "Name is required" else null
                    registrationError = if (hasRegError) {
                        "Registration number is required"
                    } else {
                        null
                    }

                    if (!hasNameError && !hasRegError) {
                        onConfirm(trimmedName, trimmedReg, rollNumber.trim().ifBlank { null })
                    }
                }
            ) {
                Text(text = "Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }
    )
}
