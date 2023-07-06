package com.crossbowffs.quotelock.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.crossbowffs.quotelock.consts.PREF_SHARE_FILE_AUTHORITY
import java.io.File

fun Context.installApk(path: String) = File(path).takeIf { it.exists() }?.let {
    val fileUri: Uri =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                this,
                PREF_SHARE_FILE_AUTHORITY,
                it
            )
        } else {
            Uri.fromFile(it)
        }
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(fileUri, "application/vnd.android.package-archive")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    applicationContext.startActivity(intent)
}