package com.crossbowffs.quotelock.data.modules.freakuotes

import android.content.Context
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.data.api.QuoteModule
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.className
import com.crossbowffs.quotelock.utils.downloadUrl
import com.yubyf.quotelockx.R
import org.jsoup.Jsoup

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

    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(Exception::class)
    override suspend fun getQuote(context: Context): QuoteData? {
        val html = "https://freakuotes.com/frase/aleatoria".downloadUrl()
        val document = Jsoup.parse(html)
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
        return QuoteData(quoteText, quoteSource)
    }

    override val characterType: Int
        get() = QuoteModule.CHARACTER_TYPE_LATIN
}