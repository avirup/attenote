package com.uteacher.attendancetracker.ui.navigation

data class ActionBarPolicy(
    val title: String,
    val showBack: Boolean
)

fun AppRoute.actionBarPolicy(): ActionBarPolicy = when (this) {
    AppRoute.Splash -> ActionBarPolicy(title = "Splash", showBack = false)
    AppRoute.AuthGate -> ActionBarPolicy(title = "Auth Gate", showBack = false)
    AppRoute.Setup -> ActionBarPolicy(title = "Setup", showBack = false)
    AppRoute.Dashboard -> ActionBarPolicy(title = "Dashboard", showBack = false)
    AppRoute.CreateClass -> ActionBarPolicy(title = "Create Class", showBack = true)
    AppRoute.ManageClassList -> ActionBarPolicy(title = "Manage Classes", showBack = true)
    is AppRoute.EditClass -> ActionBarPolicy(title = "Edit Class", showBack = true)
    AppRoute.ManageStudents -> ActionBarPolicy(title = "Manage Students", showBack = true)
    is AppRoute.TakeAttendance -> ActionBarPolicy(title = "Take Attendance", showBack = true)
    is AppRoute.AddNote -> ActionBarPolicy(title = "Add Note", showBack = true)
    AppRoute.Settings -> ActionBarPolicy(title = "Settings", showBack = true)
}
