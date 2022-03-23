package com.crossbowffs.quotelock.backup

import android.os.Looper
import androidx.annotation.MainThread
import com.crossbowffs.quotelock.utils.mainScope
import kotlinx.coroutines.launch

/**
 * @author Yubyf
 * @date 2021/6/13.
 */
interface ProgressCallback {
    @MainThread
    fun inProcessing(message: String?)

    @MainThread
    fun success(message: String?)

    @MainThread
    fun failure(message: String?)

    fun safeInProcessing(message: String?) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainScope.launch { inProcessing(message) }
            return
        }
        inProcessing(message)
    }

    fun safeSuccess(message: String? = "") {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainScope.launch { success(message) }
            return
        }
        success(message)
    }

    fun safeFailure(message: String?) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainScope.launch { failure(message) }
            return
        }
        failure(message)
    }
}