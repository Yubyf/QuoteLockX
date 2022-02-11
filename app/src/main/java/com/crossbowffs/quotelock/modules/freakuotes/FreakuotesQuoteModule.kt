package com.crossbowffs.quotelock.modules.freakuotes

import android.content.ComponentName
import android.content.Context
import com.crossbowffs.quotelock.R
import com.crossbowffs.quotelock.api.QuoteData
import com.crossbowffs.quotelock.api.QuoteModule
import com.crossbowffs.quotelock.modules.freakuotes.FreakuotesQuoteModule
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.downloadUrl
import org.jsoup.Jsoup

class FreakuotesQuoteModule : QuoteModule {

    companion object {
        private val TAG = FreakuotesQuoteModule::class.simpleName
    }

    override fun getDisplayName(context: Context): String? {
        return context.getString(R.string.module_freakuotes_name)
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

    @Throws(Exception::class)
    override fun getQuote(context: Context): QuoteData? {
        val html = "https://freakuotes.com/frase/aleatoria".downloadUrl()
        val document = Jsoup.parse(html)
        val quoteContainer = document.select(".quote-container > blockquote").first()
        val quoteText = quoteContainer.getElementsByTag("p").text()
        if (quoteText.isNullOrEmpty()) {
            Xlog.e(TAG, "Failed to find quote text")
            return null
        }
        val sourceLeft = quoteContainer.select("footer > span").text()
        val sourceRight = quoteContainer.select("footer > cite").attr("title")
        val quoteSource: String = when {
            sourceLeft.isNullOrEmpty() && sourceRight.isNullOrEmpty() -> {
                Xlog.w(TAG, "Quote source not found")
                ""
            }
            sourceLeft.isNullOrEmpty() -> "― $sourceRight"
            sourceRight.isNullOrEmpty() -> "― $sourceLeft"
            else -> "― $sourceLeft, $sourceRight"
        }
        return QuoteData(quoteText, quoteSource)
    }

    override val characterType: Int
        get() = QuoteModule.CHARACTER_TYPE_LATIN
}