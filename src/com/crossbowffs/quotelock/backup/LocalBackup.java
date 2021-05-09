package com.crossbowffs.quotelock.backup;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.crossbowffs.quotelock.R;

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
    private static final int REQUEST_CODE_PERMISSIONS = 55;

    /** check permissions. */
    public static boolean verifyPermissions(Activity activity) {
        // Check if we have read or write permission
        int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_CODE_PERMISSIONS
            );
            return false;
        }
        return true;
    }

    /** ask to the user a name for the backup and perform it. The backup will be saved to a custom folder. */
    public static boolean performBackup(Activity activity, String databaseName) {
        if (!verifyPermissions(activity)) {
            Toast.makeText(activity, "Please grant external storage permission and retry.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + activity.getResources().getString(R.string.quotelock));

        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        if (success) {
            String out = folder.getAbsolutePath() + File.separator + databaseName;
            return backup(activity, databaseName, out);
        } else {
            Toast.makeText(activity, "Unable to create directory. Retry", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    /** ask to the user what backup to restore */
    public static boolean performRestore(Activity activity, String databaseName) {
        if (!verifyPermissions(activity)) {
            Toast.makeText(activity, "Please grant external storage permission and retry.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + activity.getResources().getString(R.string.quotelock));
        if (folder.exists()) {
            File file = new File(folder, databaseName);
            if (!file.exists()) {
                Toast.makeText(activity, "Backup file not exists.\nDo a backup before a restore!", Toast.LENGTH_SHORT).show();
                return false;
            }
            return importDb(activity, databaseName, file.getAbsolutePath());
        } else {
            Toast.makeText(activity, "Backup folder not present.\nDo a backup before a restore!", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private static boolean backup(Context context, String databaseName, String outFileName) {

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

            Toast.makeText(context, "Backup Completed", Toast.LENGTH_SHORT).show();
            return true;
        } catch (Exception e) {
            Toast.makeText(context, "Unable to backup database. Retry", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return false;
    }

    private static boolean importDb(Context context, String databaseName, String inFileName) {
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

            Toast.makeText(context, "Import Completed", Toast.LENGTH_SHORT).show();
            return true;
        } catch (Exception e) {
            Toast.makeText(context, "Unable to import database. Retry", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return false;
    }
}
