package com.crossbowffs.quotelock.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.work.*
import com.crossbowffs.quotelock.di.QuoteProviderEntryPoint
import com.crossbowffs.quotelock.worker.QuoteWorker
import dagger.hilt.android.EntryPointAccessors
import java.util.concurrent.TimeUnit

object WorkUtils {
    private val TAG = className<WorkUtils>()

    /** Reference: https://stackoverflow.com/a/53532456/4985530 */
    private fun isInternetAvailable(context: Context): Boolean {
        var result = false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            result = when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.run {
                @Suppress("DEPRECATION")
                connectivityManager.activeNetworkInfo?.run {
                    isConnected
                    result = when (type) {
                        ConnectivityManager.TYPE_WIFI,
                        ConnectivityManager.TYPE_MOBILE,
                        ConnectivityManager.TYPE_ETHERNET,
                        -> true
                        else -> false
                    }
                }
            }
        }
        return result
    }

    fun shouldRefreshQuote(
        context: Context,
        isRequireInternet: Boolean,
        isUnmeteredNetworkOnly: Boolean,
    ): Boolean {
        // If our provider doesn't require internet access, we should always be
        // refreshing the quote.
        if (!isRequireInternet) {
            Xlog.d(TAG, "WorkUtils#shouldRefreshQuote: YES (provider doesn't require internet)")
            return true
        }

        // If we're not connected to the internet, we shouldn't refresh the quote.
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (!isInternetAvailable(context)) {
            Xlog.d(TAG, "WorkUtils#shouldRefreshQuote: NO (not connected to internet)")
            return false
        }

        // Check if we're on a metered connection and act according to the
        // user's preference.
        if (isUnmeteredNetworkOnly && manager?.isActiveNetworkMetered == true) {
            Xlog.d(TAG,
                "WorkUtils#shouldRefreshQuote: NO (can only update on unmetered connections)")
            return false
        }
        Xlog.d(TAG, "WorkUtils#shouldRefreshQuote: YES")
        return true
    }

    fun createQuoteDownloadWork(
        context: Context,
        refreshInterval: Int,
        isRequireInternet: Boolean,
        isUnmeteredNetworkOnly: Boolean,
        recreate: Boolean,
    ) {
        Xlog.d(TAG, "WorkUtils#createQuoteDownloadWork called, recreate == %s", recreate)

        // Instead of canceling the work whenever we disconnect from the
        // internet, we wait until the work executes. Upon execution, we re-check
        // the condition -- if network connectivity has been restored, we just
        // proceed as normal, otherwise, we do not reschedule the task until
        // we receive a network connectivity event.
        if (!shouldRefreshQuote(context, isRequireInternet, isUnmeteredNetworkOnly)) {
            Xlog.d(TAG, "Should not create work under current conditions, ignoring")
            return
        }
        val workManager = WorkManager.getInstance(context)

        // If we're not forcing a work refresh (e.g. when a setting changes),
        // keep the request if the work already exists
        val existingWorkPolicy =
            if (recreate) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP
        Xlog.d(TAG, "ExistingWorkPolicy - $existingWorkPolicy")
        val delay = getQuoteUpdateDelay(context, ensureQuoteRefreshInterval(refreshInterval))
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(getNetworkType(isRequireInternet, isUnmeteredNetworkOnly))
            .build()
        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<QuoteWorker>()
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

    private fun getQuoteUpdateDelay(context: Context, refreshInterval: Int): Int {
        // This compensates for the time since the last update in order
        // to ensure that the quote will be updated in a reasonable time
        // window. If the quote was updated >= refreshInterval time ago,
        // the update will be scheduled with zero delay.
        val currentTime = System.currentTimeMillis()
        val quoteRepository = EntryPointAccessors.fromApplication(
            context.applicationContext,
            QuoteProviderEntryPoint::class.java
        ).quoteRepository()
        val lastUpdateTime = quoteRepository.getLastUpdateTime().takeIf { it > 0L } ?: currentTime
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

    private fun ensureQuoteRefreshInterval(refreshInterval: Int): Int {
        return if (refreshInterval < 60) {
            Xlog.w(TAG, "Refresh period too short, clamping to 60 seconds")
            60
        } else refreshInterval
    }

    private fun getNetworkType(
        isRequireInternet: Boolean,
        isUnmeteredNetworkOnly: Boolean,
    ): NetworkType {
        return when {
            !isRequireInternet -> NetworkType.NOT_REQUIRED
            isUnmeteredNetworkOnly -> NetworkType.UNMETERED
            else -> NetworkType.CONNECTED
        }
    }
}