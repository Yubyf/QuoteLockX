package com.crossbowffs.quotelock.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.crossbowffs.quotelock.app.QuoteWorker;
import com.crossbowffs.quotelock.consts.PrefKeys;

import java.util.concurrent.TimeUnit;

public class WorkUtils {
    private static final String TAG = WorkUtils.class.getSimpleName();

    public static boolean shouldRefreshQuote(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PrefKeys.PREF_COMMON, Context.MODE_PRIVATE);

        // If our provider doesn't require internet access, we should always be
        // refreshing the quote.
        if (!preferences.getBoolean(PrefKeys.PREF_COMMON_REQUIRES_INTERNET, true)) {
            Xlog.d(TAG, "WorkUtils#shouldRefreshQuote: YES (provider doesn't require internet)");
            return true;
        }

        // If we're not connected to the internet, we shouldn't refresh the quote.
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = manager.getActiveNetworkInfo();
        if (netInfo == null || !netInfo.isConnected()) {
            Xlog.d(TAG, "WorkUtils#shouldRefreshQuote: NO (not connected to internet)");
            return false;
        }

        // Check if we're on a metered connection and act according to the
        // user's preference.
        boolean unmeteredOnly = preferences.getBoolean(PrefKeys.PREF_COMMON_UNMETERED_ONLY, PrefKeys.PREF_COMMON_UNMETERED_ONLY_DEFAULT);
        if (unmeteredOnly && manager.isActiveNetworkMetered()) {
            Xlog.d(TAG, "WorkUtils#shouldRefreshQuote: NO (can only update on unmetered connections)");
            return false;
        }

        Xlog.d(TAG, "WorkUtils#shouldRefreshQuote: YES");
        return true;
    }

    public static void createQuoteDownloadWork(Context context, boolean recreate) {
        Xlog.d(TAG, "WorkUtils#createQuoteDownloadWork called, recreate == %s", recreate);

        // Instead of canceling the work whenever we disconnect from the
        // internet, we wait until the work executes. Upon execution, we re-check
        // the condition -- if network connectivity has been restored, we just
        // proceed as normal, otherwise, we do not reschedule the task until
        // we receive a network connectivity event.
        if (!shouldRefreshQuote(context)) {
            Xlog.d(TAG, "Should not create work under current conditions, ignoring");
            return;
        }

        WorkManager workManager = WorkManager.getInstance(context);

        // If we're not forcing a work refresh (e.g. when a setting changes),
        // keep the request if the work already exists
        ExistingWorkPolicy existingWorkPolicy = recreate ? ExistingWorkPolicy.REPLACE : ExistingWorkPolicy.KEEP;
        Xlog.d(TAG, "ExistingWorkPolicy - " + existingWorkPolicy.toString());

        int delay = getUpdateDelay(context);
        NetworkType networkType = getNetworkType(context);

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(networkType)
                .build();

        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(QuoteWorker.class)
                .setInitialDelay(delay, TimeUnit.SECONDS)
                .setBackoffCriteria(
                        BackoffPolicy.LINEAR,
                        2 * 1000,
                        TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .build();
        workManager.enqueueUniqueWork(
                QuoteWorker.TAG,
                existingWorkPolicy,
                oneTimeWorkRequest);

        Xlog.d(TAG, "Scheduled quote download work with delay: %d", delay);
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
        Xlog.d(TAG, "Current time: %d", currentTime);
        Xlog.d(TAG, "Last update time: %d", lastUpdateTime);
        int deltaSecs = (int) ((currentTime - lastUpdateTime) / 1000);
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

    private static NetworkType getNetworkType(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PrefKeys.PREF_COMMON, Context.MODE_PRIVATE);
        if (!preferences.getBoolean(PrefKeys.PREF_COMMON_REQUIRES_INTERNET, true)) {
            return NetworkType.NOT_REQUIRED;
        }
        boolean unmeteredOnly = preferences.getBoolean(PrefKeys.PREF_COMMON_UNMETERED_ONLY, PrefKeys.PREF_COMMON_UNMETERED_ONLY_DEFAULT);
        return unmeteredOnly ? NetworkType.UNMETERED : NetworkType.CONNECTED;
    }
}
