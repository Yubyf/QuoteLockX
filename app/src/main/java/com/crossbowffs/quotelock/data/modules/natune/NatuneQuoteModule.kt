package com.crossbowffs.quotelock.data.modules.natune

import android.content.Context
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.data.api.QuoteModule
import com.crossbowffs.quotelock.data.api.QuoteModule.Companion.CHARACTER_TYPE_LATIN
import com.crossbowffs.quotelock.utils.fetchXml
import com.yubyf.quotelockx.R

class NatuneQuoteModule : QuoteModule {
    override fun getDisplayName(context: Context): String {
        return context.getString(R.string.module_natune_name)
    }

    override fun getConfigRoute(): String? = null

    override fun getMinimumRefreshInterval(context: Context): Int {
        return 0
    }

    override fun requiresInternetConnectivity(context: Context): Boolean {
        return true
    }

    @Throws(Exception::class)
    override suspend fun Context.getQuote(): QuoteData {
        val document = httpClient().fetchXml("https://natune.net/zitate/Zufalls5")
        val quoteLi = document.select(".quotes > li").first()
        val quoteText = quoteLi?.getElementsByClass("quote_text")?.first()?.text().orEmpty()
        val quoteAuthor = quoteLi?.getElementsByClass("quote_author")?.first()?.text().orEmpty()
        return QuoteData(
            quoteText = quoteText,
            quoteSource = "",
            quoteAuthor = quoteAuthor,
            provider = "natune"
        )
    }

    override val characterType: Int
        get() = CHARACTER_TYPE_LATIN
}