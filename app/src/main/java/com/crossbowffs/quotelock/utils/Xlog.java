package com.crossbowffs.quotelock.utils;

import android.util.Log;

import com.crossbowffs.quotelock.BuildConfig;

public final class Xlog {
    private static final String LOG_TAG = BuildConfig.LOG_TAG;
    private static final int LOG_LEVEL = BuildConfig.LOG_LEVEL;
    private static final boolean LOG_TO_XPOSED = BuildConfig.LOG_TO_XPOSED;

    private Xlog() {
    }

    private static void log(int priority, String tag, String message, Object... args) {
        if (priority < LOG_LEVEL) {
            return;
        }

        message = String.format(message, args);
        if (args.length > 0 && args[args.length - 1] instanceof Throwable) {
            Throwable throwable = (Throwable) args[args.length - 1];
            String stacktraceStr = Log.getStackTraceString(throwable);
            message = message + '\n' + stacktraceStr;
        }

        Log.println(priority, LOG_TAG, tag + ": " + message);
        if (LOG_TO_XPOSED) {
            Log.println(priority, "Xposed", LOG_TAG + ": " + tag + ": " + message);
        }
    }

    public static void v(String tag, String message, Object... args) {
        log(Log.VERBOSE, tag, message, args);
    }

    public static void d(String tag, String message, Object... args) {
        log(Log.DEBUG, tag, message, args);
    }

    public static void i(String tag, String message, Object... args) {
        log(Log.INFO, tag, message, args);
    }

    public static void w(String tag, String message, Object... args) {
        log(Log.WARN, tag, message, args);
    }

    public static void e(String tag, String message, Object... args) {
        log(Log.ERROR, tag, message, args);
    }
}
