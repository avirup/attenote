package com.uteacher.attenote.ui.navigation

data class ActionBarPrimaryAction(
    val title: String,
    val iconResId: Int? = null,
    val contentDescription: String = title,
    val iconSizeDp: Float? = null,
    val endPaddingDp: Float = 0f,
    val enabled: Boolean = true,
    val secondaryAction: ActionBarSecondaryAction? = null,
    val onClick: () -> Unit
)

data class ActionBarSecondaryAction(
    val title: String,
    val iconResId: Int? = null,
    val contentDescription: String = title,
    val iconSizeDp: Float? = null,
    val endPaddingDp: Float = 0f,
    val enabled: Boolean = true,
    val onClick: () -> Unit
)
