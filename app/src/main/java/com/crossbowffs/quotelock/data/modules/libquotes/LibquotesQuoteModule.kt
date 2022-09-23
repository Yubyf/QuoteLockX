package com.crossbowffs.quotelock.data.modules.libquotes

import android.content.Context
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.data.api.QuoteModule
import com.crossbowffs.quotelock.data.api.QuoteModule.Companion.CHARACTER_TYPE_CJK
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.className
import com.crossbowffs.quotelock.utils.downloadUrl
import com.yubyf.quotelockx.R
import org.jsoup.Jsoup
import java.io.IOException

class LibquotesQuoteModule : QuoteModule {

    companion object {
        private val TAG = className<LibquotesQuoteModule>()
    }

    override fun getDisplayName(context: Context): String {
        return context.getString(R.string.module_libquotes_name)
    }

    override fun getConfigRoute(): String? = null

    override fun getMinimumRefreshInterval(context: Context): Int {
        return 86400
    }

    override fun requiresInternetConnectivity(context: Context): Boolean {
        return true
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(IOException::class)
    override suspend fun getQuote(context: Context): QuoteData? {
        val html = "https://feeds.feedburner.com/libquotes/QuoteOfTheDay".downloadUrl()
        val document = Jsoup.parse(html)
        val qotdItem = document.select("item").first()
        return qotdItem?.let {
            Xlog.d(TAG, "Downloaded qotd: ${it.text()}")
            val quoteText = it.select("description").text()
            val quoteAuthor = it.select("title").text()
            QuoteData(
                quoteText = quoteText, quoteSource = "", quoteAuthor = quoteAuthor
            )
        }
    }

    override val characterType: Int
        get() = CHARACTER_TYPE_CJK
}