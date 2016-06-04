package com.crossbowffs.quotelock.app;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import com.crossbowffs.quotelock.api.QuoteData;
import com.crossbowffs.quotelock.preferences.PrefKeys;
import com.crossbowffs.quotelock.utils.JobUtils;
import com.crossbowffs.quotelock.utils.Xlog;

public class QuoteDownloaderService extends JobService {
    private static final String TAG = QuoteDownloaderService.class.getSimpleName();

    private class ServiceQuoteDownloaderTask extends QuoteDownloaderTask {
        private final JobParameters mJobParameters;

        private ServiceQuoteDownloaderTask(JobParameters parameters) {
            super(QuoteDownloaderService.this);
            mJobParameters = parameters;
        }

        @Override
        protected void onPostExecute(QuoteData quote) {
            super.onPostExecute(quote);
            // Must pass false for needsReschedule, even if there was an error
            // Otherwise, the job will get duplicated (this is an Android bug)
            jobFinished(mJobParameters, false);
            mUpdaterTask = null;
        }

        @Override
        protected void onCancelled(QuoteData quote) {
            super.onCancelled(quote);
            mUpdaterTask = null;
        }
    }

    private QuoteDownloaderTask mUpdaterTask;

    @Override
    public boolean onStartJob(JobParameters params) {
        Xlog.i(TAG, "Quote downloader job started");

        if (params.getJobId() != JobUtils.JOB_ID) {
            Xlog.e(TAG, "Job ID mismatch, ignoring");
            return false;
        }

        if (isUpdateTooFrequent()) {
            Xlog.w(TAG, "Time elapsed since last update too short, ignoring");
            return false;
        }

        mUpdaterTask = new ServiceQuoteDownloaderTask(params);
        mUpdaterTask.execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if (mUpdaterTask != null && mUpdaterTask.getStatus() != AsyncTask.Status.FINISHED) {
            mUpdaterTask.cancel(true);
            Xlog.e(TAG, "Aborted download job");
        }
        return false;
    }

    private boolean isUpdateTooFrequent() {
        // TOO SOON! YOU HAVE AWAKENED ME TOO SOON, EXECUTUS!
        // This is a workaround for a weird bug where the job is somehow
        // executed too frequently (sometimes once every few seconds)
        // I'm not sure what the cause is; it's either incorrect API usage
        // or an Android bug.
        SharedPreferences commonPrefs = getSharedPreferences(PrefKeys.PREF_COMMON, MODE_PRIVATE);
        long lastUpdated = commonPrefs.getLong(PrefKeys.PREF_COMMON_QUOTE_LAST_UPDATED, 0);
        int refreshRate = JobUtils.getRefreshInterval(commonPrefs);
        long currentTime = System.currentTimeMillis();
        Xlog.d(TAG, "Last update time: " + lastUpdated);
        Xlog.d(TAG, "Current time: " + currentTime);
        Xlog.d(TAG, "Refresh rate: " + refreshRate);
        // If we have not reached 80% of the update interval (80% * 1000ms/s == 800),
        // skip this update and wait for the next one. Use abs() in case the user
        // changes their system time to a point in the past (at worst we miss ~2 updates).
        if (Math.abs(currentTime - lastUpdated) < refreshRate * 800) {
            return true;
        } else {
            commonPrefs.edit().putLong(PrefKeys.PREF_COMMON_QUOTE_LAST_UPDATED, currentTime).apply();
            return false;
        }
    }
}
