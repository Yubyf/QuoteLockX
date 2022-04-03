package com.crossbowffs.quotelock.account.syncadapter

import android.accounts.Account
import android.accounts.AccountManager
import android.content.AbstractThreadedSyncAdapter
import android.content.ContentProviderClient
import android.content.Context
import android.content.SyncResult
import android.os.Bundle
import android.text.TextUtils
import com.crossbowffs.quotelock.backup.RemoteBackup
import com.crossbowffs.quotelock.collections.database.QuoteCollectionContract
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.className
import com.yubyf.quotelockx.BuildConfig

/**
 * @author Yubyf
 * @date 2021/6/20.
 */
class SyncAdapter(private val mContext: Context, autoInitialize: Boolean) :
    AbstractThreadedSyncAdapter(
        mContext, autoInitialize) {

    private val mAccountManager: AccountManager = AccountManager.get(mContext)

    override fun onPerformSync(
        account: Account, extras: Bundle, authority: String,
        provider: ContentProviderClient, syncResult: SyncResult,
    ) {
        Xlog.d(TAG, "Performing account sync...")
        if (!RemoteBackup.INSTANCE.isGoogleAccountSignedIn(mContext)) {
            Xlog.d(TAG, "No account signed in.")
            syncResult.stats.numAuthExceptions++
            return
        }
        val action = checkBackupOrRestore(account)
        val result = when {
            action == 0 -> {
                return
            }
            action < 0 -> {
                Xlog.d(TAG, "Performing remote restore...")
                RemoteBackup.INSTANCE.performSafeDriveRestoreSync(mContext,
                    QuoteCollectionContract.DATABASE_NAME)
            }
            else -> {
                Xlog.d(TAG, "Performing remote backup...")
                RemoteBackup.INSTANCE.performSafeDriveBackupSync(mContext,
                    QuoteCollectionContract.DATABASE_NAME)
            }
        }
        if (result.success) {
            Xlog.d(TAG, "Account sync success")
            syncResult.stats.numInserts++
            setServerSyncMarker(account, result.md5, result.timestamp)
        } else {
            Xlog.d(TAG, "Account sync failed")
            syncResult.stats.numIoExceptions++
        }
    }

    /**
     * Check whether the current sync action should be backup, restore or nothing.
     *
     * @return the value `0` if nothing to do;
     * a value less than `0` if should be restore; and
     * a value greater than `0` if should be backup
     */
    private fun checkBackupOrRestore(account: Account): Int {
        val serverMarker = getServerSyncMarker(account)
        val syncTimestamp = getSyncTimestamp(account)
        val databaseInfo = RemoteBackup.INSTANCE
            .getDatabaseInfo(mContext, QuoteCollectionContract.DATABASE_NAME)
        return if (serverMarker.isNullOrEmpty() || syncTimestamp < 0 || databaseInfo.first.isNullOrEmpty()
        ) {
            Xlog.d(TAG,
                "First sync or local database is not created, need to perform remote restore")
            -1
        } else {
            if (databaseInfo.first == serverMarker) {
                Xlog.d(TAG, "Database not changed, no need to sync")
                0
            } else if (databaseInfo.second != null && databaseInfo.second!! > syncTimestamp) {
                1
            } else {
                -1
            }
        }
    }

    /**
     * This helper function fetches the last known marker
     * we received from the server - or null if we've never synced.
     */
    private fun getServerSyncMarker(account: Account): String? {
        return mAccountManager.getUserData(account, SYNC_MARKER_KEY)
    }

    private fun getSyncTimestamp(account: Account): Long {
        val timestampString = mAccountManager.getUserData(account, SYNC_TIMESTAMP_KEY)
        return if (!TextUtils.isEmpty(timestampString)) {
            timestampString.toLong()
        } else -1
    }

    /**
     * Save off the last sync marker and timestamp from the server.
     */
    private fun setServerSyncMarker(account: Account, marker: String?, timestamp: Long) {
        mAccountManager.setUserData(account, SYNC_MARKER_KEY, marker)
        mAccountManager.setUserData(account, SYNC_TIMESTAMP_KEY, timestamp.toString())
    }

    companion object {
        private val TAG = className<SyncAdapter>()
        const val SYNC_MARKER_KEY = BuildConfig.APPLICATION_ID + ".sync.marker"
        const val SYNC_TIMESTAMP_KEY = BuildConfig.APPLICATION_ID + ".sync.timestamp"
    }

}