package com.uteacher.attendancetracker

import android.app.Application
import android.util.Log
import com.uteacher.attendancetracker.data.repository.AttendanceRepository
import com.uteacher.attendancetracker.data.repository.BackupSupportRepository
import com.uteacher.attendancetracker.data.repository.ClassRepository
import com.uteacher.attendancetracker.data.repository.NoteRepository
import com.uteacher.attendancetracker.data.repository.SettingsPreferencesRepository
import com.uteacher.attendancetracker.data.repository.StudentRepository
import com.uteacher.attendancetracker.di.appModule
import com.uteacher.attendancetracker.di.repositoryModule
import com.uteacher.attendancetracker.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.java.KoinJavaComponent.getKoin

class AttendanceTrackerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@AttendanceTrackerApplication)
            modules(appModule, repositoryModule, viewModelModule)
        }

        runCatching {
            val koin = getKoin()
            koin.get<ClassRepository>()
            koin.get<StudentRepository>()
            koin.get<AttendanceRepository>()
            koin.get<NoteRepository>()
            koin.get<SettingsPreferencesRepository>()
            koin.get<BackupSupportRepository>()
        }.onSuccess {
            Log.d(REPO_TAG, "Repositories initialized successfully")
        }.onFailure { throwable ->
            Log.e(REPO_TAG, "Repository initialization failed", throwable)
        }
    }

    private companion object {
        private const val REPO_TAG = "RepoInit"
    }
}
