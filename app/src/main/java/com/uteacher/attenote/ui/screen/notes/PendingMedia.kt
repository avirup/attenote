package com.uteacher.attenote.ui.screen.notes

import android.net.Uri

data class PendingMedia(
    val uri: Uri,
    val localPath: String? = null
)
