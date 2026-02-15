package com.uteacher.attenote.ui.screen.notes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatClear
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Lens
import androidx.compose.material.icons.filled.LooksOne
import androidx.compose.material.icons.filled.LooksTwo
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.richeditor.model.RichTextState

@Composable
fun RichTextToolbar(
    state: RichTextState,
    enabled: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            IconTab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                icon = Icons.Default.FormatBold,
                contentDescription = "Basic formatting"
            )
            IconTab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                icon = Icons.AutoMirrored.Filled.FormatAlignLeft,
                contentDescription = "Paragraph formatting"
            )
            IconTab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                icon = Icons.Default.TextFields,
                contentDescription = "Style formatting"
            )
        }

        when (selectedTab) {
            0 -> BasicToolbar(
                state = state,
                enabled = enabled,
                canUndo = canUndo,
                canRedo = canRedo,
                onUndo = onUndo,
                onRedo = onRedo
            )
            1 -> ParagraphToolbar(state = state, enabled = enabled)
            2 -> StyleToolbar(state = state, enabled = enabled)
        }
    }
}

@Composable
private fun BasicToolbar(
    state: RichTextState,
    enabled: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ToolbarIconButton(
            icon = Icons.Default.FormatBold,
            contentDescription = "Bold",
            enabled = enabled,
            selected = state.currentSpanStyle.fontWeight == FontWeight.Bold,
            onClick = { state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold)) }
        )
        ToolbarIconButton(
            icon = Icons.Default.FormatItalic,
            contentDescription = "Italic",
            enabled = enabled,
            selected = state.currentSpanStyle.fontStyle == FontStyle.Italic,
            onClick = { state.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic)) }
        )
        ToolbarIconButton(
            icon = Icons.Default.FormatUnderlined,
            contentDescription = "Underline",
            enabled = enabled,
            selected = state.currentSpanStyle.textDecoration == TextDecoration.Underline,
            onClick = { state.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline)) }
        )
        ToolbarIconButton(
            icon = Icons.Default.FormatClear,
            contentDescription = "Clear formatting",
            enabled = enabled,
            selected = false,
            onClick = { state.setHtml(state.toText()) }
        )
        ToolbarIconButton(
            icon = Icons.AutoMirrored.Filled.Undo,
            contentDescription = "Undo",
            enabled = enabled && canUndo,
            selected = false,
            onClick = onUndo
        )
        ToolbarIconButton(
            icon = Icons.AutoMirrored.Filled.Redo,
            contentDescription = "Redo",
            enabled = enabled && canRedo,
            selected = false,
            onClick = onRedo
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
        ToolbarIconButton(
            icon = Icons.AutoMirrored.Filled.FormatListBulleted,
            contentDescription = "Bullet list",
            enabled = enabled,
            selected = state.isUnorderedList,
            onClick = state::toggleUnorderedList
        )
        ToolbarIconButton(
            icon = Icons.Default.FormatListNumbered,
            contentDescription = "Numbered list",
            enabled = enabled,
            selected = state.isOrderedList,
            onClick = state::toggleOrderedList
        )
        ToolbarIconButton(
            icon = Icons.AutoMirrored.Filled.FormatAlignLeft,
            contentDescription = "Align left",
            enabled = enabled,
            selected = state.currentParagraphStyle.textAlign == TextAlign.Start,
            onClick = { state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Start)) }
        )
        ToolbarIconButton(
            icon = Icons.Default.FormatAlignCenter,
            contentDescription = "Align center",
            enabled = enabled,
            selected = state.currentParagraphStyle.textAlign == TextAlign.Center,
            onClick = { state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Center)) }
        )
        ToolbarIconButton(
            icon = Icons.AutoMirrored.Filled.FormatAlignRight,
            contentDescription = "Align right",
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
    var showColorPicker by remember { mutableStateOf(false) }
    val currentColor = state.currentSpanStyle.color

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ToolbarIconButton(
                icon = Icons.Default.LooksOne,
                contentDescription = "Heading 1",
                enabled = enabled,
                selected = isHeadingSelected(
                    currentStyle = state.currentSpanStyle,
                    headingStyle = HEADING_1_STYLE
                ),
                onClick = {
                    toggleHeading(
                        state = state,
                        headingStyle = HEADING_1_STYLE,
                        otherHeadingStyle = HEADING_2_STYLE
                    )
                }
            )
            ToolbarIconButton(
                icon = Icons.Default.LooksTwo,
                contentDescription = "Heading 2",
                enabled = enabled,
                selected = isHeadingSelected(
                    currentStyle = state.currentSpanStyle,
                    headingStyle = HEADING_2_STYLE
                ),
                onClick = {
                    toggleHeading(
                        state = state,
                        headingStyle = HEADING_2_STYLE,
                        otherHeadingStyle = HEADING_1_STYLE
                    )
                }
            )
            ToolbarIconButton(
                icon = Icons.Default.TextIncrease,
                contentDescription = "Increase size",
                enabled = enabled,
                selected = false,
                onClick = { applyFontSizeDelta(state = state, deltaSp = FONT_SIZE_STEP_SP) }
            )
            ToolbarIconButton(
                icon = Icons.Default.TextDecrease,
                contentDescription = "Decrease size",
                enabled = enabled,
                selected = false,
                onClick = { applyFontSizeDelta(state = state, deltaSp = -FONT_SIZE_STEP_SP) }
            )
            ToolbarIconButton(
                icon = Icons.Default.Palette,
                contentDescription = "Text color",
                enabled = enabled,
                selected = showColorPicker || currentColor.isSpecified,
                onClick = { showColorPicker = !showColorPicker }
            )
        }

        if (showColorPicker) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextColorOptions.forEach { option ->
                    ToolbarColorButton(
                        color = option.color,
                        enabled = enabled,
                        selected = isTextColorSelected(
                            currentColor = currentColor,
                            optionColor = option.color
                        ),
                        contentDescription = option.description,
                        onClick = { applyTextColor(state = state, color = option.color) }
                    )
                }
            }
        }
    }
}

