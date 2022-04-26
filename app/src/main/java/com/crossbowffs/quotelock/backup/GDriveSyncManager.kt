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
package com.crossbowffs.quotelock.backup

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.util.Pair
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.ioScope
import com.crossbowffs.quotelock.utils.md5String
import com.crossbowffs.quotelock.utils.toFile
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.InputStreamContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.yubyf.quotelockx.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.*
import java.security.DigestInputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


/**
 * @author Yubyf
 */
class GDriveSyncManager {
    private lateinit var drive: Drive
    private val scope: Scope by lazy { Scope(DriveScopes.DRIVE_FILE) }

    fun checkGooglePlayService(context: Context): Boolean {
        return GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
    }

    private fun checkGoogleAccount(activity: Activity, requestCode: Int): Boolean {
        return if (!ensureDriveService(activity)) {
            requestSignIn(activity, requestCode)
            false
        } else true
    }

    private fun ensureDriveService(context: Context): Boolean {
        if (::drive.isInitialized) {
            return true
        }
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
        return ::drive.isInitialized
    }

    private fun getGoogleAccount(context: Context): GoogleSignInAccount =
        GoogleSignIn.getAccountForScopes(context, scope)

    fun isGoogleAccountSignedIn(context: Context): Boolean {
        return checkGooglePlayService(context)
                && GoogleSignIn.hasPermissions(getGoogleAccount(context), scope)
    }

    fun getSignedInGoogleAccountEmail(context: Context): String? {
        return getGoogleAccount(context).email
    }

    fun getSignedInGoogleAccountPhoto(context: Context): Uri? {
        return getGoogleAccount(context).photoUrl
    }

    /**
     * Starts a sign-in activity using [.REQUEST_CODE_SIGN_IN],
     * [.REQUEST_CODE_SIGN_IN_BACKUP] or [.REQUEST_CODE_SIGN_IN_RESTORE].
     */
    fun requestSignIn(activity: Activity, code: Int) {
        Xlog.d(TAG, "Requesting sign-in")
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestProfile()
            .requestEmail()
            .requestScopes(scope)
            .build()
        val client = GoogleSignIn.getClient(activity, signInOptions)

        // The result of the sign-in Intent is handled in onActivityResult.
        activity.startActivityForResult(client.signInIntent, code)
    }

    fun signOutAccount(
        activity: Activity,
        resultAction: (success: Boolean, message: String) -> Unit,
    ) {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        val client = GoogleSignIn.getClient(activity, signInOptions)
        val signedEmail = getSignedInGoogleAccountEmail(activity)
        client.signOut().addOnCompleteListener {
            resultAction.invoke(true, signedEmail ?: "")
        }.addOnFailureListener {
            resultAction.invoke(false, it.message ?: "")
        }
    }

