package com.crossbowffs.quotelock.account

import android.accounts.Account
import android.accounts.AccountManager
import android.content.ContentResolver
import android.os.Build
import android.os.Bundle
import com.crossbowffs.quotelock.account.syncadapter.getServerSyncMarker
import com.crossbowffs.quotelock.account.syncadapter.getSyncTimestamp
import com.crossbowffs.quotelock.account.syncadapter.setServerSyncMarker
import com.crossbowffs.quotelock.data.modules.collections.QuoteCollectionRepository
import com.crossbowffs.quotelock.data.modules.collections.database.QuoteCollectionContract
import com.crossbowffs.quotelock.di.IoDispatcher
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.className
import com.yubyf.quotelockx.BuildConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author Yubyf
 * @date 2021/6/20.
 */
class SyncAccountManager @Inject constructor(
    private val accountManager: AccountManager,
    private val collectionRepository: QuoteCollectionRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) {

    fun initialize() {
        CoroutineScope(dispatcher).launch {
            collectionRepository.getAllStream().collect {
                /*
                 * Ask the framework to run sync adapter.
                 * To maintain backward compatibility, assume that
                 * changeUri is null.
                 */
                Xlog.d(TAG, "Data on change, requesting sync...")
                ContentResolver.requestSync(currentSyncAccount,
                    QuoteCollectionContract.AUTHORITY, Bundle()
                )
            }
        }
    }

    fun addOrUpdateAccount(name: String, clearUserDataIfExist: Boolean = false) {
        var account = currentSyncAccount
        if (account != null) {
            if (name == account.name) {
                Xlog.d(TAG, "The current signed-in account is already added.")
                if (clearUserDataIfExist) clearAccountUserData(account)
            } else {
                accountManager.renameAccount(account, name, null, null)
                Xlog.d(TAG, "Updated account with name $name")
                clearAccountUserData(account)
            }
        } else {
            account = Account(name, ACCOUNT_TYPE)
            accountManager.addAccountExplicitly(account, null, null)
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
            accountManager.removeAccountExplicitly(account)
        } else {
            @Suppress("DEPRECATION")
            accountManager.removeAccount(account, null, null)
        }
    }

    fun needFirstSync(): Boolean {
        return currentSyncAccount?.let {
            accountManager.getServerSyncMarker(it).isNullOrEmpty()
                    || accountManager.getSyncTimestamp(it) < 0
        } ?: false
    }

    suspend fun performFirstSync(merge: Boolean = false) {
        currentSyncAccount?.let {
            val result = collectionRepository.gDriveRestoreSync(merge)
            accountManager.setServerSyncMarker(it, result.md5, result.timestamp)
        }
    }

    private fun clearAccountUserData(account: Account) {
        Xlog.d(TAG, "Clear user data of " + account.name + ".")
        accountManager.setServerSyncMarker(account, null, -1)
    }

    private fun enableAccountSync(account: Account) {
        Xlog.d(TAG, "Enable account sync")
        ContentResolver.setIsSyncable(account, QuoteCollectionContract.AUTHORITY, 1)

//        ContentResolver.addPeriodicSync(account, QuoteCollectionContract.AUTHORITY,
//                Bundle.EMPTY, TimeUnit.MINUTES.toSeconds(10));
        Xlog.d(TAG, "Enable account auto sync")
        ContentResolver.setSyncAutomatically(account, QuoteCollectionContract.AUTHORITY, true)
    }

    private val currentSyncAccount: Account?
        get() = accountManager.getAccountsByType(ACCOUNT_TYPE)
            .run { if (isNotEmpty()) this[0] else null }

    companion object {
        private val TAG = className<SyncAccountManager>()
        private const val ACCOUNT_TYPE = BuildConfig.APPLICATION_ID + ".account"
    }
}