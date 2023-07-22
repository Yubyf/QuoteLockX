package com.crossbowffs.quotelock.worker

import android.content.Context
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.concurrent.futures.CallbackToFutureAdapter.Completer
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.crossbowffs.quotelock.data.ConfigurationRepository
import com.crossbowffs.quotelock.data.modules.QuoteRepository
import com.crossbowffs.quotelock.utils.WorkUtils
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.className
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * @author Yubyf
 */
class QuoteWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val quoteRepository: QuoteRepository,
    private val configurationRepository: ConfigurationRepository,
) : ListenableWorker(context, workerParams) {

    private var mUpdaterJob: Job? = null
    override fun startWork(): ListenableFuture<Result> {
        return CallbackToFutureAdapter.getFuture { completer: Completer<Result> ->
            Xlog.d(TAG, "Quote downloader work started")
            if (!WorkUtils.shouldRefreshQuote(
                    applicationContext,
                    configurationRepository.isRequireInternet,
                    configurationRepository.isUnmeteredNetworkOnly
                )
            ) {
                Xlog.d(TAG, "Should not refresh quote now, ignoring")
                completer.setCancelled()
                return@getFuture TAG
            }
            mUpdaterJob = CoroutineScope(Dispatchers.Default).launch {
                val quote = try {
                    quoteRepository.downloadQuote()
                } catch (e: CancellationException) {
                    completer.setCancelled()
                    mUpdaterJob = null
                    return@launch
                }
                // We must back-off the work upon failure instead of creating
                // a new one; otherwise, the update time compensation algorithm
                // will schedule the next update immediately after this one.
                completer.set(if (quote == null) Result.retry() else Result.success())
                if (quote != null) {
                    WorkUtils.createQuoteDownloadWork(
                        applicationContext,
                        configurationRepository.refreshInterval,
                        configurationRepository.isRequireInternet,
                        configurationRepository.isUnmeteredNetworkOnly,
                        true
                    )
                }
                mUpdaterJob = null
            }
            TAG
        }
    }

    override fun onStopped() {
        super.onStopped()
        if (mUpdaterJob != null) {
            // Cancel current work if it's not finished already.
            if (mUpdaterJob?.isActive == true) {
                mUpdaterJob?.cancel()
                Xlog.e(TAG, "Aborted download work")
            } else {
                Xlog.e(TAG, "Attempted to abort completed download work")
            }
        } else {
            // Since the downloader job is null, we either aborted the work in #startWork()
            // or the work already finished (so rescheduling has already been taken care of).
            // Apparently, this always happens when we create a new work, so we can
            // ignore this warning.
            Xlog.w(TAG, "Attempted to abort job, but updater task is null")
        }
    }

    companion object {
        val TAG = className<QuoteWorker>()
    }
}