package com.crossbowffs.quotelock.utils;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.crossbowffs.quotelock.app.QuoteDownloaderService;
import com.crossbowffs.quotelock.preferences.PrefKeys;

public class JobUtils {
    private static final String TAG = JobUtils.class.getSimpleName();
    public static final int JOB_ID = 0;

    public static boolean shouldRefreshQuote(Context context) {
        ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = manager.getActiveNetworkInfo();
        if (netInfo == null || !netInfo.isConnected()) {
            return false;
        }

        SharedPreferences preferences = context.getSharedPreferences(PrefKeys.PREF_COMMON, Context.MODE_PRIVATE);
        boolean unmeteredOnly = preferences.getBoolean(PrefKeys.PREF_COMMON_UNMETERED_ONLY, PrefKeys.PREF_COMMON_UNMETERED_ONLY_DEFAULT);
        if (unmeteredOnly && manager.isActiveNetworkMetered()) {
            return false;
        }

        return true;
    }

    public static void createQuoteDownloadJob(Context context, boolean forceCreate) {
        JobScheduler scheduler = (JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        // If we're not forcing a job refresh (e.g. when a setting changes),
        // ignore the request if the job already exists
        if (!forceCreate) {
            for (JobInfo job : scheduler.getAllPendingJobs()) {
                if (job.getId() == JOB_ID) {
                    Xlog.i(TAG, "Job already exists and forceCreate == false, ignoring");
                    return;
                }
            }
        }

        // Don't create the job if we haven't met the requirements
        // (e.g. no network connectivity or metered network)
        if (!shouldRefreshQuote(context)) {
            Xlog.i(TAG, "Should not refresh quote under current conditions, ignoring");
            return;
        }

        SharedPreferences preferences = context.getSharedPreferences(PrefKeys.PREF_COMMON, Context.MODE_PRIVATE);
        int refreshPeriodSecs = Integer.parseInt(preferences.getString(PrefKeys.PREF_COMMON_REFRESH_RATE, PrefKeys.PREF_COMMON_REFRESH_RATE_DEFAULT));
        if (refreshPeriodSecs < 60) {
            Xlog.i(TAG, "Refresh period too short, setting to 60 seconds");
            refreshPeriodSecs = 60;
            preferences.edit().putString(PrefKeys.PREF_COMMON_REFRESH_RATE, String.valueOf(refreshPeriodSecs)).apply();
        }
        long refreshPeriodMs = refreshPeriodSecs * 1000;
        boolean unmeteredOnly = preferences.getBoolean(PrefKeys.PREF_COMMON_UNMETERED_ONLY, PrefKeys.PREF_COMMON_UNMETERED_ONLY_DEFAULT);
        int networkType = unmeteredOnly ? JobInfo.NETWORK_TYPE_UNMETERED : JobInfo.NETWORK_TYPE_ANY;

        JobInfo jobInfo = new JobInfo.Builder(JOB_ID, new ComponentName(context, QuoteDownloaderService.class))
            .setPeriodic(refreshPeriodMs)
            .setRequiredNetworkType(networkType)
            .build();
        scheduler.schedule(jobInfo);

        Xlog.i(TAG, "Scheduled quote download job");
        Xlog.i(TAG, "Refresh period: %d", refreshPeriodSecs);
        Xlog.i(TAG, "Unmetered only: %s", unmeteredOnly);
    }

    public static void cancelQuoteDownloadJob(Context context) {
        JobScheduler scheduler = (JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        scheduler.cancel(JOB_ID);
        Xlog.i(TAG, "Canceled quote download job");
    }
}
