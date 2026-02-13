package com.uteacher.attendancetracker.ui.navigation

data class ActionBarPrimaryAction(
    val title: String,
    val enabled: Boolean = true,
    val onClick: () -> Unit
)
