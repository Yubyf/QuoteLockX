package com.crossbowffs.quotelock.data.modules.brainyquote

import android.content.Context
import com.crossbowffs.quotelock.app.configs.brainyquote.BrainyQuoteDestination
import com.crossbowffs.quotelock.app.configs.brainyquote.BrainyQuotePrefKeys
import com.crossbowffs.quotelock.app.configs.brainyquote.BrainyQuotePrefKeys.PREF_BRAINY_TYPE_STRING
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.data.api.QuoteModule
import com.crossbowffs.quotelock.data.api.QuoteModule.Companion.CHARACTER_TYPE_LATIN
import com.crossbowffs.quotelock.data.api.QuoteModule.Companion.httpClient
import com.crossbowffs.quotelock.di.QuoteModuleEntryPoint
import com.crossbowffs.quotelock.utils.fetchXml
import com.yubyf.quotelockx.R
import dagger.hilt.android.EntryPointAccessors
import java.io.IOException

class BrainyQuoteQuoteModule : QuoteModule {
    override fun getDisplayName(context: Context): String {
        return context.getString(R.string.module_brainy_name)
    }

    override fun getConfigRoute(): String = BrainyQuoteDestination.route

    override fun getMinimumRefreshInterval(context: Context): Int {
        return 86400
    }

    override fun requiresInternetConnectivity(context: Context): Boolean {
        return true
    }

    @Throws(IOException::class)
    override suspend fun Context.getQuote(): QuoteData {
        val dataStore =
            EntryPointAccessors.fromApplication<QuoteModuleEntryPoint>(applicationContext)
                .brainyDataStore()
        val type = dataStore.getStringSuspend(PREF_BRAINY_TYPE_STRING, "BR")
        val url = "https://feeds.feedburner.com/brainyquote/QUOTE$type"
        val document = httpClient.fetchXml(url)
        val quoteText = document.select("item > description").first()?.text().orEmpty().let {
            it.substring(1, it.length - 1)
        }
        val quoteAuthor = document.select("item > title").first()?.text().orEmpty()
        return QuoteData(
            quoteText = quoteText,
            quoteSource = "",
            quoteAuthor = quoteAuthor,
            provider = BrainyQuotePrefKeys.PREF_BRAINY
        )
    }

    override val characterType: Int
        get() = CHARACTER_TYPE_LATIN
}