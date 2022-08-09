package com.crossbowffs.quotelock.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.crossbowffs.quotelock.data.modules.QuoteRepository
import com.crossbowffs.quotelock.utils.WorkUtils
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.className
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CommonReceiver : BroadcastReceiver() {
    @Inject
    lateinit var quoteRepository: QuoteRepository

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Xlog.d(TAG, "Received action: %s", action ?: "-")
        if (Intent.ACTION_BOOT_COMPLETED == action) {
            // Notify LockscreenHook to show current quotes after booting.
            quoteRepository.notifyBooted()
            WorkUtils.createQuoteDownloadWork(context, false)
        }
    }

    companion object {
        private val TAG = className<CommonReceiver>()
    }
}