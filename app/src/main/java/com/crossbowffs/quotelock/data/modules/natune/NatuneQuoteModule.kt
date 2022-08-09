package com.crossbowffs.quotelock.data.modules.natune

import android.content.ComponentName
import android.content.Context
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.data.api.QuoteModule
import com.crossbowffs.quotelock.data.api.QuoteModule.Companion.CHARACTER_TYPE_LATIN
import com.crossbowffs.quotelock.utils.downloadUrl
import com.yubyf.quotelockx.R
import org.jsoup.Jsoup

class NatuneQuoteModule : QuoteModule {
    override fun getDisplayName(context: Context): String {
        return context.getString(R.string.module_natune_name)
    }

    override fun getConfigActivity(context: Context): ComponentName? {
        return null
    }

    override fun getMinimumRefreshInterval(context: Context): Int {
        return 0
    }

    override fun requiresInternetConnectivity(context: Context): Boolean {
        return true
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(Exception::class)
    override suspend fun getQuote(context: Context): QuoteData {
        val html = "https://natune.net/zitate/Zufalls5".downloadUrl()
        val document = Jsoup.parse(html)
        val quoteLi = document.select(".quotes > li").first()
        val quoteText = quoteLi.getElementsByClass("quote_text").first().text()
        val quoteAuthor = quoteLi.getElementsByClass("quote_author").first().text()
        return QuoteData(quoteText = quoteText, quoteSource = "", quoteAuthor = quoteAuthor)
    }

    override val characterType: Int
        get() = CHARACTER_TYPE_LATIN
}