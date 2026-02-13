package com.uteacher.attendancetracker.data.repository

import android.net.Uri
import com.uteacher.attendancetracker.data.repository.internal.RepositoryResult

interface BackupSupportRepository {
    suspend fun exportBackup(destinationUri: Uri): RepositoryResult<Unit>
    suspend fun importBackup(sourceUri: Uri): RepositoryResult<Unit>

    suspend fun hasInterruptedRestore(): Boolean
    suspend fun completeInterruptedRestore(): RepositoryResult<Unit>
}
