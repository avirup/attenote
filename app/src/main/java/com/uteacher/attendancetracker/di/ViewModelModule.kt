package com.uteacher.attendancetracker.di

import com.uteacher.attendancetracker.ui.screen.auth.AuthGateViewModel
import com.uteacher.attendancetracker.ui.screen.createclass.CreateClassViewModel
import com.uteacher.attendancetracker.ui.screen.dashboard.DashboardViewModel
import com.uteacher.attendancetracker.ui.screen.attendance.TakeAttendanceViewModel
import com.uteacher.attendancetracker.ui.screen.notes.AddNoteViewModel
import com.uteacher.attendancetracker.ui.screen.manageclass.EditClassViewModel
import com.uteacher.attendancetracker.ui.screen.manageclass.ManageClassListViewModel
import com.uteacher.attendancetracker.ui.screen.managestudents.ManageStudentsViewModel
import com.uteacher.attendancetracker.ui.screen.settings.SettingsViewModel
import com.uteacher.attendancetracker.ui.screen.setup.SetupViewModel
import com.uteacher.attendancetracker.ui.screen.splash.SplashViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::SplashViewModel)
    viewModelOf(::SetupViewModel)
    viewModelOf(::AuthGateViewModel)
    viewModelOf(::DashboardViewModel)
    viewModelOf(::CreateClassViewModel)
    viewModelOf(::ManageClassListViewModel)
    viewModelOf(::ManageStudentsViewModel)
    viewModel { params ->
        TakeAttendanceViewModel(
            classId = params.get(),
            scheduleId = params.get(),
            dateString = params.get(),
            classRepository = get(),
            studentRepository = get(),
            attendanceRepository = get()
        )
    }
    viewModel { params ->
        AddNoteViewModel(
            noteId = params.get(),
            dateString = params.get(),
            noteRepository = get()
        )
    }
    viewModel { params ->
        EditClassViewModel(
            classId = params.get(),
            classRepository = get(),
            studentRepository = get(),
            attendanceRepository = get()
        )
    }
    viewModel {
        SettingsViewModel(
            settingsRepository = get(),
            backupRepository = get(),
            biometricHelper = get(),
            context = androidContext()
        )
    }
}
