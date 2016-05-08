package com.crossbowffs.quotelock.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.crossbowffs.quotelock.utils.JobUtils;
import com.crossbowffs.quotelock.utils.Xlog;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = BootReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;
        Xlog.i(TAG, "Boot complete, scheduling quote download task");
        JobUtils.createQuoteDownloadJob(context);
    }
}
