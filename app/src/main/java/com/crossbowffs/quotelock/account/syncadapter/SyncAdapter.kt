package com.crossbowffs.quotelock.account.syncadapter

import android.accounts.Account
import android.accounts.AccountManager
import android.content.AbstractThreadedSyncAdapter
import android.content.ContentProviderClient
import android.content.Context
import android.content.SyncResult
import android.os.Bundle
import android.text.TextUtils
import com.crossbowffs.quotelock.account.google.GoogleAccountHelper
import com.crossbowffs.quotelock.data.modules.collections.QuoteCollectionRepository
import com.crossbowffs.quotelock.data.modules.collections.database.QuoteCollectionContract
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.className
import com.crossbowffs.quotelock.utils.getDatabaseInfo
import com.yubyf.quotelockx.BuildConfig
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking

/**
 * @author Yubyf
 * @date 2021/6/20.
 */
class SyncAdapter(private val mContext: Context, autoInitialize: Boolean) :
    AbstractThreadedSyncAdapter(mContext, autoInitialize) {

    private val mAccountManager: AccountManager = AccountManager.get(mContext)

    override fun onPerformSync(
        account: Account, extras: Bundle, authority: String,
        provider: ContentProviderClient, syncResult: SyncResult,
    ) {
        Xlog.d(TAG, "Performing account sync...")
        if (!GoogleAccountHelper.isGoogleAccountSignedIn(mContext)) {
            Xlog.d(TAG, "No account signed in.")
            syncResult.stats.numAuthExceptions++
            return
        }
        val repository = EntryPointAccessors.fromApplication(
            mContext.applicationContext, SyncAdapterEntryPoint::class.java).collectionRepository()
        val action = checkBackupOrRestore(account)
        val result = when {
            action == null -> {
                Xlog.d(TAG, "No backup or restore action found. Retry 5 minutes later.")
                syncResult.delayUntil = (System.currentTimeMillis() / 1000) + 5 * 60
                return
            }
            action == 0 -> {
                Xlog.d(TAG, "Database not changed, no need to sync")
                return
            }
            action < 0 -> {
                Xlog.d(TAG, "Performing remote restore...")
                runBlocking { repository.gDriveRestoreSync() }
            }
            else -> {
                Xlog.d(TAG, "Performing remote backup...")
                runBlocking { repository.gDriveBackupSync() }
            }
        }
        if (result.success) {
            Xlog.d(TAG, "Account sync success")
            syncResult.stats.numInserts++
            syncResult.fullSyncRequested
            mAccountManager.setServerSyncMarker(account, result.md5, result.timestamp)
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
    private fun checkBackupOrRestore(account: Account): Int? {
        val serverMarker = mAccountManager.getServerSyncMarker(account)
        val syncTimestamp = mAccountManager.getSyncTimestamp(account)
        val databaseInfo = mContext.getDatabaseInfo(QuoteCollectionContract.DATABASE_NAME)
        return if (serverMarker.isNullOrEmpty() || syncTimestamp < 0 || databaseInfo.first.isNullOrEmpty()
        ) {
            Xlog.d(TAG,
                "First sync or local database is not created, need to perform remote restore")
            null
        } else {
            when {
                databaseInfo.first == serverMarker -> 0
                databaseInfo.second != null && databaseInfo.second!! > syncTimestamp -> 1
                else -> -1
            }
        }
    }

    companion object {
        private val TAG = className<SyncAdapter>()
        const val SYNC_MARKER_KEY = BuildConfig.APPLICATION_ID + ".sync.marker"
        const val SYNC_TIMESTAMP_KEY = BuildConfig.APPLICATION_ID + ".sync.timestamp"
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SyncAdapterEntryPoint {
        fun collectionRepository(): QuoteCollectionRepository
    }
}

/**
 * This helper function fetches the last known marker
 * we received from the server - or null if we've never synced.
 */
fun AccountManager.getServerSyncMarker(account: Account): String? {
    return getUserData(account, SyncAdapter.SYNC_MARKER_KEY)
}

fun AccountManager.getSyncTimestamp(account: Account): Long {
    val timestampString = getUserData(account, SyncAdapter.SYNC_TIMESTAMP_KEY)
    return if (!TextUtils.isEmpty(timestampString)) {
        timestampString.toLong()
    } else -1
}

/**
 * Save off the last sync marker and timestamp from the server.
 */
fun AccountManager.setServerSyncMarker(account: Account, marker: String?, timestamp: Long) {
    setUserData(account, SyncAdapter.SYNC_MARKER_KEY, marker)
    setUserData(account, SyncAdapter.SYNC_TIMESTAMP_KEY, timestamp.toString())
}