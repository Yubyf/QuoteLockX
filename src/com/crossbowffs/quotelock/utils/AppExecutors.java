package com.crossbowffs.quotelock.utils;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author LiuPengyu
 */
public class AppExecutors {

    private static final String DISK_IO_THREAD_NAME = "disk-io-pool-%d";
    private static final String PROCESS_THREAD_NAME = "process-pool-%d";
    private static final Object LOCK = new Object();
    private static volatile AppExecutors sInstance;
    private final ExecutorService diskIO;
    private final Executor mainThread;
    private final ExecutorService processThread;

    private AppExecutors(ExecutorService diskIO, Executor mainThread, ExecutorService processThread) {
        this.diskIO = diskIO;
        this.mainThread = mainThread;
        this.processThread = processThread;
    }

    public static AppExecutors getInstance() {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = new AppExecutors(
                            buildSingleThreadPool(DISK_IO_THREAD_NAME),
                            new MainThreadExecutor(),
                            buildSingleThreadPool(PROCESS_THREAD_NAME));
                }
            }
        }
        return sInstance;
    }

    public ExecutorService diskIO() {
        return diskIO;
    }

    public Executor mainThread() {
        return mainThread;
    }

    public ExecutorService processThread() {
        return processThread;
    }

    private static class MainThreadExecutor implements Executor {
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command) {
            mainThreadHandler.post(command);
        }
    }

    public static ExecutorService buildSingleThreadPool(String nameFormat) {
        return new ThreadPoolExecutor(1, 1, 0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1024),
                buildThreadFactory(nameFormat),
                new ThreadPoolExecutor.AbortPolicy());
    }

    public static ExecutorService buildFixedThreadPool(String nameFormat, int count) {
        return new ThreadPoolExecutor(count, count, 0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1024),
                buildThreadFactory(nameFormat),
                new ThreadPoolExecutor.AbortPolicy());
    }

    private static ThreadFactory buildThreadFactory(String nameFormat) {
        return buildThreadFactory(nameFormat, false);
    }

    private static ThreadFactory buildThreadFactory(String nameFormat, boolean isDaemon) {
        AtomicLong threadCount = new AtomicLong(0);
        return new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = Executors.defaultThreadFactory().newThread(r);
                if (nameFormat != null) {
                    thread.setName(String.format(nameFormat, threadCount.getAndIncrement()));
                }
                thread.setDaemon(isDaemon);
                return thread;
            }
        };
    }

    public static ScheduledExecutorService buildSingleScheduledThreadPool(String nameFormat) {
        return buildFixedScheduledThreadPool(nameFormat, 1);
    }

    public static ScheduledExecutorService buildFixedScheduledThreadPool(String nameFormat, int count) {
        return new ScheduledThreadPoolExecutor(count,
                buildThreadFactory(nameFormat, true));
    }
}
