package com.crossbowffs.quotelock.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.crossbowffs.quotelock.data.ConfigurationRepository
import com.crossbowffs.quotelock.data.modules.QuoteRepository
import com.crossbowffs.quotelock.utils.WorkUtils
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.className
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CommonReceiver : BroadcastReceiver(), KoinComponent {

    private val quoteRepository: QuoteRepository by inject()

    private val configurationRepository: ConfigurationRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Xlog.d(TAG, "Received action: %s", action ?: "-")
        if (Intent.ACTION_BOOT_COMPLETED == action) {
            // Notify LockscreenHook to show current quotes after booting.
            quoteRepository.notifyBooted()
            WorkUtils.createQuoteDownloadWork(
                context,
                configurationRepository.refreshInterval,
                configurationRepository.isRequireInternet,
                configurationRepository.isUnmeteredNetworkOnly,
                false
            )
        }
    }

    companion object {
        private val TAG = className<CommonReceiver>()
    }
}