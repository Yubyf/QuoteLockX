package com.crossbowffs.quotelock.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.crossbowffs.quotelock.di.VersionEntryPoint
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.className
import dagger.hilt.android.EntryPointAccessors

/**
 * @author Yubyf
 */
class VersionWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    private val versionRepository = EntryPointAccessors.fromApplication(
        context.applicationContext,
        VersionEntryPoint::class.java
    ).versionRepository()

    override suspend fun doWork(): Result {
        return runCatching {
            Xlog.d(TAG, "Version check update work started")
            versionRepository.fetchUpdate()
            Result.success()
        }.getOrElse {
            Xlog.e(TAG, "Version check update work failed", it)
            if (runAttemptCount < 10) {
                // Exponential backoff strategy will avoid the request to repeat
                // too fast in case of failures.
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        val TAG = className<VersionWorker>()
    }
}