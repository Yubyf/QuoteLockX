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
        if (!shouldRefreshQuote(context)) {
            Xlog.i(TAG, "Should not create job under current conditions, ignoring");
            return;
        }

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

        int delay = getUpdateDelay(context);
        int networkType = getNetworkType(context);
        JobInfo jobInfo = new JobInfo.Builder(JOB_ID, new ComponentName(context, QuoteDownloaderService.class))
            .setMinimumLatency(delay * 1000)
            .setOverrideDeadline(delay * 1200) // Good balance between battery life and time accuracy
            .setRequiredNetworkType(networkType)
            .build();
        scheduler.schedule(jobInfo);

        Xlog.i(TAG, "Scheduled quote download job");
    }

    private static int getUpdateDelay(Context context) {
        // This compensates for the time since the last update in order
        // to ensure that the quote will be updated in a reasonable time
        // window. If the quote was updated >= refreshInterval time ago,
        // the update will be scheduled with zero delay.
        SharedPreferences commonPrefs = context.getSharedPreferences(PrefKeys.PREF_COMMON, Context.MODE_PRIVATE);
        SharedPreferences quotePrefs = context.getSharedPreferences(PrefKeys.PREF_QUOTES, Context.MODE_PRIVATE);
        int refreshInterval = getRefreshInterval(commonPrefs);
        long currentTime = System.currentTimeMillis();
        long lastUpdateTime = quotePrefs.getLong(PrefKeys.PREF_QUOTES_LAST_UPDATED, currentTime);
        Xlog.i(TAG, "Current time: %d", currentTime);
        Xlog.i(TAG, "Last update time: %d", lastUpdateTime);
        int deltaSecs = (int)((currentTime - lastUpdateTime) / 1000);
        if (deltaSecs < 0) {
            // In case the user has a time machine or
            // changes the system clock back in time
            deltaSecs = 0;
        }
        int delay = refreshInterval - deltaSecs;
        if (delay < 0) {
            // This occurs if the user has been offline for
            // longer than the refresh interval
            delay = 0;
        }
        return delay;
    }

    private static int getRefreshInterval(SharedPreferences commonPrefs) {
        int refreshInterval = commonPrefs.getInt(PrefKeys.PREF_COMMON_REFRESH_RATE_OVERRIDE, 0);
        if (refreshInterval == 0) {
            String refreshIntervalStr = commonPrefs.getString(PrefKeys.PREF_COMMON_REFRESH_RATE, PrefKeys.PREF_COMMON_REFRESH_RATE_DEFAULT);
            refreshInterval = Integer.parseInt(refreshIntervalStr);
        }
        if (refreshInterval < 60) {
            Xlog.w(TAG, "Refresh period too short, clamping to 60 seconds");
            refreshInterval = 60;
        }
        return refreshInterval;
    }

    private static int getNetworkType(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PrefKeys.PREF_COMMON, Context.MODE_PRIVATE);
        boolean unmeteredOnly = preferences.getBoolean(PrefKeys.PREF_COMMON_UNMETERED_ONLY, PrefKeys.PREF_COMMON_UNMETERED_ONLY_DEFAULT);
        int networkType = unmeteredOnly ? JobInfo.NETWORK_TYPE_UNMETERED : JobInfo.NETWORK_TYPE_ANY;
        return networkType;
    }
}
