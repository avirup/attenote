package com.uteacher.attendancetracker.ui.components

import androidx.compose.runtime.Composable
import com.uteacher.attendancetracker.ui.theme.component.AttenoteDatePicker
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun AttenoteDatePickerDialog(
    initialDate: LocalDate? = null,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    AttenoteDatePicker(
        showDialog = true,
        onDismissRequest = onDismiss,
        initialSelectedDateMillis = initialDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
        onDateSelected = { millis ->
            if (millis != null) {
                val date = Instant.ofEpochMilli(millis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                onDateSelected(date)
            } else {
                onDismiss()
            }
        }
    )
}
