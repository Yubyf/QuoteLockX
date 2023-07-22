package com.crossbowffs.quotelock.app

import android.app.Application
import com.crossbowffs.quotelock.account.SyncAccountManager
import com.crossbowffs.quotelock.account.google.GoogleAccountHelper.getSignedInGoogleAccountEmail
import com.crossbowffs.quotelock.account.google.GoogleAccountHelper.isGoogleAccountSignedIn
import com.crossbowffs.quotelock.di.DataModule
import com.crossbowffs.quotelock.di.workerModule
import com.google.android.material.color.DynamicColors
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module

/**
 * @author Yubyf
 * @date 2021/6/20.
 */
class App : Application() {

    private val syncAccountManager: SyncAccountManager by inject()

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
        instance = this

        startKoin {
            // Log Koin into Android logger
            androidLogger()
            // Reference Android context
            androidContext(this@App)
            // Work factory
            workManagerFactory()
            // Load modules
            modules(DataModule().module, workerModule)
        }

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