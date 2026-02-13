package com.uteacher.attendancetracker.di

import com.uteacher.attendancetracker.ui.screen.auth.AuthGateViewModel
import com.uteacher.attendancetracker.ui.screen.createclass.CreateClassViewModel
import com.uteacher.attendancetracker.ui.screen.dashboard.DashboardViewModel
import com.uteacher.attendancetracker.ui.screen.manageclass.EditClassViewModel
import com.uteacher.attendancetracker.ui.screen.manageclass.ManageClassListViewModel
import com.uteacher.attendancetracker.ui.screen.managestudents.ManageStudentsViewModel
import com.uteacher.attendancetracker.ui.screen.setup.SetupViewModel
import com.uteacher.attendancetracker.ui.screen.splash.SplashViewModel
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
        EditClassViewModel(
            classId = params.get(),
            classRepository = get(),
            studentRepository = get(),
            attendanceRepository = get()
        )
    }
}
