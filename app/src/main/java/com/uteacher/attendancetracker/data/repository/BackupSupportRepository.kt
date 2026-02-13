package com.uteacher.attendancetracker.data.repository

import android.net.Uri
import com.uteacher.attendancetracker.data.repository.internal.RepositoryResult

interface BackupSupportRepository {
    suspend fun exportBackup(): RepositoryResult<String>
    suspend fun importBackup(sourceUri: Uri): RepositoryResult<Unit>
    suspend fun checkAndRecoverInterruptedRestore(): RepositoryResult<Unit>
}
