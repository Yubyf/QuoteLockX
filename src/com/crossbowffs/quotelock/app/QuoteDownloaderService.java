package com.crossbowffs.quotelock.app;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.AsyncTask;
import com.crossbowffs.quotelock.model.VnaasQuote;
import com.crossbowffs.quotelock.utils.JobUtils;

public class QuoteDownloaderService extends JobService {
    private class ServiceQuoteDownloaderTask extends QuoteDownloaderTask {
        private final JobParameters mJobParameters;

        private ServiceQuoteDownloaderTask(JobParameters parameters) {
            super(QuoteDownloaderService.this);
            mJobParameters = parameters;
        }

        @Override
        protected void onPostExecute(VnaasQuote vnaasQuote) {
            super.onPostExecute(vnaasQuote);
            jobFinished(mJobParameters, vnaasQuote == null);
            mUpdaterTask = null;
        }

        @Override
        protected void onCancelled(VnaasQuote vnaasQuote) {
            mUpdaterTask = null;
        }
    }

    private QuoteDownloaderTask mUpdaterTask;

    @Override
    public boolean onStartJob(JobParameters params) {
        if (params.getJobId() != JobUtils.JOB_ID) {
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
}
