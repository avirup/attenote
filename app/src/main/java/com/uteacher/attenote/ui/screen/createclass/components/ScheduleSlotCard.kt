package com.uteacher.attenote.ui.screen.createclass.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uteacher.attenote.ui.screen.createclass.ScheduleSlotDraft
import com.uteacher.attenote.ui.util.computeDurationMinutes
import com.uteacher.attenote.ui.util.formatDurationCompact
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val TimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

@Composable
fun ScheduleSlotCard(
    slot: ScheduleSlotDraft,
    onDelete: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val durationMinutes = computeDurationMinutes(slot.startTime, slot.endTime)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = slot.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "${slot.startTime.format(TimeFormatter)} - ${slot.endTime.format(TimeFormatter)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (durationMinutes > 0) {
                        "Duration: ${formatDurationCompact(durationMinutes)}"
                    } else {
                        "Duration: Invalid"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (durationMinutes > 0) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }

            TextButton(
                onClick = onDelete,
                enabled = enabled
            ) {
                Text(text = "Delete", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
