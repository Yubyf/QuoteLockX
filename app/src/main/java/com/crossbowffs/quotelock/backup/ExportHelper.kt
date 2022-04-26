package com.crossbowffs.quotelock.backup

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import com.crossbowffs.quotelock.app.App
import com.crossbowffs.quotelock.collections.database.QuoteCollectionContract
import com.crossbowffs.quotelock.collections.database.QuoteCollectionDatabase
import com.crossbowffs.quotelock.collections.database.QuoteCollectionEntity
import com.crossbowffs.quotelock.collections.database.quoteCollectionDatabase
import com.crossbowffs.quotelock.utils.*
import com.opencsv.bean.CsvToBeanBuilder
import com.opencsv.bean.StatefulBeanToCsvBuilder
import com.yubyf.quotelockx.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*


@Retention(AnnotationRetention.SOURCE)
annotation class ImportExportType {
    companion object {
        const val DB = 0
        const val CSV = 1
    }
}

/**
 * Reference: [Database-Backup-Restore](https://github.com/prof18/Database-Backup-Restore/blob/master/app/src/main/java/com/prof/dbtest/backup/LocalBackup.java)
 */
object ExportHelper {
    private val PERMISSIONS_STORAGE = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    const val REQUEST_CODE_PERMISSIONS_EXPORT = 55
    const val REQUEST_CODE_PICK_CSV_FILE = 42
    const val REQUEST_CODE_PICK_DB_FILE = 43

    private val TAG = className<ExportHelper>()

    val PREF_EXPORT_ROOT_DIR: File =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val PREF_EXPORT_RELATIVE_PATH = App.INSTANCE.let {
        // Get english application name for the default export path
        it.createConfigurationContext(Configuration(it.resources.configuration).apply {
            setLocale(Locale.ENGLISH)
        }).resources.getString(R.string.quotelockx)
    }

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

    fun handleRequestPermissionsResult(grantResults: IntArray, block: ((failMsg: String) -> Unit)) {
        if (grantResults.size < 2 || grantResults[0] != PackageManager.PERMISSION_GRANTED
            || grantResults[1] != PackageManager.PERMISSION_GRANTED
        ) {
            block.invoke(App.INSTANCE.getString(R.string.grant_storage_permission_tips))
        }
    }

