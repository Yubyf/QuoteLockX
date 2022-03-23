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
import com.crossbowffs.quotelock.R
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.ioScope
import com.crossbowffs.quotelock.utils.md5String
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.InputStreamContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.launch
import java.io.*
import java.security.DigestInputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


/**
 * @author Yubyf
 */
class RemoteBackup {
    private lateinit var mDriveService: Drive
    private fun checkGoogleAccount(activity: Activity, requestCode: Int): Boolean {
        if (!ensureDriveService(activity)) {
            requestSignIn(activity, requestCode)
            return false
        }
        return true
    }

    private fun ensureDriveService(context: Context): Boolean {
        if (::mDriveService.isInitialized) {
            return true
        }
        val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(context)
        account?.also {
            // Use the authenticated account to sign in to the Drive service.
            val credential = GoogleAccountCredential.usingOAuth2(
                context, setOf(DriveScopes.DRIVE_FILE))
            credential.selectedAccount = it.account
            mDriveService = Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                GsonFactory(),
                credential)
                .setApplicationName(context.getString(R.string.quotelock))
                .build()
        }
        return ::mDriveService.isInitialized
    }

    fun isGoogleAccountSignedIn(context: Context): Boolean {
        return GoogleSignIn.getLastSignedInAccount(context) != null
    }

    fun getSignedInGoogleAccountEmail(context: Context): String? {
        return GoogleSignIn.getLastSignedInAccount(context)?.email
    }

    fun getSignedInGoogleAccountPhoto(context: Context): Uri? {
        return GoogleSignIn.getLastSignedInAccount(context)?.photoUrl
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
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()
        val client = GoogleSignIn.getClient(activity, signInOptions)

        // The result of the sign-in Intent is handled in onActivityResult.
        activity.startActivityForResult(client.signInIntent, code)
    }

    fun switchAccount(activity: Activity, code: Int, callback: ProgressCallback) {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        val client = GoogleSignIn.getClient(activity, signInOptions)
        callback.safeInProcessing("Signing out Google account...")
        val signedEmail = getSignedInGoogleAccountEmail(activity)
        client.signOut().addOnCompleteListener {
            callback.safeSuccess(signedEmail)
            requestSignIn(activity, code)
        }.addOnFailureListener { e: Exception -> callback.safeFailure(e.message) }
    }

    /**
     * Handles the `result` of a completed sign-in activity initiated from [ ][.requestSignIn].
     */
    fun handleSignInResult(
        activity: Activity, result: Intent?, callback: ProgressCallback,
        action: Runnable,
    ) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
            .addOnSuccessListener { googleAccount: GoogleSignInAccount ->
                Xlog.d(TAG, "Signed in as " + googleAccount.email)

                // Use the authenticated account to sign in to the Drive service.
                val credential = GoogleAccountCredential.usingOAuth2(
                    activity, setOf(DriveScopes.DRIVE_FILE))
                credential.selectedAccount = googleAccount.account
                mDriveService = Drive.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    GsonFactory(),
                    credential)
                    .setApplicationName(activity.getString(R.string.quotelock))
                    .build()
                action.run()
            }
            .addOnFailureListener { exception: Exception? ->
                Xlog.e(TAG, "Unable to sign in.", exception!!)
                callback.safeFailure("Unable to sign in.")
            }
    }

    /**
     * Get the md5 checksum and last modification time of given database file.
     */
    fun getDatabaseInfo(context: Context, databaseName: String?): Pair<String?, Long?> {
        //database path
        val databaseFileName = context.getDatabasePath(databaseName).toString()
        val dbFile = File(databaseFileName)
        return if (!dbFile.exists()) {
            Pair(null, null)
        } else try {
            val md5Str = dbFile.md5String()
            val modifiedTime = dbFile.lastModified()
            Pair(md5Str, modifiedTime)
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
            val googleFile = files().create(metadata).execute()
                ?: throw IOException("Null result when requesting file creation.")
            googleFile.id
        } catch (exception: Exception) {
            Xlog.e(TAG, "Couldn't create file.", exception)
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
        } catch (exception: IOException) {
            Xlog.e(TAG, "Unable to query files.", exception)
            null
        }
    }

    /**
     * Opens the file identified by `fileId` and returns a [Pair] of its name and
     * stream.
     */
    @Throws(IOException::class)
    fun Drive.readFile(fileId: String?): Pair<Result, InputStream> {
        val result = Result()
        // Retrieve the metadata as a File object.
        val metadata = files()[fileId].setFields(NEEDED_FILE_FIELDS).execute()
        result.success = true
        result.md5 = metadata.md5Checksum
        result.timestamp = if (metadata.modifiedTime == null) -1 else metadata.modifiedTime.value

        // Get the stream of file.
        return Pair.create(result, files()[fileId].executeMediaAsInputStream())
    }

    /**
     * Import the file identified by `fileId`.
     */
    private fun Drive.importDbFileSync(
        context: Context,
        fileId: String?,
        databaseName: String?,
    ): Result {
        return try {
            val outFileName = context.getDatabasePath(databaseName).toString()
            val digest = MessageDigest.getInstance("MD5")
            val readFileResult = readFile(fileId)
            readFileResult.second.use { inputStream ->
                if (inputStream == null) {
                    throw IOException()
                }
                DigestInputStream(inputStream, digest).use { dis ->
                    FileOutputStream(outFileName).use { output ->
                        // Transfer bytes from the input file to the output file
                        val buffer = ByteArray(1024)
                        var length: Int
                        while (dis.read(buffer).also { length = it } > 0) {
                            output.write(buffer, 0, length)
                        }
                    }
                }
            }
            readFileResult.first
        } catch (exception: Exception) {
            Xlog.e(TAG, "Unable to read file via REST.", exception)
            Result()
        }
    }

    /**
     * Updates the file identified by `fileId` with the given `name`.
     */
    private fun Drive.saveFileSync(context: Context, fileId: String?, name: String?): Result {
        try {
            val result = Result()
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
            result.success = true
            result.md5 = remoteFile.md5Checksum
            result.timestamp =
                if (remoteFile.modifiedTime == null) -1 else remoteFile.modifiedTime.value
            return result
        } catch (exception: Exception) {
            Xlog.e(TAG, "Unable to save file via REST.", exception)
            return Result()
        }
    }

    fun performDriveBackupAsync(
        activity: Activity,
        databaseName: String,
        callback: ProgressCallback,
    ) {
        if (!checkGoogleAccount(activity, REQUEST_CODE_SIGN_IN_BACKUP)) {
            return
        }
        mDriveService.run {
            ioScope.launch scope@{
                callback.safeInProcessing("Querying backup file on Google Drive...")
                val queryFiles = queryFilesSync()
                queryFiles ?: run {
                    callback.safeFailure("Unable to query files on Google Drive.")
                    return@scope
                }
                val databaseFile = queryFiles.files.find { it.name == databaseName }
                databaseFile?.run {
                    callback.safeInProcessing("Importing the existing backup file via Google Drive...")
                    val saveResult = saveFileSync(activity, id, databaseName)
                    if (saveResult.success) {
                        callback.safeSuccess()
                    } else {
                        callback.safeFailure("Unable to save file on Google Drive.")
                    }
                    return@scope
                }
                callback.safeInProcessing("There is no existing backup file on Google Drive. Creating now...")
                val createFileResult = createFileSync(databaseName)
                if (createFileResult == null) {
                    callback.safeSuccess()
                } else {
                    callback.safeFailure("Couldn't create file on Google Drive.")
                }
            }
        }
    }

    fun performSafeDriveBackupSync(context: Context, databaseName: String): Result {
        val result = Result()
        return if (!ensureDriveService(context)) {
            result
        } else try {
            val fileList = mDriveService.queryFilesSync() ?: return result
            var fileId: String? = null
            for (file in fileList.files) {
                if (file.name == databaseName) {
                    fileId = file.id
                    break
                }
            }
            if (fileId == null) {
                fileId = mDriveService.createFileSync(databaseName)
            }
            return mDriveService.saveFileSync(context, fileId, databaseName)
        } catch (e: Exception) {
            Xlog.e(TAG, "Unable to backup files.", e)
            result
        }
    }

    fun performDriveRestoreAsync(
        activity: Activity,
        databaseName: String,
        callback: ProgressCallback,
    ) {
        if (!checkGoogleAccount(activity, REQUEST_CODE_SIGN_IN_RESTORE)) {
            return
        }
        mDriveService.run {
            ioScope.launch scope@{
                callback.safeInProcessing("Querying backup file on Google Drive...")
                val queryFiles = queryFilesSync()
                queryFiles ?: run {
                    callback.safeFailure("Unable to query files on Google Drive.")
                    return@scope
                }
                val databaseFile = queryFiles.files.find { it.name == databaseName }
                databaseFile?.run {
                    callback.safeInProcessing("Importing the existing backup file via Google Drive...")
                    val importResult = importDbFileSync(activity, id, databaseName)
                    if (importResult.success) {
                        callback.safeSuccess()
                    } else {
                        callback.safeFailure("Unable to read file via Google Drive.")
                    }
                    return@scope
                }
                Xlog.e(TAG, "There is no $databaseName on drive")
                callback.safeFailure("There is no existing backup file on Google Drive.")
            }
        }
    }

    fun performSafeDriveRestoreSync(context: Context, databaseName: String): Result {
        val result = Result()
        if (!ensureDriveService(context)) {
            return result
        }
        try {
            val fileList = mDriveService.queryFilesSync() ?: return result
            for (file in fileList.files) {
                if (file.name == databaseName) {
                    return mDriveService.importDbFileSync(context, file.id, databaseName)
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

        constructor(result: Result) : this() {
            success = result.success
            md5 = result.md5
            timestamp = result.timestamp
        }

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
        val instance = RemoteBackup()
    }
}