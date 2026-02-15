package com.uteacher.attenote.ui.screen.attendance.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.uteacher.attenote.domain.model.AttendanceStatus
import com.uteacher.attenote.ui.screen.attendance.AttendanceRecordItem

@Composable
fun AttendanceRecordCard(
    record: AttendanceRecordItem,
    onTogglePresent: (Boolean) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val statusLabel = when (record.status) {
        AttendanceStatus.PRESENT -> "Present"
        AttendanceStatus.ABSENT -> "Absent"
        AttendanceStatus.SKIPPED -> "Skipped"
    }
    val statusColor = when (record.status) {
        AttendanceStatus.PRESENT -> MaterialTheme.colorScheme.primary
        AttendanceStatus.ABSENT -> MaterialTheme.colorScheme.error
        AttendanceStatus.SKIPPED -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val cardColor = when (record.status) {
        AttendanceStatus.PRESENT -> MaterialTheme.colorScheme.primaryContainer
        AttendanceStatus.ABSENT -> MaterialTheme.colorScheme.errorContainer
        AttendanceStatus.SKIPPED -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.68f),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = record.student.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Reg: ${record.student.registrationNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                record.student.rollNumber?.let { roll ->
                    Text(
                        text = "Roll: $roll",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = statusColor
                )
                Switch(
                    checked = record.status == AttendanceStatus.PRESENT,
                    onCheckedChange = onTogglePresent,
                    enabled = enabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.error
                    )
                )
            }
        }
    }
}
