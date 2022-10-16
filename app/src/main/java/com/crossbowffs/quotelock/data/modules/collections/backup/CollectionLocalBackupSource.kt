package com.crossbowffs.quotelock.data.modules.collections.backup

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.crossbowffs.quotelock.consts.PREF_PUBLIC_RELATIVE_PATH
import com.crossbowffs.quotelock.data.AsyncResult
import com.crossbowffs.quotelock.data.api.AndroidString
import com.crossbowffs.quotelock.data.modules.collections.database.QuoteCollectionContract
import com.crossbowffs.quotelock.data.modules.collections.database.QuoteCollectionDao
import com.crossbowffs.quotelock.data.modules.collections.database.QuoteCollectionDatabase
import com.crossbowffs.quotelock.data.modules.collections.database.QuoteCollectionEntity
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.className
import com.crossbowffs.quotelock.utils.copyFileToDownloads
import com.crossbowffs.quotelock.utils.toFile
import com.opencsv.bean.CsvToBeanBuilder
import com.opencsv.bean.StatefulBeanToCsvBuilder
import com.yubyf.quotelockx.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader

/**
 * Reference: [Database-Backup-Restore](https://github.com/prof18/Database-Backup-Restore/blob/master/app/src/main/java/com/prof/dbtest/backup/LocalBackup.java)
 */
class CollectionLocalBackupSource internal constructor(
    private val context: Context,
    private val collectionDao: QuoteCollectionDao,
) {

    private val _cacheDir = context.cacheDir

    /**
     *  Ask the user a name for the export database file and perform it.
     *  The export file will be saved to a custom folder in [Environment.DIRECTORY_DOWNLOADS].
     */
    fun exportDb(databaseName: String): AsyncResult<String> = runCatching {
        //database path
        val inFileName = context.getDatabasePath(databaseName).toString()
        AsyncResult.Success(copyFileToDownloads(File(inFileName)))
    }.onFailure {
        Xlog.e(TAG, "Failed to export database: $it")
    }
        .getOrDefault(
            AsyncResult.Error.Message(
                AndroidString.StringRes(R.string.unable_to_export_database)
            )
        )

    /**
     *  Ask the user a name for the export csv file and perform it.
     *  The export file will be saved to a custom folder in [Environment.DIRECTORY_DOWNLOADS].
     */
    @Throws(Exception::class)
    suspend fun exportCsv(databaseName: String): AsyncResult<String> = runCatching {
        val csvFile =
            File(_cacheDir, File(databaseName).nameWithoutExtension.plus(".csv"))
        val collections = collectionDao.getAll()
        FileWriter(csvFile).use {
            val beanToCsv =
                StatefulBeanToCsvBuilder<QuoteCollectionEntity>(it).build()
            beanToCsv.write(collections)
        }
        AsyncResult.Success(copyFileToDownloads(csvFile))
    }.onFailure {
        Xlog.e(TAG, "Failed to export CSV: $it")
    }.getOrDefault(
        AsyncResult.Error.Message(AndroidString.StringRes(R.string.unable_to_export_csv))
    )

    /** Ask the user which file to import from. */
    @SuppressLint("Recycle")
    suspend fun importDb(
        databaseName: String, fileUri: Uri,
        merge: Boolean = false,
    ): AsyncResult<Unit> = runCatching {
        val temporaryDatabaseFile = File(_cacheDir, databaseName)
        context.contentResolver.openInputStream(fileUri)
            ?.toFile(temporaryDatabaseFile)
            ?: throw Exception()
        if (importCollectionDatabaseFrom(temporaryDatabaseFile, merge)) {
            AsyncResult.Success(Unit)
        } else {
            throw Exception("Open database failed")
        }
    }.onFailure {
        Xlog.e(TAG, "Failed to import database: $it")
    }.getOrDefault(
        AsyncResult.Error.Message(AndroidString.StringRes(R.string.unable_to_import_database))
    )

    /** Ask the user which file to import from. */
    suspend fun importCsv(
        fileUri: Uri,
        merge: Boolean = false,
    ): AsyncResult<Unit> = runCatching {
        val collections =
            context.contentResolver.openInputStream(fileUri)?.use {
                CsvToBeanBuilder<QuoteCollectionEntity>(InputStreamReader(it))
                    .withType(QuoteCollectionEntity::class.java).build().parse()
            } ?: throw Exception()
        if (collections.isNotEmpty()) {
            if (!merge) {
                collectionDao.clear()
            }
            collectionDao.insert(collections)
            AsyncResult.Success(Unit)
        } else {
            throw Exception("Empty collections")
        }
    }.onFailure {
        Xlog.e(TAG, "Failed to import CSV: $it")
    }.getOrDefault(
        AsyncResult.Error.Message(AndroidString.StringRes(R.string.unable_to_import_csv))
    )

    /**
     * Import collection database from .db file by replacing data contents.
     */
    @Throws(NoSuchElementException::class)
    suspend fun importCollectionDatabaseFrom(
        file: File,
        merge: Boolean,
    ): Boolean {
        return withContext(Dispatchers.IO) {
            val temporaryCollectionDatabase =
                QuoteCollectionDatabase.openTemporaryDatabaseFrom(
                    context,
                    "${QuoteCollectionContract.DATABASE_NAME}_${System.currentTimeMillis()}", file
                )
            val collections = temporaryCollectionDatabase.dao().getAll()
            if (collections.isNotEmpty()) {
                if (!merge) {
                    collectionDao.clear()
                }
                collectionDao.insert(collections.map { it.copy(id = null) })
            }
            temporaryCollectionDatabase.close()
            collections.isNotEmpty()
        }
    }

    @SuppressLint("Range")
    private fun copyFileToDownloads(file: File): String {
        return context.copyFileToDownloads(file, PREF_PUBLIC_RELATIVE_PATH)
    }

    companion object {
        private val TAG = className<CollectionLocalBackupSource>()
    }
}