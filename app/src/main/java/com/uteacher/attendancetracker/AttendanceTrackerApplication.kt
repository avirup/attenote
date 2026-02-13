package com.uteacher.attendancetracker

import android.app.Application
import android.content.pm.ApplicationInfo
import android.util.Log
import androidx.room.Room
import com.uteacher.attendancetracker.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AttendanceTrackerApplication : Application() {

    lateinit var database: AppDatabase
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        val builder = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
        val isDebugBuild = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (isDebugBuild) {
            builder.fallbackToDestructiveMigration()
        }
        database = builder.build()

        appScope.launch {
            runCatching {
                database.openHelper.writableDatabase
            }.onSuccess {
                Log.d(TAG, "Room database initialized: ${AppDatabase.DATABASE_NAME}")
            }.onFailure { throwable ->
                Log.e(TAG, "Room database initialization failed", throwable)
            }
        }
    }

    companion object {
        private const val TAG = "AppDatabase"
    }
}
