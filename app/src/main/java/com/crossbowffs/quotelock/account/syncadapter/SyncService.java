package com.crossbowffs.quotelock.account.syncadapter;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Service to handle Account sync. This is invoked with an intent with action
 * ACTION_AUTHENTICATOR_INTENT. It instantiates the syncadapter and returns its IBinder.
 * <br />
 * Reference: <a href="https://github.com/aosp-mirror/platform_development/blob/2f18dab43e/samples/SampleSyncAdapter/src/com/example/android/samplesync/syncadapter/SyncService.java">aosp/platform_development</a>
 */
public class SyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();

    private static SyncAdapter sSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new SyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