    /**
     *  Ask the user a name for the export file and perform it.
     *  The export file will be saved to a custom folder in [Environment.DIRECTORY_DOWNLOADS].
     */
    fun performExport(
        activity: Activity,
        databaseName: String,
        @ImportExportType exportType: Int,
        action: (success: Boolean, message: String) -> Unit,
    ) {
        // Use MediaStore to save files in public directories above Android Q.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
            && !verifyPermissions(activity, REQUEST_CODE_PERMISSIONS_EXPORT)
        ) {
            action.invoke(false, activity.getString(R.string.grant_storage_permission_tips))
            return
        }
        ioScope.launch {
            try {
                val path = if (exportType == ImportExportType.CSV) {
                    exportCsv(activity, databaseName)
                } else {
                    exportDb(activity, databaseName)
                }
                withContext(Dispatchers.Main) { action.invoke(true, path) }
            } catch (e: Exception) {
                Xlog.e(TAG, "Failed to export database: $e")
                withContext(Dispatchers.Main) { action.invoke(false, e.message ?: "") }
            }
        }
    }

    /** Ask the user which file to import from. */
    fun performImport(
        activity: Activity,
        databaseName: String,
        pickedUri: Uri,
        @ImportExportType importType: Int,
        action: (success: Boolean, message: String) -> Unit,
    ) {
        ioScope.launch scope@{
            try {
                if (importType == ImportExportType.CSV) {
                    importCsv(activity, pickedUri)
                } else {
                    importDb(activity, databaseName, pickedUri)
                }
                withContext(Dispatchers.Main) { action.invoke(true, "") }
            } catch (e: Exception) {
                Xlog.e(TAG, "Failed to import database: $e")
                withContext(Dispatchers.Main) { action.invoke(false, e.message ?: "") }
            }
        }
    }

    @Throws(Exception::class)
    private fun exportDb(context: Context, databaseName: String): String {
        //database path
        val inFileName = context.getDatabasePath(databaseName).toString()
        return try {
            val dbFile = File(inFileName)
            copyFileToDownloads(context, dbFile)
        } catch (e: Exception) {
            Xlog.e(TAG, "Failed to export database: $e")
            throw Exception(context.getString(R.string.unable_to_export_database))
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(Exception::class)
    private suspend fun exportCsv(context: Context, databaseName: String): String {
        return try {
            val csvFile =
                File(context.cacheDir, File(databaseName).nameWithoutExtension.plus(".csv"))
            val collections = quoteCollectionDatabase.dao().getAll().first()
            FileWriter(csvFile).use {
                val beanToCsv =
                    StatefulBeanToCsvBuilder<QuoteCollectionEntity>(it).build()
                beanToCsv.write(collections)
            }
            copyFileToDownloads(context, csvFile)
        } catch (e: Exception) {
            Xlog.e(TAG, "Failed to export CSV: $e")
            throw Exception(context.getString(R.string.unable_to_export_csv))
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(Exception::class)
    private suspend fun importDb(context: Context, databaseName: String, fileUri: Uri): Boolean {
        val temporaryDatabaseFile = File(context.cacheDir, databaseName)
        return try {
            context.contentResolver.openInputStream(fileUri)?.toFile(temporaryDatabaseFile)
                ?: throw Exception()
            if (importCollectionDatabaseFrom(context, temporaryDatabaseFile)) {
                true
            } else {
                throw Exception("Open database failed")
            }
        } catch (e: Exception) {
            Xlog.e(TAG, "Failed to import database: $e")
            throw Exception(context.getString(R.string.unable_to_import_database))
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(Exception::class)
    private suspend fun importCsv(context: Context, fileUri: Uri): Boolean {
        return try {
            val collections =
                context.contentResolver.openInputStream(fileUri)?.let {
                    CsvToBeanBuilder<QuoteCollectionEntity>(InputStreamReader(it))
                        .withType(QuoteCollectionEntity::class.java).build().parse()
                } ?: throw Exception()
            quoteCollectionDatabase.dao().clear()
            if (collections.isNotEmpty()) {
                quoteCollectionDatabase.dao().insert(collections)
            }
            true
        } catch (e: Exception) {
            Xlog.e(TAG, "Failed to import CSV: $e")
            throw Exception(context.getString(R.string.unable_to_import_csv))
        }
    }

    @SuppressLint("Range")
    private fun copyFileToDownloads(context: Context, file: File): String {
        // Generate export name with timestamp
        val date = Date(System.currentTimeMillis())
        val simpleDateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        val exportName = file.name.replace(file.nameWithoutExtension,
            file.nameWithoutExtension.plus("_").plus(simpleDateFormat.format(date)))

        // Copy file
        val resolver = context.contentResolver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Adapt the scope storage above Android Q by MediaStore.
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS + File.separatorChar + PREF_EXPORT_RELATIVE_PATH)
                put(MediaStore.MediaColumns.DISPLAY_NAME, exportName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.sqlite3")
            }
            resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        } else {
            // Save file through File API on Android P-.
            val targetFile = File(File(PREF_EXPORT_ROOT_DIR, PREF_EXPORT_RELATIVE_PATH), exportName)
            targetFile.parentFile?.mkdirs()
            Uri.fromFile(targetFile)
        }?.also { resolver.openOutputStream(it)?.fromFile(file.absoluteFile) }
            ?: throw IOException()
        return Environment.DIRECTORY_DOWNLOADS.plus(File.separatorChar)
            .plus(PREF_EXPORT_RELATIVE_PATH).plus(File.separatorChar).plus(exportName)
    }
}

/**
 * Import collection database from .db file by replacing data contents.
 */
@Throws(NoSuchElementException::class)
suspend fun importCollectionDatabaseFrom(context: Context, file: File): Boolean {
    return withContext(Dispatchers.IO) {
        val temporaryCollectionDatabase =
            QuoteCollectionDatabase.openTemporaryDatabaseFrom(context,
                "${QuoteCollectionContract.DATABASE_NAME}_${System.currentTimeMillis()}", file)
        val collections = temporaryCollectionDatabase.dao().getAll().first()
        if (collections.isNotEmpty()) {
            quoteCollectionDatabase.dao().clear()
            quoteCollectionDatabase.dao().insert(collections)
        }
        temporaryCollectionDatabase.close()
        collections.isNotEmpty()
    }
}