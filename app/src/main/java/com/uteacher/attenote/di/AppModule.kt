package com.uteacher.attenote.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.uteacher.attenote.data.local.AppDatabase
import com.uteacher.attenote.util.BiometricHelper
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "attenote_preferences"
)

val appModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    single { get<AppDatabase>().classDao() }
    single { get<AppDatabase>().studentDao() }
    single { get<AppDatabase>().classStudentCrossRefDao() }
    single { get<AppDatabase>().scheduleDao() }
    single { get<AppDatabase>().attendanceSessionDao() }
    single { get<AppDatabase>().attendanceRecordDao() }
    single { get<AppDatabase>().noteDao() }
    single { get<AppDatabase>().noteMediaDao() }

    single<DataStore<Preferences>> { androidContext().dataStore }
    single { BiometricHelper(androidContext()) }
}
