package com.crossbowffs.quotelock.data.modules.libquotes

import android.content.Context
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.data.api.QuoteModule
import com.crossbowffs.quotelock.data.api.QuoteModule.Companion.CHARACTER_TYPE_CJK
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.className
import com.crossbowffs.quotelock.utils.fetchXml
import com.yubyf.quotelockx.R
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

    @Throws(IOException::class)
    override suspend fun Context.getQuote(): QuoteData? {
        val document = httpClient().fetchXml("https://feeds.feedburner.com/libquotes/QuoteOfTheDay")
        val qotdItem = document.select("item").first()
        return qotdItem?.let {
            Xlog.d(TAG, "Downloaded qotd: ${it.text()}")
            val quoteText = it.select("description").text()
            val quoteAuthor = it.select("title").text()
            QuoteData(
                quoteText = quoteText,
                quoteSource = "",
                quoteAuthor = quoteAuthor,
                provider = "libquotes"
            )
        }
    }

    override val characterType: Int
        get() = CHARACTER_TYPE_CJK
}