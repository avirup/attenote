package com.uteacher.attendancetracker.util

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object MediaCleanupScheduler {

    private const val UNIQUE_WORK_NAME = "orphan_media_cleanup"

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<OrphanMediaCleanupWorker>(
            repeatInterval = 7,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
