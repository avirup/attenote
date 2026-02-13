package com.uteacher.attendancetracker

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.uteacher.attendancetracker.data.local.AppDatabase
import com.uteacher.attendancetracker.data.repository.AttendanceRepositoryImpl
import com.uteacher.attendancetracker.data.repository.BackupSupportRepositoryImpl
import com.uteacher.attendancetracker.data.repository.ClassRepositoryImpl
import com.uteacher.attendancetracker.data.repository.NoteRepositoryImpl
import com.uteacher.attendancetracker.data.repository.SettingsPreferencesRepositoryImpl
import com.uteacher.attendancetracker.data.repository.StudentRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "attenote_preferences"
)

class AttendanceTrackerApplication : Application() {

    lateinit var database: AppDatabase
        private set

    private var repositoryWarmupRefs: List<Any> = emptyList()

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
                warmupRepositories()
            }.onFailure { throwable ->
                Log.e(TAG, "Room database initialization failed", throwable)
            }
        }
    }

    private fun warmupRepositories() {
        runCatching {
            val classRepository = ClassRepositoryImpl(
                classDao = database.classDao(),
                scheduleDao = database.scheduleDao(),
                db = database
            )
            Log.d(REPO_TAG, "ClassRepositoryImpl initialized")

            val studentRepository = StudentRepositoryImpl(
                studentDao = database.studentDao(),
                crossRefDao = database.classStudentCrossRefDao(),
                db = database
            )
            Log.d(REPO_TAG, "StudentRepositoryImpl initialized")

            val attendanceRepository = AttendanceRepositoryImpl(
                sessionDao = database.attendanceSessionDao(),
                recordDao = database.attendanceRecordDao(),
                db = database
            )
            Log.d(REPO_TAG, "AttendanceRepositoryImpl initialized")

            val noteRepository = NoteRepositoryImpl(
                noteDao = database.noteDao(),
                mediaDao = database.noteMediaDao(),
                db = database,
                context = applicationContext
            )
            Log.d(REPO_TAG, "NoteRepositoryImpl initialized")

            val settingsRepository = SettingsPreferencesRepositoryImpl(
                dataStore = applicationContext.dataStore
            )
            Log.d(REPO_TAG, "SettingsPreferencesRepositoryImpl initialized")

            val backupRepository = BackupSupportRepositoryImpl(
                db = database,
                dataStore = applicationContext.dataStore,
                context = applicationContext
            )
            Log.d(REPO_TAG, "BackupSupportRepositoryImpl initialized")

            repositoryWarmupRefs = listOf(
                classRepository,
                studentRepository,
                attendanceRepository,
                noteRepository,
                settingsRepository,
                backupRepository
            )
        }.onSuccess {
            Log.d(REPO_TAG, "Repositories initialized successfully")
        }.onFailure { throwable ->
            Log.e(REPO_TAG, "Repository warmup failed", throwable)
        }
    }

    companion object {
        private const val TAG = "AppDatabase"
        private const val REPO_TAG = "RepoInit"
    }
}