@Composable
private fun IconTab(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String
) {
    Tab(
        selected = selected,
        onClick = onClick,
        text = null,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription
            )
        }
    )
}

@Composable
private fun ToolbarIconButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    selected: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    Color.Transparent
                }
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = when {
                !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                selected -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
private fun ToolbarColorButton(
    color: Color,
    contentDescription: String,
    enabled: Boolean,
    selected: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    Color.Transparent
                }
            )
    ) {
        Icon(
            imageVector = if (color.isSpecified) Icons.Default.Lens else Icons.Default.FormatClear,
            contentDescription = contentDescription,
            tint = when {
                !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                color.isSpecified -> color
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

private fun applyTextColor(
    state: RichTextState,
    color: Color
) {
    if (color.isSpecified) {
        state.addSpanStyle(SpanStyle(color = color))
        return
    }

    val currentColor = state.currentSpanStyle.color
    if (currentColor.isSpecified) {
        state.removeSpanStyle(SpanStyle(color = currentColor))
    }
}

private fun isTextColorSelected(
    currentColor: Color,
    optionColor: Color
): Boolean {
    return if (optionColor.isSpecified) {
        currentColor.isSpecified && currentColor == optionColor
    } else {
        !currentColor.isSpecified
    }
}

private fun applyFontSizeDelta(
    state: RichTextState,
    deltaSp: Float
) {
    val currentSize = state.currentSpanStyle.fontSize
    val currentValue = if (currentSize == TextUnit.Unspecified || currentSize.value.isNaN()) {
        DEFAULT_FONT_SIZE_SP
    } else {
        currentSize.value
    }
    val nextValue = (currentValue + deltaSp).coerceIn(MIN_FONT_SIZE_SP, MAX_FONT_SIZE_SP)
    state.addSpanStyle(SpanStyle(fontSize = nextValue.sp))
}

private fun toggleHeading(
    state: RichTextState,
    headingStyle: SpanStyle,
    otherHeadingStyle: SpanStyle
) {
    if (isHeadingSelected(state.currentSpanStyle, headingStyle)) {
        state.removeSpanStyle(headingStyle)
        return
    }

    if (isHeadingSelected(state.currentSpanStyle, otherHeadingStyle)) {
        state.removeSpanStyle(otherHeadingStyle)
    }
    state.addSpanStyle(headingStyle)
}

private fun isHeadingSelected(
    currentStyle: SpanStyle,
    headingStyle: SpanStyle
): Boolean {
    return currentStyle.fontSize == headingStyle.fontSize &&
        currentStyle.fontWeight == headingStyle.fontWeight
}

private data class TextColorOption(
    val description: String,
    val color: Color
)

private val TextColorOptions = listOf(
    TextColorOption(description = "Default text color", color = Color.Unspecified),
    TextColorOption(description = "Red text", color = Color(0xFFB3261E)),
    TextColorOption(description = "Blue text", color = Color(0xFF1565C0)),
    TextColorOption(description = "Green text", color = Color(0xFF2E7D32)),
    TextColorOption(description = "Orange text", color = Color(0xFFEF6C00)),
    TextColorOption(description = "Purple text", color = Color(0xFF6A1B9A))
)

private const val FONT_SIZE_STEP_SP = 2f
private const val DEFAULT_FONT_SIZE_SP = 16f
private const val MIN_FONT_SIZE_SP = 12f
private const val MAX_FONT_SIZE_SP = 48f
private val HEADING_1_STYLE = SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold)
private val HEADING_2_STYLE = SpanStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
