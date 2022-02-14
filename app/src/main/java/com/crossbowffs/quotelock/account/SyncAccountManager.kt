package com.crossbowffs.quotelock.account

import android.accounts.Account
import android.accounts.AccountManager
import android.content.ContentResolver
import android.content.Context
import android.os.Build
import com.crossbowffs.quotelock.BuildConfig
import com.crossbowffs.quotelock.account.SyncAccountManager
import com.crossbowffs.quotelock.account.syncadapter.SyncAdapter
import com.crossbowffs.quotelock.collections.provider.QuoteCollectionContract
import com.crossbowffs.quotelock.collections.provider.QuoteCollectionObserver
import com.crossbowffs.quotelock.utils.Xlog

/**
 * @author Yubyf
 * @date 2021/6/20.
 */
class SyncAccountManager {

    private lateinit var mAccountManager: AccountManager

    fun initialize(context: Context) {
        mAccountManager = AccountManager.get(context)
        val resolver = context.contentResolver
        val observer = QuoteCollectionObserver(null)
        // Register the observer for the quote collection table.
        resolver.registerContentObserver(QuoteCollectionContract.Collections.CONTENT_URI,
            true, observer)
    }

    fun addOrUpdateAccount(name: String) {
        var account = currentSyncAccount
        if (account != null) {
            if (name == account.name) {
                Xlog.d(TAG, "The current signed-in account is already added.")
            } else {
                mAccountManager.renameAccount(account, name, null, null)
                Xlog.d(TAG, "Updated account with name $name")
                clearAccountUserData(account)
            }
        } else {
            account = Account(name, ACCOUNT_TYPE)
            mAccountManager.addAccountExplicitly(account, null, null)
            Xlog.d(TAG, "Added account of name $name")
            clearAccountUserData(account)
        }
        enableAccountSync(account)
    }

    fun removeAccount(name: String) {
        val account = currentSyncAccount
        if (account == null || name != account.name) {
            return
        }
        clearAccountUserData(account)
        Xlog.d(TAG, "Remove account - $name.")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            mAccountManager.removeAccountExplicitly(account)
        } else {
            mAccountManager.removeAccount(account, null, null)
        }
    }

    private fun clearAccountUserData(account: Account) {
        Xlog.d(TAG, "Clear user data of " + account.name + ".")
        mAccountManager.setUserData(account, SyncAdapter.SYNC_MARKER_KEY, null)
        mAccountManager.setUserData(account, SyncAdapter.SYNC_TIMESTAMP_KEY, "-1")
    }

    private fun enableAccountSync(account: Account) {
        Xlog.d(TAG, "Enable account sync")
        ContentResolver.setIsSyncable(account, QuoteCollectionContract.AUTHORITY, 1)

//        ContentResolver.addPeriodicSync(account, QuoteCollectionContract.AUTHORITY,
//                Bundle.EMPTY, TimeUnit.MINUTES.toSeconds(10));
        Xlog.d(TAG, "Enable account auto sync")
        ContentResolver.setSyncAutomatically(account, QuoteCollectionContract.AUTHORITY, true)
    }

    val currentSyncAccount: Account?
        get() = mAccountManager.getAccountsByType(ACCOUNT_TYPE)
            .run { if (isNotEmpty()) this[0] else null }

    companion object {
        private val TAG = SyncAccountManager::class.simpleName
        private const val ACCOUNT_TYPE = BuildConfig.APPLICATION_ID + ".account"
        val instance = SyncAccountManager()
    }
}