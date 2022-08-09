package com.crossbowffs.quotelock.app

import android.app.Application
import com.crossbowffs.quotelock.account.SyncAccountManager
import com.crossbowffs.quotelock.account.google.GoogleAccountHelper.getSignedInGoogleAccountEmail
import com.crossbowffs.quotelock.account.google.GoogleAccountHelper.isGoogleAccountSignedIn
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * @author Yubyf
 * @date 2021/6/20.
 */
@HiltAndroidApp
class App : Application() {

    @Inject
    lateinit var syncAccountManager: SyncAccountManager

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
        instance = this
        syncAccountManager.initialize()
        if (isGoogleAccountSignedIn(this)) {
            getSignedInGoogleAccountEmail(this).takeIf { !it.isNullOrEmpty() }?.let { account ->
                syncAccountManager.addOrUpdateAccount(account)
            }
        }
    }

    companion object {
        lateinit var instance: App
    }
}