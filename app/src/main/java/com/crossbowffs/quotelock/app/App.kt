package com.crossbowffs.quotelock.app

import android.app.Application
import com.crossbowffs.quotelock.account.SyncAccountManager
import com.crossbowffs.quotelock.backup.GDriveSyncManager

/**
 * @author Yubyf
 * @date 2021/6/20.
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        SyncAccountManager.instance.initialize(this)
        if (GDriveSyncManager.INSTANCE.isGoogleAccountSignedIn(this)) {
            val accountName = GDriveSyncManager.INSTANCE.getSignedInGoogleAccountEmail(this)
            if (!accountName.isNullOrEmpty()) {
                SyncAccountManager.instance.addOrUpdateAccount(accountName)
            }
        }
    }

    companion object {
        lateinit var INSTANCE: App
    }
}