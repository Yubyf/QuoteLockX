package com.crossbowffs.quotelock.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import com.crossbowffs.quotelock.utils.JobUtils;
import com.crossbowffs.quotelock.utils.Xlog;

public class CommonReceiver extends BroadcastReceiver {
    private static final String TAG = CommonReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Xlog.d(TAG, "Received action: %s", action);
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            JobUtils.createQuoteDownloadJob(context, false);
        }
    }
}
