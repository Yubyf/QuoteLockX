package com.crossbowffs.quotelock.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import com.crossbowffs.quotelock.consts.PREF_BOOT_NOTIFY_FLAG
import com.crossbowffs.quotelock.consts.PREF_QUOTES
import com.crossbowffs.quotelock.utils.WorkUtils
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.className

class CommonReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Xlog.d(TAG, "Received action: %s", action ?: "-")
        if (Intent.ACTION_BOOT_COMPLETED == action || ConnectivityManager.CONNECTIVITY_ACTION == action) {
            if (Intent.ACTION_BOOT_COMPLETED == action) {
                // Notify LockscreenHook to show current quotes after booting.
                notifyBooted(context)
            }
            WorkUtils.createQuoteDownloadWork(context, false)
        }
    }

    private fun notifyBooted(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREF_QUOTES, Context.MODE_PRIVATE)
        val hasBootNotifyFlag = sharedPreferences.contains(PREF_BOOT_NOTIFY_FLAG)
        if (!hasBootNotifyFlag) {
            sharedPreferences.edit().putInt(PREF_BOOT_NOTIFY_FLAG, 0).apply()
        } else {
            sharedPreferences.edit().remove(PREF_BOOT_NOTIFY_FLAG).apply()
        }
    }

    companion object {
        private val TAG = className<CommonReceiver>()
    }
}