package com.crossbowffs.quotelock.backup

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import com.crossbowffs.quotelock.app.App
import com.crossbowffs.quotelock.collections.database.QuoteCollectionContract
import com.crossbowffs.quotelock.collections.database.QuoteCollectionDatabase
import com.crossbowffs.quotelock.collections.database.quoteCollectionDatabase
import com.crossbowffs.quotelock.utils.fromFile
import com.crossbowffs.quotelock.utils.ioScope
import com.crossbowffs.quotelock.utils.toFile
import com.yubyf.quotelockx.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Reference: [Database-Backup-Restore](https://github.com/prof18/Database-Backup-Restore/blob/master/app/src/main/java/com/prof/dbtest/backup/LocalBackup.java)
 *
 * @author Yubyf
 */
object LocalBackup {
    private val PERMISSIONS_STORAGE = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    const val REQUEST_CODE_PERMISSIONS_BACKUP = 55
    const val REQUEST_CODE_PICK_FILE = 43

    val PREF_BACKUP_ROOT_DIR: File =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val PREF_BACKUP_RELATIVE_PATH = App.INSTANCE.resources.getString(R.string.quotelockx)

    /** Check necessary permissions.  */
    private fun verifyPermissions(activity: Activity, requestCode: Int): Boolean {
        // Check if we have write permission
        return if (ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                activity,
                PERMISSIONS_STORAGE,
                requestCode
            )
            false
        } else true
    }

    fun handleRequestPermissionsResult(
        grantResults: IntArray, callback: ProgressCallback,
        action: Runnable,
    ) {
        if (grantResults.size < 2 || grantResults[0] != PackageManager.PERMISSION_GRANTED
            || grantResults[1] != PackageManager.PERMISSION_GRANTED
        ) {
            callback.failure("Please grant external storage permission and retry.")
            return
        }
        action.run()
    }

    /** Ask the user a name for the backup and perform it.
     *  The backup will be saved to a custom folder in [Environment.DIRECTORY_DOWNLOADS].
     */
    fun performBackup(activity: Activity, databaseName: String, callback: ProgressCallback) {
        // Use MediaStore to save files in public directories above Android Q.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
            && !verifyPermissions(activity, REQUEST_CODE_PERMISSIONS_BACKUP)
        ) {
            callback.failure("Please grant external storage permission and retry.")
            return
        }
        ioScope.launch {
            try {
                backupDb(activity, databaseName)
                callback.safeSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                callback.safeFailure(e.message)
            }
        }
    }

    /** Ask the user which backup to restore from. */
    fun performRestore(
        activity: Activity,
        databaseName: String,
        pickedUri: Uri,
        callback: ProgressCallback,
    ) {
        ioScope.launch scope@{
            try {
                importDb(activity, databaseName, pickedUri)
                callback.safeSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                callback.safeFailure(e.message)
            }
        }
    }

    @Throws(Exception::class)
    private fun backupDb(context: Context, databaseName: String): Boolean {
        //database path
        val inFileName = context.getDatabasePath(databaseName).toString()
        return try {
            val dbFile = File(inFileName)
            copyFileToDownloads(context, dbFile) != null
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Unable to backup database. Retry")
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(Exception::class)
    private suspend fun importDb(context: Context, databaseName: String, fileUri: Uri): Boolean {
        val temporaryDatabaseFile = File(context.cacheDir, databaseName)
        return try {
            context.contentResolver.openInputStream(fileUri)?.toFile(temporaryDatabaseFile)
                ?: throw Exception()
            importCollectionDatabaseFrom(context, temporaryDatabaseFile)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Unable to import database. Retry")
        }
    }

    @SuppressLint("Range")
    private fun copyFileToDownloads(context: Context, file: File): Uri? {
        // Generate export name with timestamp
        val date = Date(System.currentTimeMillis())
        val simpleDateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        val exportName = file.name.replace(file.nameWithoutExtension,
            file.nameWithoutExtension.plus("_").plus(simpleDateFormat.format(date)))

        // Copy file
        val resolver = context.contentResolver
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Adapt the scope storage above Android Q by MediaStore.
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS + File.separatorChar + PREF_BACKUP_RELATIVE_PATH)
                put(MediaStore.MediaColumns.DISPLAY_NAME, exportName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.sqlite3")
            }
            resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        } else {
            // Save file through File API on Android P-.
            val targetFile = File(File(PREF_BACKUP_ROOT_DIR, PREF_BACKUP_RELATIVE_PATH), exportName)
            targetFile.parentFile?.mkdirs()
            Uri.fromFile(targetFile)
        }?.also { resolver.openOutputStream(it)?.fromFile(file.absoluteFile) }
    }
}

/**
 * Import collection database from .db file by replacing data contents.
 */
@Throws(NoSuchElementException::class)
suspend fun importCollectionDatabaseFrom(context: Context, file: File) {
    withContext(Dispatchers.IO) {
        val temporaryCollectionDatabase =
            QuoteCollectionDatabase.openTemporaryDatabaseFrom(context,
                "${QuoteCollectionContract.DATABASE_NAME}_${System.currentTimeMillis()}", file)
        val collections = temporaryCollectionDatabase.dao().getAll().first()
        quoteCollectionDatabase.dao().clear()
        if (collections.isNotEmpty()) {
            quoteCollectionDatabase.dao().insert(collections)
        }
        temporaryCollectionDatabase.close()
    }
}