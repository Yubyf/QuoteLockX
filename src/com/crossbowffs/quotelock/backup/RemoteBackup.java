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
package com.crossbowffs.quotelock.backup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.crossbowffs.quotelock.R;
import com.crossbowffs.quotelock.utils.AppExecutors;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.concurrent.Executor;

/**
 * @author Yubyf
 */
public class RemoteBackup {
    private static final String TAG = "RemoteBackup";
    private final Executor mExecutor = AppExecutors.getInstance().diskIO();
    private Drive mDriveService;

    public static final int REQUEST_CODE_SIGN_IN = 1;
    public static final int REQUEST_CODE_SIGN_IN_BACKUP = 2;
    public static final int REQUEST_CODE_SIGN_IN_RESTORE = 3;

    private static final RemoteBackup INSTANCE = new RemoteBackup();

    public static RemoteBackup getInstance() {
        return INSTANCE;
    }

    public boolean checkGoogleAccount(Activity activity, int requestCode) {
        if (mDriveService == null) {
            GoogleSignInAccount account;
            if ((account = GoogleSignIn.getLastSignedInAccount(activity)) == null) {
                requestSignIn(activity, requestCode);
                return false;
            } else {
                // Use the authenticated account to sign in to the Drive service.
                GoogleAccountCredential credential =
                        GoogleAccountCredential.usingOAuth2(
                                activity, Collections.singleton(DriveScopes.DRIVE_FILE));
                credential.setSelectedAccount(account.getAccount());
                mDriveService =
                        new Drive.Builder(
                                AndroidHttp.newCompatibleTransport(),
                                new GsonFactory(),
                                credential)
                                .setApplicationName(activity.getString(R.string.quotelock))
                                .build();
                return true;
            }
        } else {
            return true;
        }
    }

    public boolean isGoogleAccountSignedIn(Activity activity) {
        return GoogleSignIn.getLastSignedInAccount(activity) != null;
    }

    public String getSignedInGoogleAccountEmail(Context context) {
        GoogleSignInAccount account;
        return (account = GoogleSignIn.getLastSignedInAccount(context)) != null ? account.getEmail() : null;
    }

    public Uri getSignedInGoogleAccountPhoto(Context context) {
        GoogleSignInAccount account;
        return (account = GoogleSignIn.getLastSignedInAccount(context)) != null ? account.getPhotoUrl() : null;
    }

    /**
     * Starts a sign-in activity using {@link #REQUEST_CODE_SIGN_IN},
     * {@link #REQUEST_CODE_SIGN_IN_BACKUP} or {@link #REQUEST_CODE_SIGN_IN_RESTORE}.
     */
    public void requestSignIn(Activity activity, int code) {
        Log.d(TAG, "Requesting sign-in");

        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestProfile()
                        .requestEmail()
                        .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                        .build();
        GoogleSignInClient client = GoogleSignIn.getClient(activity, signInOptions);

        // The result of the sign-in Intent is handled in onActivityResult.
        activity.startActivityForResult(client.getSignInIntent(), code);
    }

    public void switchAccount(Activity activity, int code, ProgressCallback callback) {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();
        GoogleSignInClient client = GoogleSignIn.getClient(activity, signInOptions);
        callback.safeInProcessing("Signing out Google account...");
        client.signOut().addOnCompleteListener(task -> {
            callback.safeSuccess();
            requestSignIn(activity, code);
        }).addOnFailureListener(e -> callback.safeFailure(e.getMessage()));
    }

