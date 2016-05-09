package com.crossbowffs.quotelock.utils;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import com.crossbowffs.quotelock.app.QuoteDownloaderService;
import com.crossbowffs.quotelock.preferences.PrefKeys;

public class JobUtils {
    private static final String TAG = JobUtils.class.getSimpleName();
    public static final int JOB_ID = 1;

    public static void createQuoteDownloadJob(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PrefKeys.PREF_COMMON, Context.MODE_PRIVATE);
        int refreshPeriodSecs = Integer.parseInt(preferences.getString(PrefKeys.PREF_COMMON_REFRESH_RATE, PrefKeys.PREF_COMMON_REFRESH_RATE_DEFAULT));
        if (refreshPeriodSecs < 60) {
            Xlog.i(TAG, "Refresh period too short, setting to 60 seconds");
            refreshPeriodSecs = 60;
        }
        boolean unmeteredOnly = preferences.getBoolean(PrefKeys.PREF_COMMON_UNMETERED_ONLY, PrefKeys.PREF_COMMON_UNMETERED_ONLY_DEFAULT);

        long refreshPeriodMs = refreshPeriodSecs * 1000;
        int networkType = unmeteredOnly ? JobInfo.NETWORK_TYPE_UNMETERED : JobInfo.NETWORK_TYPE_ANY;

        JobInfo jobInfo = new JobInfo.Builder(JOB_ID, new ComponentName(context, QuoteDownloaderService.class))
            .setMinimumLatency(refreshPeriodMs)
            .setRequiredNetworkType(networkType)
            .build();
        JobScheduler scheduler = (JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        scheduler.schedule(jobInfo);

        Xlog.i(TAG, "Scheduled recurring quote download task");
        Xlog.i(TAG, "  Refresh period: %d", refreshPeriodSecs);
        Xlog.i(TAG, "  Unmetered only: %s", unmeteredOnly);
    }
}
