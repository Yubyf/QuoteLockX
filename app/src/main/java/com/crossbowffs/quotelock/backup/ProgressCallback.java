package com.crossbowffs.quotelock.backup;

import android.os.Looper;

import androidx.annotation.MainThread;

import com.crossbowffs.quotelock.utils.AppExecutors;

/**
 * @author Yubyf
 * @date 2021/6/13.
 */
public interface ProgressCallback {

    @MainThread
    void inProcessing(String message);

    @MainThread
    void success(String message);

    @MainThread
    void failure(String message);

    default void safeInProcessing(String message) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            AppExecutors.Companion.getInstance().mainThread().execute(() -> inProcessing(message));
            return;
        }
        inProcessing(message);
    }

    default void safeSuccess() {
        safeSuccess("");
    }

    default void safeSuccess(String message) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            AppExecutors.Companion.getInstance().mainThread().execute(() -> success(message));
            return;
        }
        success(message);
    }


    default void safeFailure(String message) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            AppExecutors.Companion.getInstance().mainThread().execute(() -> failure(message));
            return;
        }
        failure(message);
    }
}
