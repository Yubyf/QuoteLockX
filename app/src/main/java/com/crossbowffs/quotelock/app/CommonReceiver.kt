package com.crossbowffs.quotelock.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.crossbowffs.quotelock.consts.PREF_BOOT_NOTIFY_FLAG
import com.crossbowffs.quotelock.data.quotesDataStore
import com.crossbowffs.quotelock.utils.WorkUtils
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.className

class CommonReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Xlog.d(TAG, "Received action: %s", action ?: "-")
        if (Intent.ACTION_BOOT_COMPLETED == action) {
            // Notify LockscreenHook to show current quotes after booting.
            notifyBooted()
            WorkUtils.createQuoteDownloadWork(context, false)
        }
    }

    private fun notifyBooted() {
        val hasBootNotifyFlag = quotesDataStore.contains(PREF_BOOT_NOTIFY_FLAG)
        if (!hasBootNotifyFlag) {
            quotesDataStore.putInt(PREF_BOOT_NOTIFY_FLAG, 0)
        } else {
            quotesDataStore.remove(PREF_BOOT_NOTIFY_FLAG)
        }
    }

    companion object {
        private val TAG = className<CommonReceiver>()
    }
}