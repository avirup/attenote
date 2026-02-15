package com.uteacher.attenote.ui.screen.dashboard.components

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.uteacher.attenote.domain.model.Note
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun NoteCard(
    note: Note,
    onOpenNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentPreview = buildContentPreview(note.content)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium
                )

                if (contentPreview.isNotEmpty()) {
                    Text(
                        text = contentPreview,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = "Updated on ${formatRelativeDate(note.updatedAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TextButton(onClick = onOpenNote) {
                Text(text = "Open")
            }
        }
    }
}

private fun buildContentPreview(content: String): String {
    val stripped = content.replace(Regex("<[^>]*>"), "").trim()
    if (stripped.length <= 100) return stripped
    return stripped.take(100) + "..."
}

private fun formatRelativeDate(date: LocalDate): String {
    val today = LocalDate.now()
    val days = ChronoUnit.DAYS.between(date, today)
    return when {
        days <= 0 -> "today"
        days == 1L -> "1 day ago"
        days < 7L -> "$days days ago"
        else -> {
            val monthLabel = date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            "$monthLabel ${date.dayOfMonth}, ${date.year}"
        }
    }
}
