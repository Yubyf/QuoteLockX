package com.crossbowffs.quotelock.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;

import com.crossbowffs.quotelock.BuildConfig;
import com.crossbowffs.quotelock.account.syncadapter.SyncAdapter;
import com.crossbowffs.quotelock.collections.provider.QuoteCollectionContract;
import com.crossbowffs.quotelock.collections.provider.QuoteCollectionObserver;
import com.crossbowffs.quotelock.utils.Xlog;

/**
 * @author Yubyf
 * @date 2021/6/20.
 */
public class SyncAccountManager {
    private static final String TAG = SyncAccountManager.class.getSimpleName();

    private static final String ACCOUNT_TYPE = BuildConfig.APPLICATION_ID + ".account";

    private AccountManager mAccountManager;

    private static final SyncAccountManager INSTANCE = new SyncAccountManager();

    public static SyncAccountManager getInstance() {
        return INSTANCE;
    }

    public void initialize(Context context) {
        mAccountManager = AccountManager.get(context);
        ContentResolver resolver = context.getContentResolver();
        QuoteCollectionObserver observer = new QuoteCollectionObserver(null);
        // Register the observer for the quote collection table.
        resolver.registerContentObserver(QuoteCollectionContract.Collections.CONTENT_URI,
                true, observer);
    }

    public void addOrUpdateAccount(@NonNull String name) {
        Account account = getCurrentSyncAccount();
        if (account != null) {
            if (name.equals(account.name)) {
                Xlog.d(TAG, "The current signed-in account is already added.");
            } else {
                mAccountManager.renameAccount(account, name, null, null);
                Xlog.d(TAG, "Updated account with name " + name);
                clearAccountUserData(account);
            }
        } else {
            account = new Account(name, ACCOUNT_TYPE);
            mAccountManager.addAccountExplicitly(account, null, null);
            Xlog.d(TAG, "Added account of name " + name);
            clearAccountUserData(account);
        }
        enableAccountSync(account);
    }

    public void removeAccount(@NonNull String name) {
        Account account = getCurrentSyncAccount();
        if (account == null || !name.equals(account.name)) {
            return;
        }
        clearAccountUserData(account);
        Xlog.d(TAG, "Remove account - " + name + ".");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            mAccountManager.removeAccountExplicitly(account);
        } else {
            mAccountManager.removeAccount(account, null, null);
        }
    }

    private void clearAccountUserData(Account account) {
        Xlog.d(TAG, "Clear user data of " + account.name + ".");
        mAccountManager.setUserData(account, SyncAdapter.SYNC_MARKER_KEY, null);
        mAccountManager.setUserData(account, SyncAdapter.SYNC_TIMESTAMP_KEY, "-1");
    }

    private void enableAccountSync(Account account) {
        Xlog.d(TAG, "Enable account sync");
        ContentResolver.setIsSyncable(account, QuoteCollectionContract.AUTHORITY, 1);

//        ContentResolver.addPeriodicSync(account, QuoteCollectionContract.AUTHORITY,
//                Bundle.EMPTY, TimeUnit.MINUTES.toSeconds(10));

        Xlog.d(TAG, "Enable account auto sync");
        ContentResolver.setSyncAutomatically(account, QuoteCollectionContract.AUTHORITY, true);
    }

    public Account getCurrentSyncAccount() {
        Account[] accounts;
        return (accounts = mAccountManager.getAccountsByType(ACCOUNT_TYPE)).length > 0 ? accounts[0] : null;
    }
}
