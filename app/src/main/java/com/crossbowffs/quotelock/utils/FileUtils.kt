package com.crossbowffs.quotelock.utils

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.util.Pair
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author Yubyf
 */

@SuppressLint("Range")
fun Context.copyFileToDownloads(file: File, relativePath: String): String {
    // Generate export name with timestamp
    val date = Date(System.currentTimeMillis())
    val simpleDateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
    val exportName = file.name.replace(file.nameWithoutExtension,
        file.nameWithoutExtension.plus("_").plus(simpleDateFormat.format(date)))

    // Copy file
    val resolver = contentResolver
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // Adapt the scope storage above Android Q by MediaStore.
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS + File.separatorChar + relativePath)
            put(MediaStore.MediaColumns.DISPLAY_NAME, exportName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.sqlite3")
        }
        resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
    } else {
        // Save file through File API on Android P-.
        val targetFile =
            File(File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                relativePath), exportName)
        targetFile.parentFile?.mkdirs()
        Uri.fromFile(targetFile)
    }?.also { resolver.openOutputStream(it)?.fromFile(file.absoluteFile) }
        ?: throw IOException()
    return Environment.DIRECTORY_DOWNLOADS.plus(File.separatorChar)
        .plus(relativePath).plus(File.separatorChar).plus(exportName)
}


/**
 * Get the md5 checksum and last modification time of given database file.
 */
fun Context.getDatabaseInfo(databaseName: String?): Pair<String?, Long?> {
    // database path
    val databaseFilePath = getDatabasePath(databaseName).toString()
    val dbFile = File(databaseFilePath)
    return if (!dbFile.exists()) {
        Pair(null, null)
    } else try {
        Pair(dbFile.md5String(), dbFile.lastModified())
    } catch (e: Exception) {
        e.printStackTrace()
        Pair(null, null)
    }
}