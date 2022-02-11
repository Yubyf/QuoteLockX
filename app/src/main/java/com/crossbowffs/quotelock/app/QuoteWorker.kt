package com.crossbowffs.quotelock.app

import android.content.Context
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.concurrent.futures.CallbackToFutureAdapter.Completer
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.crossbowffs.quotelock.api.QuoteData
import com.crossbowffs.quotelock.app.QuoteWorker
import com.crossbowffs.quotelock.utils.WorkUtils
import com.crossbowffs.quotelock.utils.Xlog
import com.google.common.util.concurrent.ListenableFuture

/**
 * @author Yubyf
 */
class QuoteWorker(context: Context, workerParams: WorkerParameters) :
    ListenableWorker(context, workerParams) {

    private inner class WorkerQuoteDownloaderTask constructor(private val mWorkerCompleter: Completer<Result>) :
        QuoteDownloaderTask(this@QuoteWorker.applicationContext) {
        override fun onPostExecute(quote: QuoteData?) {
            super.onPostExecute(quote)
            // We must back-off the work upon failure instead of creating
            // a new one; otherwise, the update time compensation algorithm
            // will schedule the next update immediately after this one.
            mWorkerCompleter.set(if (quote == null) Result.retry() else Result.success())
            if (quote != null) {
                WorkUtils.createQuoteDownloadWork(mContext, true)
            }
            mUpdaterTask = null
        }

        override fun onCancelled(quote: QuoteData?) {
            super.onCancelled(quote)
            // No need to call #stop(), since #onStopped() will
            // handle the back-off for us.
            // stop();
            mUpdaterTask = null
            mWorkerCompleter.setCancelled()
        }
    }

    private var mUpdaterTask: QuoteDownloaderTask? = null
    override fun startWork(): ListenableFuture<Result> {
        return CallbackToFutureAdapter.getFuture { completer: Completer<Result> ->
            Xlog.d(TAG, "Quote downloader work started")
            if (!WorkUtils.shouldRefreshQuote(applicationContext)) {
                Xlog.d(TAG, "Should not refresh quote now, ignoring")
                completer.setCancelled()
                return@getFuture TAG
            }
            mUpdaterTask = WorkerQuoteDownloaderTask(completer).apply {
                this.execute()
            }
            TAG
        }
    }

    override fun onStopped() {
        super.onStopped()
        val task = mUpdaterTask
        if (task != null) {
            // Cancel current work if it's not finished already.
            val canceled = task.cancel(true)
            if (canceled) {
                Xlog.e(TAG, "Aborted download work")
            } else {
                Xlog.e(TAG, "Attempted to abort completed download work")
            }
        } else {
            // Since the task is null, we either aborted the work in #startWork()
            // or the work already finished (so rescheduling has already been taken care of).
            // Apparently, this always happens when we create a new work, so we can
            // ignore this warning.
            Xlog.w(TAG, "Attempted to abort work, but updater task is null")
        }
    }

    companion object {
        val TAG = QuoteWorker::class.simpleName ?: ""
    }
}