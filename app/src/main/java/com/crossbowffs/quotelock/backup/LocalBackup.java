package com.crossbowffs.quotelock.backup;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;

import androidx.core.app.ActivityCompat;

import com.crossbowffs.quotelock.R;
import com.crossbowffs.quotelock.utils.AppExecutors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Reference: <a href="https://github.com/prof18/Database-Backup-Restore/blob/master/app/src/main/java/com/prof/dbtest/backup/LocalBackup.java">Database-Backup-Restore</a>
 *
 * @author Yubyf
 */
public class LocalBackup {

    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static final int REQUEST_CODE_PERMISSIONS_BACKUP = 55;
    public static final int REQUEST_CODE_PERMISSIONS_RESTORE = 43;

    /** check permissions. */
    public static boolean verifyPermissions(Activity activity, int requestCode) {
        // Check if we have read or write permission
        int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    requestCode
            );
            return false;
        }
        return true;
    }

    public static void handleRequestPermissionsResult(int[] grantResults, ProgressCallback callback,
                                                      Runnable action) {
        if (grantResults.length < 2 || grantResults[0] != PackageManager.PERMISSION_GRANTED
                || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
            callback.failure("Please grant external storage permission and retry.");
            return;
        }
        action.run();
    }

    /** ask to the user a name for the backup and perform it. The backup will be saved to a custom folder. */
    public static void performBackup(Activity activity, String databaseName, ProgressCallback callback) {
        if (!verifyPermissions(activity, REQUEST_CODE_PERMISSIONS_BACKUP)) {
            callback.failure("Please grant external storage permission and retry.");
            return;
        }

        AppExecutors.getInstance().diskIO().execute(() -> {
            File folder = new File(Environment.getExternalStorageDirectory() + File.separator + activity.getResources().getString(R.string.quotelock));

            boolean success = true;
            if (!folder.exists()) {
                success = folder.mkdirs();
            }
            if (success) {
                String out = folder.getAbsolutePath() + File.separator + databaseName;
                try {
                    backup(activity, databaseName, out);
                    callback.safeSuccess();
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.safeFailure(e.getMessage());
                }
            } else {
                callback.safeFailure("Unable to create directory. Retry");
            }
        });
    }

    /** ask to the user what backup to restore */
    public static void performRestore(Activity activity, String databaseName, ProgressCallback callback) {
        if (!verifyPermissions(activity, REQUEST_CODE_PERMISSIONS_RESTORE)) {
            callback.failure("Please grant external storage permission and retry.");
        }

        AppExecutors.getInstance().diskIO().execute(() -> {
            File folder = new File(Environment.getExternalStorageDirectory() + File.separator + activity.getResources().getString(R.string.quotelock));
            if (folder.exists()) {
                File file = new File(folder, databaseName);
                if (!file.exists()) {
                    callback.safeFailure("Backup file not exists.\nDo a backup before a restore!");
                    return;
                }
                try {
                    importDb(activity, databaseName, file.getAbsolutePath());
                    callback.safeSuccess();
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.safeFailure(e.getMessage());
                }
            } else {
                callback.safeFailure("Backup folder not present.\nDo a backup before a restore!");
            }
        });
    }

    private static boolean backup(Context context, String databaseName, String outFileName) throws Exception {
        //database path
        final String inFileName = context.getDatabasePath(databaseName).toString();

        try {

            File dbFile = new File(inFileName);
            FileInputStream fis = new FileInputStream(dbFile);

            // Open the empty db as the output stream
            OutputStream output = new FileOutputStream(outFileName);

            // Transfer bytes from the input file to the output file
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }

            // Close the streams
            output.flush();
            output.close();
            fis.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Unable to backup database. Retry");
        }
    }

    private static boolean importDb(Context context, String databaseName, String inFileName) throws Exception {
        final String outFileName = context.getDatabasePath(databaseName).toString();

        try {

            File dbFile = new File(inFileName);
            FileInputStream fis = new FileInputStream(dbFile);

            // Open the empty db as the output stream
            OutputStream output = new FileOutputStream(outFileName);

            // Transfer bytes from the input file to the output file
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }

            // Close the streams
            output.flush();
            output.close();
            fis.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Unable to import database. Retry");
        }
    }
}
