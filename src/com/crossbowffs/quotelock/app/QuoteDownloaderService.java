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
            jobFinished(mJobParameters, quote == null);
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
            return true;
        }
        return false;
    }

    private boolean isUpdateTooFrequent() {
        // TOO SOON! YOU HAVE AWAKENED ME TOO SOON, EXECUTUS!
        // This is a workaround for a weird bug where the job is somehow
        // executed too frequently (sometimes once every few seconds)
        // I'm not sure what the cause is; it's either incorrect API usage
        // or an Android bug.
        SharedPreferences quotePrefs = getSharedPreferences(PrefKeys.PREF_QUOTES, MODE_PRIVATE);
        long lastUpdated = quotePrefs.getLong(PrefKeys.PREF_QUOTES_LAST_UPDATED, 0);
        SharedPreferences commonPrefs = getSharedPreferences(PrefKeys.PREF_COMMON, MODE_PRIVATE);
        String refreshRateStr = commonPrefs.getString(PrefKeys.PREF_COMMON_REFRESH_RATE, PrefKeys.PREF_COMMON_REFRESH_RATE_DEFAULT);
        int refreshRate = Integer.parseInt(refreshRateStr);
        Xlog.i(TAG, "Last update time: " + lastUpdated);
        Xlog.i(TAG, "Current time: " + System.currentTimeMillis());
        Xlog.i(TAG, "Refresh rate: " + refreshRate);
        // If we have not reached 90% of the update interval (90% * 1000ms/s == 900),
        // skip this update and wait for the next one. Use abs() in case the user
        // changes their system time to a point in the past (at worst we miss ~2 updates).
        return (Math.abs(System.currentTimeMillis() - lastUpdated) < refreshRate * 900);
    }
}
