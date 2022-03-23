package com.crossbowffs.quotelock.backup

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import androidx.core.app.ActivityCompat
import com.crossbowffs.quotelock.R
import com.crossbowffs.quotelock.utils.ioScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * Reference: [Database-Backup-Restore](https://github.com/prof18/Database-Backup-Restore/blob/master/app/src/main/java/com/prof/dbtest/backup/LocalBackup.java)
 *
 * @author Yubyf
 */
object LocalBackup {
    private val PERMISSIONS_STORAGE = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    const val REQUEST_CODE_PERMISSIONS_BACKUP = 55
    const val REQUEST_CODE_PERMISSIONS_RESTORE = 43

    /** check permissions.  */
    private fun verifyPermissions(activity: Activity?, requestCode: Int): Boolean {
        // Check if we have read or write permission
        val writePermission = ActivityCompat.checkSelfPermission(activity!!,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val readPermission =
            ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
        if (writePermission != PackageManager.PERMISSION_GRANTED
            || readPermission != PackageManager.PERMISSION_GRANTED
        ) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                activity,
                PERMISSIONS_STORAGE,
                requestCode
            )
            return false
        }
        return true
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

    /** ask to the user a name for the backup and perform it. The backup will be saved to a custom folder.  */
    fun performBackup(activity: Activity, databaseName: String, callback: ProgressCallback) {
        if (!verifyPermissions(activity, REQUEST_CODE_PERMISSIONS_BACKUP)) {
            callback.failure("Please grant external storage permission and retry.")
            return
        }
        ioScope.launch {
            val folder = File(Environment.getExternalStorageDirectory()
                .toString() + File.separator + activity.resources.getString(R.string.quotelock))
            var success = true
            if (!folder.exists()) {
                success = folder.mkdirs()
            }
            if (success) {
                val out = folder.absolutePath + File.separator + databaseName
                try {
                    backup(activity, databaseName, out)
                    callback.safeSuccess()
                } catch (e: Exception) {
                    e.printStackTrace()
                    callback.safeFailure(e.message)
                }
            } else {
                callback.safeFailure("Unable to create directory. Retry")
            }
        }
    }

    /** ask to the user what backup to restore  */
    fun performRestore(activity: Activity, databaseName: String, callback: ProgressCallback) {
        if (!verifyPermissions(activity, REQUEST_CODE_PERMISSIONS_RESTORE)) {
            callback.failure("Please grant external storage permission and retry.")
        }
        ioScope.launch scope@{
            val folder = File(Environment.getExternalStorageDirectory()
                .toString() + File.separator + activity.resources.getString(R.string.quotelock))
            if (folder.exists()) {
                val file = File(folder, databaseName)
                if (!file.exists()) {
                    callback.safeFailure("Backup file not exists.\nDo a backup before a restore!")
                    return@scope
                }
                try {
                    importDb(activity, databaseName, file.absolutePath)
                    callback.safeSuccess()
                } catch (e: Exception) {
                    e.printStackTrace()
                    callback.safeFailure(e.message)
                }
            } else {
                callback.safeFailure("Backup folder not present.\nDo a backup before a restore!")
            }
        }
    }

    @Throws(Exception::class)
    private fun backup(context: Context, databaseName: String, outFileName: String): Boolean {
        //database path
        val inFileName = context.getDatabasePath(databaseName).toString()
        return try {
            val dbFile = File(inFileName)
            val fis = FileInputStream(dbFile)

            // Open the empty db as the output stream
            val output: OutputStream = FileOutputStream(outFileName)

            // Transfer bytes from the input file to the output file
            val buffer = ByteArray(1024)
            var length: Int
            while (fis.read(buffer).also { length = it } > 0) {
                output.write(buffer, 0, length)
            }

            // Close the streams
            output.flush()
            output.close()
            fis.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Unable to backup database. Retry")
        }
    }

    @Throws(Exception::class)
    private fun importDb(context: Context, databaseName: String, inFileName: String): Boolean {
        val outFileName = context.getDatabasePath(databaseName).toString()
        return try {
            val dbFile = File(inFileName)
            val fis = FileInputStream(dbFile)

            // Open the empty db as the output stream
            val output: OutputStream = FileOutputStream(outFileName)

            // Transfer bytes from the input file to the output file
            val buffer = ByteArray(1024)
            var length: Int
            while (fis.read(buffer).also { length = it } > 0) {
                output.write(buffer, 0, length)
            }

            // Close the streams
            output.flush()
            output.close()
            fis.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Unable to import database. Retry")
        }
    }
}