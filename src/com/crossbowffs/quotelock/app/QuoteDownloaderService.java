package com.crossbowffs.quotelock.app;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
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

        if (checkMeteredNetwork()) {
            Xlog.i(TAG, "Current network is metered, ignoring");
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

    private boolean checkMeteredNetwork() {
        SharedPreferences preferences = getSharedPreferences(PrefKeys.PREF_COMMON, MODE_PRIVATE);
        boolean unmeteredOnly = preferences.getBoolean(PrefKeys.PREF_COMMON_UNMETERED_ONLY, PrefKeys.PREF_COMMON_UNMETERED_ONLY_DEFAULT);
        if (!unmeteredOnly) {
            return false;
        }

        ConnectivityManager manager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        return manager.isActiveNetworkMetered();
    }
}
