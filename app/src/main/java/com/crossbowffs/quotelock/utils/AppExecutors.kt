package com.crossbowffs.quotelock.utils

import android.os.Handler
import android.os.Looper
import java.util.concurrent.*
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy
import java.util.concurrent.atomic.AtomicLong

/**
 * @author Yubyf
 *
 * TODO: Replace with coroutine
 */
class AppExecutors private constructor(
    private val diskIO: ExecutorService,
    private val mainThread: Executor,
    private val processThread: ExecutorService,
) {
    fun diskIO(): ExecutorService {
        return diskIO
    }

    fun mainThread(): Executor {
        return mainThread
    }

    fun processThread(): ExecutorService {
        return processThread
    }

    private class MainThreadExecutor : Executor {
        private val mainThreadHandler = Handler(Looper.getMainLooper())
        override fun execute(command: Runnable) {
            mainThreadHandler.post(command)
        }
    }

    companion object {
        private const val DISK_IO_THREAD_NAME = "disk-io-pool-%d"
        private const val PROCESS_THREAD_NAME = "process-pool-%d"
        val instance: AppExecutors by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            AppExecutors(
                buildSingleThreadPool(DISK_IO_THREAD_NAME),
                MainThreadExecutor(),
                buildSingleThreadPool(PROCESS_THREAD_NAME)
            )
        }

        fun buildSingleThreadPool(nameFormat: String?): ExecutorService {
            return ThreadPoolExecutor(
                1, 1, 0L,
                TimeUnit.MILLISECONDS,
                LinkedBlockingQueue(1024),
                buildThreadFactory(nameFormat),
                AbortPolicy()
            )
        }

        fun buildFixedThreadPool(nameFormat: String?, count: Int): ExecutorService {
            return ThreadPoolExecutor(
                count, count, 0L,
                TimeUnit.MILLISECONDS,
                LinkedBlockingQueue(1024),
                buildThreadFactory(nameFormat),
                AbortPolicy()
            )
        }

        private fun buildThreadFactory(
            nameFormat: String?,
            isDaemon: Boolean = false,
        ): ThreadFactory {
            val threadCount = AtomicLong(0)
            return ThreadFactory { r ->
                val thread = Executors.defaultThreadFactory().newThread(r)
                if (nameFormat != null) {
                    thread.name = String.format(nameFormat, threadCount.getAndIncrement())
                }
                thread.isDaemon = isDaemon
                thread
            }
        }

        fun buildSingleScheduledThreadPool(nameFormat: String?): ScheduledExecutorService {
            return buildFixedScheduledThreadPool(nameFormat, 1)
        }

        fun buildFixedScheduledThreadPool(
            nameFormat: String?,
            count: Int,
        ): ScheduledExecutorService {
            return ScheduledThreadPoolExecutor(
                count,
                buildThreadFactory(nameFormat, true)
            )
        }
    }
}