    /**
     * Handles the {@code result} of a completed sign-in activity initiated from {@link
     * #requestSignIn(Activity, int)}.
     */
    public void handleSignInResult(Activity activity, Intent result, ProgressCallback callback,
                                   Runnable action) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
                .addOnSuccessListener(googleAccount -> {
                    Log.d(TAG, "Signed in as " + googleAccount.getEmail());

                    // Use the authenticated account to sign in to the Drive service.
                    GoogleAccountCredential credential =
                            GoogleAccountCredential.usingOAuth2(
                                    activity, Collections.singleton(DriveScopes.DRIVE_FILE));
                    credential.setSelectedAccount(googleAccount.getAccount());

                    mDriveService =
                            new Drive.Builder(
                                    AndroidHttp.newCompatibleTransport(),
                                    new GsonFactory(),
                                    credential)
                                    .setApplicationName(activity.getString(R.string.quotelock))
                                    .build();
                    action.run();
                })
                .addOnFailureListener(exception -> {
                    Log.e(TAG, "Unable to sign in.", exception);
                    callback.safeFailure("Unable to sign in.");
                });
    }

    /**
     * Creates a text file in the user's My Drive folder and returns its file ID.
     */
    public Task<String> createFile(String name) {
        if (mDriveService == null) {
            return null;
        }
        return Tasks.call(mExecutor, () -> {
            File metadata = new File()
                    .setParents(Collections.singletonList("root"))
                    .setMimeType("application/vnd.sqlite3")
                    .setName(name);

            File googleFile = mDriveService.files().create(metadata).execute();
            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }

            return googleFile.getId();
        });
    }

    /**
     * Returns a {@link FileList} containing all the visible files in the user's My Drive.
     *
     * <p>The returned list will only contain files visible to this app, i.e. those which were
     * created by this app. To perform operations on files not created by the app, the project must
     * request Drive Full Scope in the <a href="https://play.google.com/apps/publish">Google
     * Developer's Console</a> and be submitted to Google for verification.</p>
     */
    public Task<FileList> queryFiles() {
        return Tasks.call(mExecutor, () ->
                mDriveService.files().list().setSpaces("drive").execute());
    }

    /**
     * Opens the file identified by {@code fileId} and returns a {@link Pair} of its name and
     * stream.
     */
    public Pair<String, InputStream> readFile(String fileId) throws IOException {
        // Retrieve the metadata as a File object.
        File metadata = mDriveService.files().get(fileId).execute();
        String name = metadata.getName();

        // Get the stream of file.
        return Pair.create(name, mDriveService.files().get(fileId).executeMediaAsInputStream());
    }

    /**
     * Import the file identified by {@code fileId}.
     */
    public Task<Void> importDbFile(Context context, String databaseName, String fileId) {
        return Tasks.call(mExecutor, () -> {
            final String outFileName = context.getDatabasePath(databaseName).toString();

            // Stream the remote file to app data file.
            try (InputStream is = readFile(fileId).second;
                 OutputStream output = new FileOutputStream(outFileName)) {
                if (is == null) {
                    throw new IOException();
                }
                // Transfer bytes from the input file to the output file
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    output.write(buffer, 0, length);
                }
            }
            return null;
        });
    }

    /**
     * Updates the file identified by {@code fileId} with the given {@code name}.
     */
    public Task<Void> saveFile(Activity activity, String fileId, String name) {
        return Tasks.call(mExecutor, () -> {
            //database path
            final String inFileName = activity.getDatabasePath(name).toString();
            java.io.File dbFile = new java.io.File(inFileName);
            FileInputStream fis = new FileInputStream(dbFile);

            // Create a File containing any metadata changes.
            File metadata = new File().setName(name);

            // Convert content to an InputStreamContent instance.
            InputStreamContent contentStream = new InputStreamContent("application/vnd.sqlite3", fis);

            // Update the metadata and contents.
            mDriveService.files().update(fileId, metadata, contentStream).execute();
            return null;
        });
    }

    public void performDriveBackup(Activity activity, String databaseName, ProgressCallback callback) {
        if (!checkGoogleAccount(activity, REQUEST_CODE_SIGN_IN_BACKUP)) {
            return;
        }
        callback.safeInProcessing("Querying backup file on Google Drive...");
        queryFiles().addOnSuccessListener(fileList -> {
            for (File file : fileList.getFiles()) {
                if (!file.getName().equals(databaseName)) {
                    continue;
                }
                callback.safeInProcessing("Updating the existing backup file on Google Drive...");
                saveFile(activity, file.getId(), databaseName)
                        .addOnSuccessListener(unused -> callback.safeSuccess())
                        .addOnFailureListener(exception -> {
                            Log.e(TAG, "Unable to save file via REST.", exception);
                            callback.safeFailure("Unable to save file on Google Drive.");
                        });
                return;
            }
            callback.safeInProcessing("There is no existing backup file on Google Drive. Creating now...");
            createFile(databaseName)
                    .addOnSuccessListener(fileId ->
                            saveFile(activity, fileId, databaseName)
                                    .addOnSuccessListener(unused -> callback.safeSuccess())
                                    .addOnFailureListener(exception -> {
                                        Log.e(TAG, "Unable to save file via REST.", exception);
                                        callback.safeFailure("Unable to save file on Google Drive.");
                                    }))
                    .addOnFailureListener(exception -> {
                        Log.e(TAG, "Couldn't create file.", exception);
                        callback.safeFailure("Couldn't create file on Google Drive.");
                    });
        }).addOnFailureListener(exception -> {
            Log.e(TAG, "Unable to query files.", exception);
            callback.safeFailure("Couldn't query files on Google Drive.");
        });
    }

    public void performDriveRestore(Activity activity, String databaseName, ProgressCallback callback) {
        if (!checkGoogleAccount(activity, REQUEST_CODE_SIGN_IN_RESTORE)) {
            return;
        }
        callback.safeInProcessing("Querying backup file on Google Drive...");
        queryFiles().addOnSuccessListener(fileList -> {
            for (File file : fileList.getFiles()) {
                if (!file.getName().equals(databaseName)) {
                    continue;
                }
                callback.safeInProcessing("Importing the existing backup file via Google Drive...");
                importDbFile(activity, databaseName, file.getId())
                        .addOnSuccessListener(unused -> callback.safeSuccess())
                        .addOnFailureListener(exception -> {
                            Log.e(TAG, "Unable to read file via REST.", exception);
                            callback.safeFailure("Unable to read file via Google Drive.");
                        });
                return;
            }
            Log.e(TAG, "There is no " + databaseName + " on drive");
            callback.safeFailure("There is no existing backup file on Google Drive.");
        }).addOnFailureListener(exception -> {
            Log.e(TAG, "Unable to query files.", exception);
            callback.safeFailure("Unable to query files on Google Drive.");
        });
    }
}
