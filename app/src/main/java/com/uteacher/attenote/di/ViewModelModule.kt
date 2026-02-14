package com.uteacher.attenote.di

import com.uteacher.attenote.ui.screen.auth.AuthGateViewModel
import com.uteacher.attenote.ui.screen.createclass.CreateClassViewModel
import com.uteacher.attenote.ui.screen.dashboard.DashboardViewModel
import com.uteacher.attenote.ui.screen.dailysummary.DailySummaryViewModel
import com.uteacher.attenote.ui.screen.attendance.TakeAttendanceViewModel
import com.uteacher.attenote.ui.screen.notes.AddNoteViewModel
import com.uteacher.attenote.ui.screen.manageclass.EditClassViewModel
import com.uteacher.attenote.ui.screen.manageclass.ManageClassListViewModel
import com.uteacher.attenote.ui.screen.managestudents.ManageStudentsViewModel
import com.uteacher.attenote.ui.screen.settings.SettingsViewModel
import com.uteacher.attenote.ui.screen.setup.SetupViewModel
import com.uteacher.attenote.ui.screen.splash.SplashViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::SplashViewModel)
    viewModelOf(::SetupViewModel)
    viewModelOf(::AuthGateViewModel)
    viewModelOf(::DashboardViewModel)
    viewModelOf(::DailySummaryViewModel)
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
