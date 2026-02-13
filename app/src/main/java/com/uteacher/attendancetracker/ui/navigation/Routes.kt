package com.uteacher.attendancetracker.ui.navigation

import kotlinx.serialization.Serializable

private val DatePattern = Regex("^\\d{4}-\\d{2}-\\d{2}$")

@Serializable
sealed interface AppRoute {

    @Serializable
    data object Splash : AppRoute

    @Serializable
    data object AuthGate : AppRoute

    @Serializable
    data object Dashboard : AppRoute

    @Serializable
    data object DailySummary : AppRoute

    @Serializable
    data object Setup : AppRoute

    @Serializable
    data object CreateClass : AppRoute

    @Serializable
    data object ManageClassList : AppRoute

    @Serializable
    data class EditClass(val classId: Long) : AppRoute {
        init {
            require(classId > 0) { "classId must be positive" }
        }
    }

    @Serializable
    data object ManageStudents : AppRoute

    @Serializable
    data class TakeAttendance(
        val classId: Long,
        val scheduleId: Long,
        val date: String
    ) : AppRoute {
        init {
            require(classId > 0) { "classId must be positive" }
            require(scheduleId > 0) { "scheduleId must be positive" }
            require(DatePattern.matches(date)) {
                "date must be yyyy-MM-dd format, got: $date"
            }
        }
    }

    @Serializable
    data class AddNote(
        val date: String,
        val noteId: Long = -1L
    ) : AppRoute {
        init {
            require(DatePattern.matches(date)) {
                "date must be yyyy-MM-dd format, got: $date"
            }
            require(noteId >= -1L) {
                "noteId must be >= -1, got: $noteId"
            }
        }
    }

    @Serializable
    data object Settings : AppRoute
}
