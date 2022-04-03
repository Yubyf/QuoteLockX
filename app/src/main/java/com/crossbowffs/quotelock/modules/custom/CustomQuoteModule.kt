package com.crossbowffs.quotelock.modules.custom

import android.content.ComponentName
import android.content.Context
import com.crossbowffs.quotelock.api.QuoteData
import com.crossbowffs.quotelock.api.QuoteModule
import com.crossbowffs.quotelock.api.QuoteModule.Companion.CHARACTER_TYPE_DEFAULT
import com.crossbowffs.quotelock.modules.custom.app.CustomQuoteConfigActivity
import com.crossbowffs.quotelock.modules.custom.database.customQuoteDatabase
import com.yubyf.quotelockx.R
import kotlinx.coroutines.flow.firstOrNull

class CustomQuoteModule : QuoteModule {
    override fun getDisplayName(context: Context): String {
        return context.getString(R.string.module_custom_name)
    }

    override fun getConfigActivity(context: Context): ComponentName? {
        return ComponentName(context, CustomQuoteConfigActivity::class.java)
    }

    override fun getMinimumRefreshInterval(context: Context): Int {
        return 0
    }

    override fun requiresInternetConnectivity(context: Context): Boolean {
        return false
    }

    @Throws(Exception::class)
    override suspend fun getQuote(context: Context): QuoteData {
        return customQuoteDatabase.dao().getRandomItem().firstOrNull()?.let {
            QuoteData(it.text, it.source)
        } ?: QuoteData(
            context.getString(R.string.module_custom_setup_line1),
            context.getString(R.string.module_custom_setup_line2)
        )
    }

    override val characterType: Int
        get() = CHARACTER_TYPE_DEFAULT
}