package com.crossbowffs.quotelock.app

import android.app.Application
import android.text.TextUtils
import com.crossbowffs.quotelock.account.SyncAccountManager
import com.crossbowffs.quotelock.backup.RemoteBackup

/**
 * @author Yubyf
 * @date 2021/6/20.
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        SyncAccountManager.getInstance().initialize(this)
        if (RemoteBackup.getInstance().isGoogleAccountSignedIn(this)) {
            val accountName = RemoteBackup.getInstance().getSignedInGoogleAccountEmail(this)
            if (!TextUtils.isEmpty(accountName)) {
                SyncAccountManager.getInstance().addOrUpdateAccount(accountName)
            }
        }
    }
}