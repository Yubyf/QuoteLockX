package com.crossbowffs.quotelock.account.syncadapter

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Service to handle Account sync. This is invoked with an intent with action
 * ACTION_AUTHENTICATOR_INTENT. It instantiates the syncadapter and returns its IBinder.
 * <br></br>
 * Reference: [aosp/platform_development](https://github.com/aosp-mirror/platform_development/blob/2f18dab43e/samples/SampleSyncAdapter/src/com/example/android/samplesync/syncadapter/SyncService.java)
 */
class SyncService : Service() {

    override fun onCreate() {
        synchronized(sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = SyncAdapter(applicationContext, true)
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return sSyncAdapter?.syncAdapterBinder
    }

    companion object {
        private val sSyncAdapterLock = Any()

        @SuppressLint("StaticFieldLeak")
        private var sSyncAdapter: SyncAdapter? = null
    }
}