package com.uteacher.attendancetracker.ui.screen.notes.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.richeditor.model.RichTextState

@Composable
fun RichTextToolbar(
    state: RichTextState,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    androidx.compose.foundation.layout.Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ToolbarButton(
                label = "Basic",
                enabled = enabled,
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 }
            )
            ToolbarButton(
                label = "Paragraph",
                enabled = enabled,
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 }
            )
            ToolbarButton(
                label = "Style",
                enabled = enabled,
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 }
            )
        }

        when (selectedTab) {
            0 -> BasicToolbar(state = state, enabled = enabled)
            1 -> ParagraphToolbar(state = state, enabled = enabled)
            2 -> StyleToolbar(state = state, enabled = enabled)
        }
    }
}

@Composable
private fun BasicToolbar(
    state: RichTextState,
    enabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ToolbarButton(
            label = "Bold",
            enabled = enabled,
            selected = state.currentSpanStyle.fontWeight == FontWeight.Bold,
            onClick = { state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold)) }
        )
        ToolbarButton(
            label = "Italic",
            enabled = enabled,
            selected = state.currentSpanStyle.fontStyle == FontStyle.Italic,
            onClick = { state.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic)) }
        )
        ToolbarButton(
            label = "Underline",
            enabled = enabled,
            selected = state.currentSpanStyle.textDecoration == TextDecoration.Underline,
            onClick = { state.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline)) }
        )
        ToolbarButton(
            label = "Clear",
            enabled = enabled,
            selected = false,
            onClick = { state.setHtml(state.annotatedString.text) }
        )
        ToolbarButton(
            label = "Undo",
            enabled = enabled,
            selected = false,
            onClick = { invokeNoArgMethod(state, "undo") }
        )
        ToolbarButton(
            label = "Redo",
            enabled = enabled,
            selected = false,
            onClick = { invokeNoArgMethod(state, "redo") }
        )
    }
}

@Composable
private fun ParagraphToolbar(
    state: RichTextState,
    enabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ToolbarButton(
            label = "Bullet",
            enabled = enabled,
            selected = state.isUnorderedList,
            onClick = { state.toggleUnorderedList() }
        )
        ToolbarButton(
            label = "Numbered",
            enabled = enabled,
            selected = state.isOrderedList,
            onClick = { state.toggleOrderedList() }
        )
        ToolbarButton(
            label = "Left",
            enabled = enabled,
            selected = state.currentParagraphStyle.textAlign == TextAlign.Start,
            onClick = { state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Start)) }
        )
        ToolbarButton(
            label = "Center",
            enabled = enabled,
            selected = state.currentParagraphStyle.textAlign == TextAlign.Center,
            onClick = { state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Center)) }
        )
        ToolbarButton(
            label = "Right",
            enabled = enabled,
            selected = state.currentParagraphStyle.textAlign == TextAlign.End,
            onClick = { state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.End)) }
        )
    }
}

@Composable
private fun StyleToolbar(
    state: RichTextState,
    enabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ToolbarButton(
            label = "H1",
            enabled = enabled,
            selected = state.currentSpanStyle.fontSize == 28.sp,
            onClick = { state.toggleSpanStyle(SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold)) }
        )
        ToolbarButton(
            label = "H2",
            enabled = enabled,
            selected = state.currentSpanStyle.fontSize == 22.sp,
            onClick = { state.toggleSpanStyle(SpanStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold)) }
        )
        ToolbarButton(
            label = "Body",
            enabled = enabled,
            selected = state.currentSpanStyle.fontSize == 16.sp,
            onClick = { state.toggleSpanStyle(SpanStyle(fontSize = 16.sp)) }
        )
        ToolbarButton(
            label = "Code",
            enabled = enabled,
            selected = state.isCodeSpan,
            onClick = { state.toggleCodeSpan() }
        )
        ToolbarButton(
            label = "A+",
            enabled = enabled,
            selected = false,
            onClick = { state.toggleSpanStyle(SpanStyle(fontSize = 20.sp)) }
        )
        ToolbarButton(
            label = "A-",
            enabled = enabled,
            selected = false,
            onClick = { state.toggleSpanStyle(SpanStyle(fontSize = 12.sp)) }
        )
    }
}

@Composable
private fun ToolbarButton(
    label: String,
    enabled: Boolean,
    selected: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline
            }
        ),
        modifier = Modifier.padding(0.dp)
    ) {
        Text(
            text = label,
            style = if (selected) {
                MaterialTheme.typography.labelLarge.merge(TextStyle(color = MaterialTheme.colorScheme.primary))
            } else {
                MaterialTheme.typography.labelLarge
            }
        )
    }
}

private fun invokeNoArgMethod(target: Any, methodName: String): Boolean {
    return runCatching {
        val method = target.javaClass.methods.firstOrNull { method ->
            method.name == methodName && method.parameterTypes.isEmpty()
        } ?: return false
        method.invoke(target)
        true
    }.getOrElse { false }
}
