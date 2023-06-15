package com.crossbowffs.quotelock.data.modules.wikiquote

import android.content.Context
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.data.api.QuoteModule
import com.crossbowffs.quotelock.data.api.QuoteModule.Companion.CHARACTER_TYPE_CJK
import com.crossbowffs.quotelock.data.api.QuoteModule.Companion.httpClient
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.className
import com.crossbowffs.quotelock.utils.fetchXml
import com.yubyf.quotelockx.R
import java.io.IOException
import java.util.regex.Pattern

class WikiquoteQuoteModule : QuoteModule {

    companion object {
        private val TAG = className<WikiquoteQuoteModule>()
    }

    override fun getDisplayName(context: Context): String {
        return context.getString(R.string.module_wikiquote_name)
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
        val document =
            httpClient.fetchXml("https://zh.m.wikiquote.org/zh-cn/Wikiquote:%E9%A6%96%E9%A1%B5")
        val quoteAllText = document.select("#mp-everyday-quote").text()
        Xlog.d(TAG, "Downloaded text: %s", quoteAllText)
        val quoteMatcher =
            Pattern.compile("(.*?)\\s*[\\u2500\\u2014\\u002D]{2}\\s*(.*?)").matcher(quoteAllText)
        if (!quoteMatcher.matches()) {
            Xlog.e(TAG, "Failed to parse quote")
            return null
        }
        val quoteText = quoteMatcher.group(1).orEmpty()
        val quoteAuthor = quoteMatcher.group(2).orEmpty()
        return QuoteData(
            quoteText = quoteText,
            quoteSource = "",
            quoteAuthor = quoteAuthor,
            provider = "wikiquote"
        )
    }

    override val characterType: Int
        get() = CHARACTER_TYPE_CJK
}