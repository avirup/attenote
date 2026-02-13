package com.uteacher.attendancetracker.domain.model

enum class FabPosition {
    LEFT,
    RIGHT
}

fun String.toFabPosition(): FabPosition {
    return try {
        FabPosition.valueOf(this)
    } catch (_: IllegalArgumentException) {
        FabPosition.RIGHT
    }
}
