package com.uteacher.attenote.ui.screen.managestudents.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.uteacher.attenote.ui.screen.managestudents.StudentEditMode

@Composable
fun StudentEditorDialog(
    mode: StudentEditMode,
    name: String,
    registrationNumber: String,
    department: String,
    rollNumber: String,
    email: String,
    phone: String,
    nameError: String?,
    regError: String?,
    error: String?,
    isSaving: Boolean,
    onNameChanged: (String) -> Unit,
    onRegChanged: (String) -> Unit,
    onDepartmentChanged: (String) -> Unit,
    onRollChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onDeleteRequested: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (mode == StudentEditMode.ADD) "Add Student" else "Edit Student",
                    style = MaterialTheme.typography.titleLarge
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChanged,
                    label = { Text("Name *") },
                    singleLine = true,
                    isError = !nameError.isNullOrBlank(),
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth()
                )
                if (!nameError.isNullOrBlank()) {
                    Text(
                        text = nameError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                OutlinedTextField(
                    value = registrationNumber,
                    onValueChange = onRegChanged,
                    label = { Text("Registration Number *") },
                    singleLine = true,
                    isError = !regError.isNullOrBlank(),
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth()
                )
                if (!regError.isNullOrBlank()) {
                    Text(
                        text = regError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                OutlinedTextField(
                    value = department,
                    onValueChange = onDepartmentChanged,
                    label = { Text("Department (optional)") },
                    singleLine = true,
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = rollNumber,
                    onValueChange = onRollChanged,
                    label = { Text("Roll Number (optional)") },
                    singleLine = true,
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChanged,
                    label = { Text("Email (optional)") },
                    singleLine = true,
                    enabled = !isSaving,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = onPhoneChanged,
                    label = { Text("Phone (optional)") },
                    singleLine = true,
                    enabled = !isSaving,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                if (!error.isNullOrBlank()) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (mode == StudentEditMode.EDIT) {
                        TextButton(
                            onClick = onDeleteRequested,
                            enabled = !isSaving
                        ) {
                            Text(
                                text = "Delete Student",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            enabled = !isSaving
                        ) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onSave,
                            enabled = !isSaving
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(if (mode == StudentEditMode.ADD) "Add" else "Save")
                            }
                        }
                    }
                }
            }
        }
    }
}
