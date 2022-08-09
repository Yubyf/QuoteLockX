package com.crossbowffs.quotelock.data.modules.custom

import android.content.ComponentName
import android.content.Context
import com.crossbowffs.quotelock.app.configs.custom.CustomQuoteConfigActivity
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.data.api.QuoteModule
import com.crossbowffs.quotelock.data.api.QuoteModule.Companion.CHARACTER_TYPE_DEFAULT
import com.crossbowffs.quotelock.di.QuoteModuleEntryPoint
import com.yubyf.quotelockx.R
import dagger.hilt.android.EntryPointAccessors

class CustomQuoteModule : QuoteModule {
    override fun getDisplayName(context: Context): String {
        return context.getString(R.string.module_custom_name)
    }

    override fun getConfigActivity(context: Context): ComponentName {
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
        val repository = EntryPointAccessors.fromApplication<QuoteModuleEntryPoint>(
            context.applicationContext).customQuoteRepository()
        return repository.getRandomItem()?.let {
            QuoteData(it.text, it.source)
        } ?: QuoteData(
            context.getString(R.string.module_custom_setup_line1),
            context.getString(R.string.module_custom_setup_line2)
        )
    }

    override val characterType: Int
        get() = CHARACTER_TYPE_DEFAULT
}