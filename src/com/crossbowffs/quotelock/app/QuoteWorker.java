package com.crossbowffs.quotelock.app;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.crossbowffs.quotelock.api.QuoteData;
import com.crossbowffs.quotelock.utils.WorkUtils;
import com.crossbowffs.quotelock.utils.Xlog;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * @author Yubyf
 */
public class QuoteWorker extends ListenableWorker {
    public static final String TAG = QuoteWorker.class.getSimpleName();

    private class WorkerQuoteDownloaderTask extends QuoteDownloaderTask {
        private final CallbackToFutureAdapter.Completer<Result> mWorkerCompleter;

        private WorkerQuoteDownloaderTask(CallbackToFutureAdapter.Completer<Result> completer) {
            super(QuoteWorker.this.getApplicationContext());
            mWorkerCompleter = completer;
        }

        @Override
        protected void onPostExecute(QuoteData quote) {
            super.onPostExecute(quote);
            // We must back-off the work upon failure instead of creating
            // a new one; otherwise, the update time compensation algorithm
            // will schedule the next update immediately after this one.
            mWorkerCompleter.set(quote == null ? Result.retry() : Result.success());
            if (quote != null) {
                WorkUtils.createQuoteDownloadWork(mContext, true);
            }
            mUpdaterTask = null;
        }

        @Override
        protected void onCancelled(QuoteData quote) {
            super.onCancelled(quote);
            // No need to call #stop(), since #onStopped() will
            // handle the back-off for us.
            // stop();
            mUpdaterTask = null;
            mWorkerCompleter.setCancelled();
        }
    }

    private QuoteDownloaderTask mUpdaterTask;

    public QuoteWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        return CallbackToFutureAdapter.getFuture(completer -> {
            Xlog.d(TAG, "Quote downloader work started");

            if (!WorkUtils.shouldRefreshQuote(getApplicationContext())) {
                Xlog.d(TAG, "Should not refresh quote now, ignoring");
                completer.setCancelled();
                return TAG;
            }

            mUpdaterTask = new WorkerQuoteDownloaderTask(completer);
            mUpdaterTask.execute();
            return TAG;
        });
    }

    @Override
    public void onStopped() {
        super.onStopped();
        QuoteDownloaderTask task = mUpdaterTask;
        if (task != null) {
            // Cancel current work if it's not finished already.
            boolean canceled = task.cancel(true);
            if (canceled) {
                Xlog.e(TAG, "Aborted download work");
            } else {
                Xlog.e(TAG, "Attempted to abort completed download work");
            }
        } else {
            // Since the task is null, we either aborted the work in #startWork()
            // or the work already finished (so rescheduling has already been taken care of).
            // Apparently, this always happens when we create a new work, so we can
            // ignore this warning.
            Xlog.w(TAG, "Attempted to abort work, but updater task is null");
        }
    }
}
