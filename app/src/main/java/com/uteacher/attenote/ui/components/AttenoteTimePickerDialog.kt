package com.uteacher.attenote.ui.components

import androidx.compose.runtime.Composable
import com.uteacher.attenote.ui.theme.component.AttenoteTimePicker
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
        is24Hour = false,
        onTimeSelected = { hour, minute ->
            onTimeSelected(LocalTime.of(hour, minute))
        }
    )
}
