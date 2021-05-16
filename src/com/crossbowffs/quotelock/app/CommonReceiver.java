package com.crossbowffs.quotelock.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;

import com.crossbowffs.quotelock.consts.PrefKeys;
import com.crossbowffs.quotelock.utils.WorkUtils;
import com.crossbowffs.quotelock.utils.Xlog;

public class CommonReceiver extends BroadcastReceiver {
    private static final String TAG = CommonReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Xlog.d(TAG, "Received action: %s", action);
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
                // Notify LockscreenHook to show current quotes after booting.
                notifyBooted(context);
            }
            WorkUtils.createQuoteDownloadWork(context, false);
        }
    }

    private void notifyBooted(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PrefKeys.PREF_QUOTES, Context.MODE_PRIVATE);
        boolean hasBootNotifyFlag = sharedPreferences.contains(PrefKeys.PREF_BOOT_NOTIFY_FLAG);
        if (!hasBootNotifyFlag) {
            sharedPreferences.edit().putInt(PrefKeys.PREF_BOOT_NOTIFY_FLAG, 0).apply();
        } else {
            sharedPreferences.edit().remove(PrefKeys.PREF_BOOT_NOTIFY_FLAG).apply();
        }
    }
}
