package com.crossbowffs.quotelock.utils

import android.util.Log
import com.crossbowffs.quotelock.BuildConfig


private const val LOG_TAG = BuildConfig.LOG_TAG
private const val LOG_LEVEL = BuildConfig.LOG_LEVEL
private const val LOG_TO_XPOSED = BuildConfig.LOG_TO_XPOSED

object Xlog {
    private fun log(priority: Int, tag: String?, message: String, vararg args: Any) {
        if (priority < LOG_LEVEL) {
            return
        }
        var msg = String.format(message, *args)
        if (args.isNotEmpty() && args[args.size - 1] is Throwable) {
            val throwable = args[args.size - 1] as Throwable
            val stacktraceStr = Log.getStackTraceString(throwable)
            msg = """
                $msg
                $stacktraceStr
                """.trimIndent()
        }
        Log.println(priority, LOG_TAG, "${tag?.let { "$it:" }} $msg")
        if (LOG_TO_XPOSED) {
            Log.println(priority, "Xposed", "$LOG_TAG: ${tag?.let { "$it: " }}$msg")
        }
    }

    // TODO: Remove field compatibility annotation for java
    @JvmStatic
    fun v(tag: String?, message: String, vararg args: Any) {
        log(Log.VERBOSE, tag, message, *args)
    }

    // TODO: Remove field compatibility annotation for java
    @JvmStatic
    fun d(tag: String?, message: String, vararg args: Any) {
        log(Log.DEBUG, tag, message, *args)
    }

    // TODO: Remove field compatibility annotation for java
    @JvmStatic
    fun i(tag: String?, message: String, vararg args: Any) {
        log(Log.INFO, tag, message, *args)
    }

    // TODO: Remove field compatibility annotation for java
    @JvmStatic
    fun w(tag: String?, message: String, vararg args: Any) {
        log(Log.WARN, tag, message, *args)
    }

    // TODO: Remove field compatibility annotation for java
    @JvmStatic
    fun e(tag: String?, message: String, vararg args: Any) {
        log(Log.ERROR, tag, message, *args)
    }
}