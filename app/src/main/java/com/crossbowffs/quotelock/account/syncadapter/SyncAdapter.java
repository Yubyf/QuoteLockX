package com.crossbowffs.quotelock.account.syncadapter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.core.util.Pair;

import com.crossbowffs.quotelock.BuildConfig;
import com.crossbowffs.quotelock.backup.RemoteBackup;
import com.crossbowffs.quotelock.collections.provider.QuoteCollectionHelper;
import com.crossbowffs.quotelock.utils.Xlog;

import java.util.Objects;

/**
 * @author Yubyf
 * @date 2021/6/20.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = SyncAdapter.class.getSimpleName();
    public static final String SYNC_MARKER_KEY = BuildConfig.APPLICATION_ID + ".sync.marker";
    public static final String SYNC_TIMESTAMP_KEY = BuildConfig.APPLICATION_ID + ".sync.timestamp";

    private final AccountManager mAccountManager;

    private final Context mContext;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
        mAccountManager = AccountManager.get(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        Xlog.d(TAG, "Performing account sync...");
        if (!RemoteBackup.Companion.getInstance().isGoogleAccountSignedIn(mContext)) {
            Xlog.d(TAG, "No account signed in.");
            syncResult.stats.numAuthExceptions++;
            return;
        }
        RemoteBackup.Result result;
        int action = checkBackupOrRestore(account);
        if (action == 0) {
            return;
        } else if (action < 0) {
            Xlog.d(TAG, "Performing remote restore...");
            result = RemoteBackup.Companion.getInstance().performSafeDriveRestoreSync(mContext,
                    QuoteCollectionHelper.DATABASE_NAME);
        } else {
            Xlog.d(TAG, "Performing remote backup...");
            result = RemoteBackup.Companion.getInstance().performSafeDriveBackupSync(mContext,
                    QuoteCollectionHelper.DATABASE_NAME);
        }
        if (result.getSuccess()) {
            Xlog.d(TAG, "Account sync success");
            syncResult.stats.numInserts++;
            setServerSyncMarker(account, result.getMd5(), result.getTimestamp());
        } else {
            Xlog.d(TAG, "Account sync failed");
            syncResult.stats.numIoExceptions++;
        }
    }

    /**
     * Check whether the current sync action should be backup, restore or nothing.
     *
     * @return the value {@code 0} if nothing to do;
     * a value less than {@code 0} if should be restore; and
     * a value greater than {@code 0} if should be backup
     */
    private int checkBackupOrRestore(Account account) {
        String serverMarker = getServerSyncMarker(account);
        long syncTimestamp = getSyncTimestamp(account);
        Pair<String, Long> databaseInfo = RemoteBackup.Companion.getInstance()
                .getDatabaseInfo(mContext, QuoteCollectionHelper.DATABASE_NAME);
        int result = 0;
        if (TextUtils.isEmpty(serverMarker) || syncTimestamp < 0
                || TextUtils.isEmpty(databaseInfo.first)) {
            Xlog.d(TAG, "First sync or local database is not created, need to perform remote restore");
            result = -1;
        } else {
            if (Objects.equals(databaseInfo.first, serverMarker)) {
                Xlog.d(TAG, "Database not changed, no need to sync");
            } else if (databaseInfo.second != null && databaseInfo.second > syncTimestamp) {
                result = 1;
            } else {
                result = -1;
            }
        }
        return result;
    }

    /**
     * This helper function fetches the last known marker
     * we received from the server - or null if we've never synced.
     */
    private String getServerSyncMarker(Account account) {
        return mAccountManager.getUserData(account, SYNC_MARKER_KEY);
    }

    private long getSyncTimestamp(Account account) {
        String timestampString = mAccountManager.getUserData(account, SYNC_TIMESTAMP_KEY);
        if (!TextUtils.isEmpty(timestampString)) {
            return Long.parseLong(timestampString);
        }
        return -1;
    }

    /**
     * Save off the last sync marker and timestamp from the server.
     */
    private void setServerSyncMarker(Account account, String marker, long timestamp) {
        mAccountManager.setUserData(account, SYNC_MARKER_KEY, marker);
        mAccountManager.setUserData(account, SYNC_TIMESTAMP_KEY, Long.toString(timestamp));
    }
}
