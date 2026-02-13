package com.uteacher.attendancetracker.di

import android.util.Log
import com.uteacher.attendancetracker.data.repository.AttendanceRepository
import com.uteacher.attendancetracker.data.repository.AttendanceRepositoryImpl
import com.uteacher.attendancetracker.data.repository.BackupSupportRepository
import com.uteacher.attendancetracker.data.repository.BackupSupportRepositoryImpl
import com.uteacher.attendancetracker.data.repository.ClassRepository
import com.uteacher.attendancetracker.data.repository.ClassRepositoryImpl
import com.uteacher.attendancetracker.data.repository.NoteRepository
import com.uteacher.attendancetracker.data.repository.NoteRepositoryImpl
import com.uteacher.attendancetracker.data.repository.SettingsPreferencesRepository
import com.uteacher.attendancetracker.data.repository.SettingsPreferencesRepositoryImpl
import com.uteacher.attendancetracker.data.repository.StudentRepository
import com.uteacher.attendancetracker.data.repository.StudentRepositoryImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private const val REPO_TAG = "RepoInit"

val repositoryModule = module {
    single<ClassRepository>(createdAtStart = true) {
        ClassRepositoryImpl(
            classDao = get(),
            scheduleDao = get(),
            db = get()
        ).also {
            Log.d(REPO_TAG, "ClassRepositoryImpl initialized")
        }
    }

    single<StudentRepository>(createdAtStart = true) {
        StudentRepositoryImpl(
            studentDao = get(),
            crossRefDao = get(),
            db = get()
        ).also {
            Log.d(REPO_TAG, "StudentRepositoryImpl initialized")
        }
    }

    single<AttendanceRepository>(createdAtStart = true) {
        AttendanceRepositoryImpl(
            sessionDao = get(),
            recordDao = get(),
            db = get()
        ).also {
            Log.d(REPO_TAG, "AttendanceRepositoryImpl initialized")
        }
    }

    single<NoteRepository>(createdAtStart = true) {
        NoteRepositoryImpl(
            noteDao = get(),
            mediaDao = get(),
            db = get(),
            context = androidContext()
        ).also {
            Log.d(REPO_TAG, "NoteRepositoryImpl initialized")
        }
    }

    single<SettingsPreferencesRepository>(createdAtStart = true) {
        SettingsPreferencesRepositoryImpl(
            dataStore = get()
        ).also {
            Log.d(REPO_TAG, "SettingsPreferencesRepositoryImpl initialized")
        }
    }

    single<BackupSupportRepository>(createdAtStart = true) {
        BackupSupportRepositoryImpl(
            db = get(),
            dataStore = get(),
            context = androidContext()
        ).also {
            Log.d(REPO_TAG, "BackupSupportRepositoryImpl initialized")
        }
    }
}
