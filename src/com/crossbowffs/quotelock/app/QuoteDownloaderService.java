package com.crossbowffs.quotelock.app;

import android.app.job.JobParameters;
import android.app.job.JobService;
import com.crossbowffs.quotelock.api.QuoteData;
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
            // We must back-off the job upon failure instead of creating
            // a new one; otherwise, the update time compensation algorithm
            // will schedule the next update immediately after this one.
            jobFinished(mJobParameters, quote == null);
            if (quote != null) {
                JobUtils.createQuoteDownloadJob(mContext, true);
            }
            mUpdaterTask = null;
        }

        @Override
        protected void onCancelled(QuoteData quote) {
            super.onCancelled(quote);
            // No need to call #jobFinished, since #onStopJob will
            // handle the back-off for us.
            // jobFinished(mJobParameters, true);
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

        if (!JobUtils.shouldRefreshQuote(this)) {
            Xlog.i(TAG, "Should not refresh quote now, ignoring");
            return false;
        }

        mUpdaterTask = new ServiceQuoteDownloaderTask(params);
        mUpdaterTask.execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        QuoteDownloaderTask task = mUpdaterTask;
        if (task != null) {
            // If the job already finished, we have either already
            // rescheduled or backed-off the task, so we do not submit
            // a new reschedule attempt from this method. If the job was
            // not canceled however, we need this method to reschedule
            // the job for us, so we return true.
            boolean canceled = task.cancel(true);
            if (canceled) {
                Xlog.e(TAG, "Aborted download job");
                return true;
            } else {
                Xlog.e(TAG, "Attempted to abort completed download job");
                return false;
            }
        } else {
            // Since the task is null, we either aborted the job in #onStartJob
            // (so we shouldn't reschedule) or the job already finished
            // (so rescheduling has already been taken care of).
            // Apparently, this always happens when we create a new job, so we can
            // ignore this warning.
            Xlog.w(TAG, "Attempted to abort job, but updater task is null");
            return false;
        }
    }
}
