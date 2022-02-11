package com.crossbowffs.quotelock.utils

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import androidx.work.*
import com.crossbowffs.quotelock.app.QuoteWorker
import com.crossbowffs.quotelock.consts.*
import java.util.concurrent.TimeUnit

object WorkUtils {
    private val TAG = WorkUtils::class.simpleName

    // TODO: Remove field compatibility annotation for java
    @JvmStatic
    fun shouldRefreshQuote(context: Context): Boolean {
        val preferences = context.getSharedPreferences(PREF_COMMON, Context.MODE_PRIVATE)

        // If our provider doesn't require internet access, we should always be
        // refreshing the quote.
        if (!preferences.getBoolean(PREF_COMMON_REQUIRES_INTERNET, true)) {
            Xlog.d(TAG, "WorkUtils#shouldRefreshQuote: YES (provider doesn't require internet)")
            return true
        }

        // If we're not connected to the internet, we shouldn't refresh the quote.
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val netInfo = manager?.activeNetworkInfo
        if (netInfo == null || !netInfo.isConnected) {
            Xlog.d(TAG, "WorkUtils#shouldRefreshQuote: NO (not connected to internet)")
            return false
        }

        // Check if we're on a metered connection and act according to the
        // user's preference.
        val unmeteredOnly =
            preferences.getBoolean(PREF_COMMON_UNMETERED_ONLY, PREF_COMMON_UNMETERED_ONLY_DEFAULT)
        if (unmeteredOnly && manager.isActiveNetworkMetered) {
            Xlog.d(TAG,
                "WorkUtils#shouldRefreshQuote: NO (can only update on unmetered connections)")
            return false
        }
        Xlog.d(TAG, "WorkUtils#shouldRefreshQuote: YES")
        return true
    }

    // TODO: Remove field compatibility annotation for java
    @JvmStatic
    fun createQuoteDownloadWork(context: Context, recreate: Boolean) {
        Xlog.d(TAG, "WorkUtils#createQuoteDownloadWork called, recreate == %s", recreate)

        // Instead of canceling the work whenever we disconnect from the
        // internet, we wait until the work executes. Upon execution, we re-check
        // the condition -- if network connectivity has been restored, we just
        // proceed as normal, otherwise, we do not reschedule the task until
        // we receive a network connectivity event.
        if (!shouldRefreshQuote(context)) {
            Xlog.d(TAG, "Should not create work under current conditions, ignoring")
            return
        }
        val workManager = WorkManager.getInstance(context)

        // If we're not forcing a work refresh (e.g. when a setting changes),
        // keep the request if the work already exists
        val existingWorkPolicy =
            if (recreate) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP
        Xlog.d(TAG, "ExistingWorkPolicy - $existingWorkPolicy")
        val delay = getUpdateDelay(context)
        val networkType = getNetworkType(context)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(networkType)
            .build()
        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(QuoteWorker::class.java)
            .setInitialDelay(delay.toLong(), TimeUnit.SECONDS)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR, (
                        2 * 1000).toLong(),
                TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .build()
        workManager.enqueueUniqueWork(
            QuoteWorker.TAG,
            existingWorkPolicy,
            oneTimeWorkRequest)
        Xlog.d(TAG, "Scheduled quote download work with delay: %d", delay)
    }

    private fun getUpdateDelay(context: Context): Int {
        // This compensates for the time since the last update in order
        // to ensure that the quote will be updated in a reasonable time
        // window. If the quote was updated >= refreshInterval time ago,
        // the update will be scheduled with zero delay.
        val commonPrefs = context.getSharedPreferences(PREF_COMMON, Context.MODE_PRIVATE)
        val quotePrefs = context.getSharedPreferences(PREF_QUOTES, Context.MODE_PRIVATE)
        val refreshInterval = getRefreshInterval(commonPrefs)
        val currentTime = System.currentTimeMillis()
        val lastUpdateTime = quotePrefs.getLong(PREF_QUOTES_LAST_UPDATED, currentTime)
        Xlog.d(TAG, "Current time: %d", currentTime)
        Xlog.d(TAG, "Last update time: %d", lastUpdateTime)
        var deltaSecs = ((currentTime - lastUpdateTime) / 1000).toInt()
        if (deltaSecs < 0) {
            // In case the user has a time machine or
            // changes the system clock back in time
            deltaSecs = 0
        }
        var delay = refreshInterval - deltaSecs
        if (delay < 0) {
            // This occurs if the user has been offline for
            // longer than the refresh interval
            delay = 0
        }
        return delay
    }

    private fun getRefreshInterval(commonPrefs: SharedPreferences): Int {
        var refreshInterval = commonPrefs.getInt(PREF_COMMON_REFRESH_RATE_OVERRIDE, 0)
        if (refreshInterval == 0) {
            val refreshIntervalStr =
                commonPrefs.getString(PREF_COMMON_REFRESH_RATE, PREF_COMMON_REFRESH_RATE_DEFAULT)!!
            refreshInterval = refreshIntervalStr.toInt()
        }
        if (refreshInterval < 60) {
            Xlog.w(TAG, "Refresh period too short, clamping to 60 seconds")
            refreshInterval = 60
        }
        return refreshInterval
    }

    private fun getNetworkType(context: Context): NetworkType {
        val preferences = context.getSharedPreferences(PREF_COMMON, Context.MODE_PRIVATE)
        if (!preferences.getBoolean(PREF_COMMON_REQUIRES_INTERNET, true)) {
            return NetworkType.NOT_REQUIRED
        }
        val unmeteredOnly =
            preferences.getBoolean(PREF_COMMON_UNMETERED_ONLY, PREF_COMMON_UNMETERED_ONLY_DEFAULT)
        return if (unmeteredOnly) NetworkType.UNMETERED else NetworkType.CONNECTED
    }
}