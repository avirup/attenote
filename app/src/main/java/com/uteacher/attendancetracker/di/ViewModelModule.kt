package com.uteacher.attendancetracker.di

import com.uteacher.attendancetracker.ui.screen.auth.AuthGateViewModel
import com.uteacher.attendancetracker.ui.screen.dashboard.DashboardViewModel
import com.uteacher.attendancetracker.ui.screen.setup.SetupViewModel
import com.uteacher.attendancetracker.ui.screen.splash.SplashViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::SplashViewModel)
    viewModelOf(::SetupViewModel)
    viewModelOf(::AuthGateViewModel)
    viewModelOf(::DashboardViewModel)
}
