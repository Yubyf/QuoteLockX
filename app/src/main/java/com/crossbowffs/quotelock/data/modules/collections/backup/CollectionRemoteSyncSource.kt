/*
 *   Copyright 2016 Marco Gomiero
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package com.crossbowffs.quotelock.data.modules.collections.backup

import android.content.Context
import androidx.core.util.Pair
import com.crossbowffs.quotelock.account.google.GoogleAccountHelper.getGoogleAccount
import com.crossbowffs.quotelock.data.AsyncResult
import com.crossbowffs.quotelock.di.IoDispatcher
import com.crossbowffs.quotelock.di.ResourceProvider
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.toFile
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.InputStreamContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.yubyf.quotelockx.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.*
import java.security.DigestInputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author Yubyf
 */
@Singleton
class CollectionRemoteSyncSource @Inject internal constructor(
    @ApplicationContext private val context: Context,
    private val localBackupSource: CollectionLocalBackupSource,
    private val resourceProvider: ResourceProvider,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) {
    private lateinit var drive: Drive

    fun updateDriveService() {
        getGoogleAccount(context).also {
            // Use the authenticated account to sign in to the Drive service.
            val credential = GoogleAccountCredential.usingOAuth2(
                context, setOf(DriveScopes.DRIVE_FILE))
            credential.selectedAccount = it.account
            drive = Drive.Builder(
                NetHttpTransport(),
                GsonFactory(),
                credential)
                .setApplicationName(context.getString(R.string.quotelockx))
                .build()
        }
    }

    fun ensureDriveService(): Boolean {
        if (::drive.isInitialized) {
            return true
        }
        updateDriveService()
        return ::drive.isInitialized
    }

    /**
     * Creates a text file in the user's My Drive folder and returns its file ID.
     */
    private fun Drive.createFileSync(name: String?): String? {
        return try {
            val metadata = File()
                .setParents(listOf("root"))
                .setMimeType("application/vnd.sqlite3")
                .setName(name)
            files().create(metadata).execute()?.id
                ?: throw IOException("Null result when requesting file creation.")
        } catch (e: Exception) {
            Xlog.e(TAG, "Couldn't create file.", e)
            null
        }
    }

    /**
     * Returns a [FileList] containing all the visible files in the user's My Drive.
     *
     *
     * The returned list will only contain files visible to this app, i.e. those which were
     * created by this app. To perform operations on files not created by the app, the project must
     * request Drive Full Scope in the [Google
 * Developer's Console](https://play.google.com/apps/publish) and be submitted to Google for verification.
     */
    private fun Drive.queryFilesSync(): FileList? {
        return try {
            files().list().setSpaces("drive").execute()
        } catch (e: IOException) {
            Xlog.e(TAG, "Unable to query files.", e)
            null
        }
    }

    /**
     * Opens the file identified by `fileId` and returns a [Pair] of its name and
     * stream.
     */
    @Throws(IOException::class)
    fun Drive.readFile(fileId: String?): Pair<Result, InputStream> {
        // Retrieve the metadata as a File object.
        val metadata = files()[fileId].setFields(NEEDED_FILE_FIELDS).execute()
        val result = Result().apply {
            success = true
            md5 = metadata.md5Checksum
            timestamp = if (metadata.modifiedTime == null) -1 else metadata.modifiedTime.value
        }

        // Get the stream of file.
        return Pair.create(result, files()[fileId].executeMediaAsInputStream())
    }

    /**
     * Import the file identified by `fileId`.
     */
    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun Drive.importDbFileSync(
        fileId: String?,
        databaseName: String,
    ): Result {
        return try {
            val temporaryDatabaseFile = File(context.cacheDir, databaseName)
            val digest = MessageDigest.getInstance("MD5")
            val readFileResult = readFile(fileId)
            readFileResult.second.use { inputStream ->
                inputStream?.run {
                    DigestInputStream(this, digest).use { it.toFile(temporaryDatabaseFile) }
                } ?: throw IOException()
            }
            if (!localBackupSource.importCollectionDatabaseFrom(context, temporaryDatabaseFile)) {
                throw Exception("Open database failed")
            }
            readFileResult.first
        } catch (e: Exception) {
            Xlog.e(TAG, "Unable to read file via REST.", e)
            Result()
        }
    }

    /**
     * Updates the file identified by `fileId` with the given `name`.
     */
    private fun Drive.saveFileSync(fileId: String?, name: String?): Result {
        try {
            //database path
            val inFileName = context.getDatabasePath(name).toString()
            val dbFile = File(inFileName)
            val fis = FileInputStream(dbFile)

            // Create a File containing any metadata changes.
            val metadata = File().setName(name)

            // Convert content to an InputStreamContent instance.
            val contentStream = InputStreamContent("application/vnd.sqlite3", fis)

            // Update the metadata and contents.
            val remoteFile = files().update(fileId, metadata, contentStream)
                .setFields(NEEDED_FILE_FIELDS).execute()
            return Result().apply {
                success = true
                md5 = remoteFile.md5Checksum
                timestamp =
                    if (remoteFile.modifiedTime == null) -1 else remoteFile.modifiedTime.value
            }
        } catch (e: Exception) {
            Xlog.e(TAG, "Unable to save file via REST.", e)
            return Result()
        }
    }

    suspend fun performDriveBackup(
        databaseName: String,
    ): Flow<AsyncResult<String>> = flow {
        drive.run {
            emit(AsyncResult.Loading(
                resourceProvider.getString(R.string.google_drive_querying_backup_file)))
            val queryFiles = withContext(dispatcher) { queryFilesSync() }
            queryFiles ?: run {
                emit(AsyncResult.Error(
                    Exception(resourceProvider.getString(R.string.google_drive_query_failed))))
                return@flow
            }
            val databaseFile = queryFiles.files.find { it.name == databaseName }
            databaseFile?.run {
                emit(AsyncResult.Loading(
                    resourceProvider.getString(R.string.google_drive_uploading_file)))
                if (withContext(dispatcher) { saveFileSync(id, databaseName) }.success) {
                    emit(AsyncResult.Success(""))
                } else {
                    emit(AsyncResult.Error(
                        Exception(resourceProvider.getString(R.string.google_drive_save_failed))))
                }
                return@flow
            }
            emit(AsyncResult.Loading(
                resourceProvider.getString(R.string.google_drive_creating_file)))
            if (withContext(dispatcher) { createFileSync(databaseName) }.isNullOrBlank()) {
                emit(AsyncResult.Success(""))
            } else {
                emit(AsyncResult.Error(
                    Exception(resourceProvider.getString(R.string.google_drive_create_failed))))
            }
        }
    }

    fun performSafeDriveBackupSync(databaseName: String): Result {
        val result = Result()
        return if (!ensureDriveService()) {
            result
        } else try {
            val fileList = drive.queryFilesSync() ?: return result
            var fileId: String? = null
            for (file in fileList.files) {
                if (file.name == databaseName) {
                    fileId = file.id
                    break
                }
            }
            if (fileId == null) {
                fileId = drive.createFileSync(databaseName)
            }
            return drive.saveFileSync(fileId, databaseName)
        } catch (e: Exception) {
            Xlog.e(TAG, "Unable to backup files.", e)
            result
        }
    }

    suspend fun performDriveRestore(
        databaseName: String,
    ): Flow<AsyncResult<String>> = flow {
        drive.run {
            emit(AsyncResult.Loading(
                resourceProvider.getString(R.string.google_drive_querying_backup_file)))
            val queryFiles = withContext(dispatcher) { queryFilesSync() }
            queryFiles ?: run {
                emit(AsyncResult.Error(
                    Exception(resourceProvider.getString(R.string.google_drive_query_failed))))
                return@flow
            }
            val databaseFile = queryFiles.files.find { it.name == databaseName }
            databaseFile?.run {
                emit(AsyncResult.Loading(
                    resourceProvider.getString(R.string.google_drive_importing_file)))
                if (withContext(dispatcher) { importDbFileSync(id, databaseName) }.success) {
                    emit(AsyncResult.Success(""))
                } else {
                    emit(AsyncResult.Error(
                        Exception(resourceProvider.getString(R.string.google_drive_read_failed))))
                }
                return@flow
            }
            Xlog.e(TAG, "There is no $databaseName on drive")
            emit(AsyncResult.Error(
                Exception(resourceProvider.getString(R.string.google_drive_no_existing_file))))
        }
    }

    fun performSafeDriveRestoreSync(databaseName: String): Result {
        val result = Result()
        if (!ensureDriveService()) {
            return result
        }
        try {
            val fileList = drive.queryFilesSync() ?: return result
            for (file in fileList.files) {
                if (file.name == databaseName) {
                    return runBlocking { drive.importDbFileSync(file.id, databaseName) }
                }
            }
        } catch (e: IOException) {
            Xlog.e(TAG, "Unable to restore files.", e)
        } catch (e: NoSuchAlgorithmException) {
            Xlog.e(TAG, "Unable to restore files.", e)
        }
        return result
    }

    /**
     * @author Yubyf
     * @date 2021/8/15.
     */
    class Result(var success: Boolean = false, var md5: String? = null, var timestamp: Long = -1L) {
        override fun toString(): String {
            return "Result{" +
                    "success=" + success +
                    ", md5='" + md5 + '\'' +
                    ", timestamp=" + timestamp +
                    '}'
        }
    }

    companion object {
        private const val TAG = "RemoteBackup"
        private const val NEEDED_FILE_FIELDS = "md5Checksum,name,modifiedTime"
    }
}