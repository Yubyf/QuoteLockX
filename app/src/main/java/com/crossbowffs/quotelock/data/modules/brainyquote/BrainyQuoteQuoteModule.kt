package com.crossbowffs.quotelock.data.modules.brainyquote

import android.content.Context
import com.crossbowffs.quotelock.app.configs.brainyquote.BrainyQuoteDestination
import com.crossbowffs.quotelock.app.configs.brainyquote.BrainyQuotePrefKeys.PREF_BRAINY_TYPE_STRING
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.data.api.QuoteModule
import com.crossbowffs.quotelock.data.api.QuoteModule.Companion.CHARACTER_TYPE_LATIN
import com.crossbowffs.quotelock.di.QuoteModuleEntryPoint
import com.crossbowffs.quotelock.utils.downloadUrl
import com.yubyf.quotelockx.R
import dagger.hilt.android.EntryPointAccessors
import org.jsoup.Jsoup
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

    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(IOException::class)
    override suspend fun getQuote(context: Context): QuoteData {
        val dataStore =
            EntryPointAccessors.fromApplication<QuoteModuleEntryPoint>(context.applicationContext)
                .brainyDataStore()
        val type = dataStore.getStringSuspend(PREF_BRAINY_TYPE_STRING, "BR")
        val url = "https://feeds.feedburner.com/brainyquote/QUOTE$type"
        val rssXml = url.downloadUrl()
        val document = Jsoup.parse(rssXml)
        val quoteText = document.select("item > description").first().text()
        val quoteAuthor = document.select("item > title").first().text()
        return QuoteData(quoteText = quoteText.substring(1, quoteText.length - 1), quoteSource = "",
            quoteAuthor = quoteAuthor)
    }

    override val characterType: Int
        get() = CHARACTER_TYPE_LATIN
}