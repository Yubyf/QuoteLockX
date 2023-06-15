package com.crossbowffs.quotelock.data.modules.freakuotes

import android.content.Context
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.data.api.QuoteModule
import com.crossbowffs.quotelock.data.api.QuoteModule.Companion.httpClient
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.className
import com.crossbowffs.quotelock.utils.fetchXml
import com.yubyf.quotelockx.R

class FreakuotesQuoteModule : QuoteModule {

    companion object {
        private val TAG = className<FreakuotesQuoteModule>()
    }

    override fun getDisplayName(context: Context): String {
        return context.getString(R.string.module_freakuotes_name)
    }

    override fun getConfigRoute(): String? = null

    override fun getMinimumRefreshInterval(context: Context): Int {
        return 0
    }

    override fun requiresInternetConnectivity(context: Context): Boolean {
        return true
    }

    @Throws(Exception::class)
    override suspend fun Context.getQuote(): QuoteData? {
        val document = httpClient.fetchXml("https://freakuotes.com/frase/aleatoria")
        val quoteContainer = document.select(".quote-container > blockquote").first()
        val quoteText = quoteContainer?.getElementsByTag("p")?.text().orEmpty()
        if (quoteText.isEmpty()) {
            Xlog.e(TAG, "Failed to find quote text")
            return null
        }
        val sourceLeft = quoteContainer?.select("footer > span")?.text().orEmpty()
        val sourceRight = quoteContainer?.select("footer > cite")?.attr("title").orEmpty()
        val quoteSource: String = when {
            sourceLeft.isEmpty() && sourceRight.isEmpty() -> {
                Xlog.w(TAG, "Quote source not found")
                ""
            }
            sourceLeft.isEmpty() -> sourceRight
            sourceRight.isEmpty() -> sourceLeft
            else -> "$sourceLeft, $sourceRight"
        }
        return QuoteData(quoteText, quoteSource, "freakuotes")
    }

    override val characterType: Int
        get() = QuoteModule.CHARACTER_TYPE_LATIN
}