    /**
     * Handles the `result` of a completed sign-in activity initiated from [ ][.requestSignIn].
     */
    fun handleSignInResult(
        activity: Activity,
        result: Intent?,
        resultAction: (success: Boolean, message: String) -> Unit,
    ) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
            .addOnSuccessListener {
                Xlog.d(TAG, "Signed in as " + it.email)

                // Use the authenticated account to sign in to the Drive service.
                val credential = GoogleAccountCredential.usingOAuth2(
                    activity, setOf(DriveScopes.DRIVE_FILE))
                credential.selectedAccount = it.account
                drive = Drive.Builder(
                    NetHttpTransport(),
                    GsonFactory(),
                    credential)
                    .setApplicationName(activity.getString(R.string.quotelockx))
                    .build()
                resultAction.invoke(true, "")
            }
            .addOnFailureListener {
                Xlog.e(TAG, "Unable to sign in.", it)
                resultAction.invoke(false,
                    activity.getString(R.string.google_account_sign_in_failed))
            }
    }

    /**
     * Get the md5 checksum and last modification time of given database file.
     */
    fun getDatabaseInfo(context: Context, databaseName: String?): Pair<String?, Long?> {
        // database path
        val databaseFilePath = context.getDatabasePath(databaseName).toString()
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
        context: Context,
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
            if (!importCollectionDatabaseFrom(context, temporaryDatabaseFile)) {
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
    private fun Drive.saveFileSync(context: Context, fileId: String?, name: String?): Result {
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

    fun performDriveBackupAsync(
        activity: Activity,
        databaseName: String,
        progressAction: ((String) -> Unit),
        resultAction: (success: Boolean, message: String) -> Unit,
    ) {
        if (!checkGoogleAccount(activity, REQUEST_CODE_SIGN_IN_BACKUP)) {
            return
        }
        drive.run {
            ioScope.launch scope@{
                withContext(Dispatchers.Main) {
                    progressAction.invoke(activity.getString(R.string.google_drive_querying_backup_file))
                }
                val queryFiles = queryFilesSync()
                queryFiles ?: run {
                    withContext(Dispatchers.Main) {
                        resultAction(false, activity.getString(R.string.google_drive_query_failed))
                    }
                    return@scope
                }
                val databaseFile = queryFiles.files.find { it.name == databaseName }
                databaseFile?.run {
                    withContext(Dispatchers.Main) {
                        progressAction.invoke(activity.getString(R.string.google_drive_uploading_file))
                    }
                    val saveResult = saveFileSync(activity, id, databaseName)
                    withContext(Dispatchers.Main) {
                        if (saveResult.success) {
                            resultAction(true, "")
                        } else {
                            resultAction(false,
                                activity.getString(R.string.google_drive_save_failed))
                        }
                    }
                    return@scope
                }
                withContext(Dispatchers.Main) {
                    progressAction.invoke(activity.getString(R.string.google_drive_creating_file))
                }
                val createFileResult = createFileSync(databaseName)
                withContext(Dispatchers.Main) {
                    if (createFileResult == null) {
                        resultAction(true, "")
                    } else {
                        resultAction(false, activity.getString(R.string.google_drive_create_failed))
                    }
                }
            }
        }
    }

    fun performSafeDriveBackupSync(context: Context, databaseName: String): Result {
        val result = Result()
        return if (!ensureDriveService(context)) {
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
            return drive.saveFileSync(context, fileId, databaseName)
        } catch (e: Exception) {
            Xlog.e(TAG, "Unable to backup files.", e)
            result
        }
    }

    fun performDriveRestoreAsync(
        activity: Activity,
        databaseName: String,
        progressAction: ((String) -> Unit),
        resultAction: (success: Boolean, message: String) -> Unit,
    ) {
        if (!checkGoogleAccount(activity, REQUEST_CODE_SIGN_IN_RESTORE)) {
            return
        }
        drive.run {
            ioScope.launch scope@{
                withContext(Dispatchers.Main) {
                    progressAction.invoke(activity.getString(R.string.google_drive_querying_backup_file))
                }
                val queryFiles = queryFilesSync()
                queryFiles ?: run {
                    withContext(Dispatchers.Main) {
                        resultAction(false, activity.getString(R.string.google_drive_query_failed))
                    }
                    return@scope
                }
                val databaseFile = queryFiles.files.find { it.name == databaseName }
                databaseFile?.run {
                    withContext(Dispatchers.Main) {
                        progressAction.invoke(activity.getString(R.string.google_drive_importing_file))
                    }
                    val importResult = importDbFileSync(activity, id, databaseName)
                    withContext(Dispatchers.Main) {
                        if (importResult.success) {
                            resultAction(true, "")
                        } else {
                            resultAction(false,
                                activity.getString(R.string.google_drive_read_failed))
                        }
                    }
                    return@scope
                }
                Xlog.e(TAG, "There is no $databaseName on drive")
                withContext(Dispatchers.Main) {
                    resultAction(false, activity.getString(R.string.google_drive_no_existing_file))
                }
            }
        }
    }

    fun performSafeDriveRestoreSync(context: Context, databaseName: String): Result {
        val result = Result()
        if (!ensureDriveService(context)) {
            return result
        }
        try {
            val fileList = drive.queryFilesSync() ?: return result
            for (file in fileList.files) {
                if (file.name == databaseName) {
                    return runBlocking { drive.importDbFileSync(context, file.id, databaseName) }
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
        const val REQUEST_CODE_SIGN_IN = 1
        const val REQUEST_CODE_SIGN_IN_BACKUP = 2
        const val REQUEST_CODE_SIGN_IN_RESTORE = 3
        val INSTANCE = GDriveSyncManager()
    }
}