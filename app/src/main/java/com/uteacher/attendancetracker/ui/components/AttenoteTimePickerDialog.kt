package com.uteacher.attendancetracker.ui.components

import androidx.compose.runtime.Composable
import com.uteacher.attendancetracker.ui.theme.component.AttenoteTimePicker
import java.time.LocalTime

@Composable
fun AttenoteTimePickerDialog(
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    AttenoteTimePicker(
        showDialog = true,
        onDismissRequest = onDismiss,
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        onTimeSelected = { hour, minute ->
            onTimeSelected(LocalTime.of(hour, minute))
        }
    )
}
