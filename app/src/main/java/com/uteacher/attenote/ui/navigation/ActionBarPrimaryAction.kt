package com.uteacher.attenote.ui.navigation

data class ActionBarPrimaryAction(
    val title: String,
    val iconResId: Int? = null,
    val contentDescription: String = title,
    val enabled: Boolean = true,
    val onClick: () -> Unit
)
