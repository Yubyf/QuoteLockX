package com.crossbowffs.quotelock.modules.wikiquote

import android.content.ComponentName
import android.content.Context
import com.crossbowffs.quotelock.R
import com.crossbowffs.quotelock.api.QuoteData
import com.crossbowffs.quotelock.api.QuoteModule
import com.crossbowffs.quotelock.api.QuoteModule.Companion.CHARACTER_TYPE_CJK
import com.crossbowffs.quotelock.modules.wikiquote.WikiquoteQuoteModule
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.downloadUrl
import org.jsoup.Jsoup
import java.io.IOException
import java.util.regex.Pattern

class WikiquoteQuoteModule : QuoteModule {

    companion object {
        private val TAG = WikiquoteQuoteModule::class.simpleName
    }

    override fun getDisplayName(context: Context): String {
        return context.getString(R.string.module_wikiquote_name)
    }

    override fun getConfigActivity(context: Context): ComponentName? {
        return null
    }

    override fun getMinimumRefreshInterval(context: Context): Int {
        return 86400
    }

    override fun requiresInternetConnectivity(context: Context): Boolean {
        return true
    }

    @Throws(IOException::class)
    override fun getQuote(context: Context): QuoteData? {
        val html = "https://zh.m.wikiquote.org/zh-cn/Wikiquote:%E9%A6%96%E9%A1%B5".downloadUrl()
        val document = Jsoup.parse(html)
        val quoteAllText = document.select("#mp-everyday-quote").text()
        Xlog.d(TAG, "Downloaded text: %s", quoteAllText)
        val quoteMatcher =
            Pattern.compile("(.*?)\\s*[\\u2500\\u2014\\u002D]{2}\\s*(.*?)").matcher(quoteAllText)
        if (!quoteMatcher.matches()) {
            Xlog.e(TAG, "Failed to parse quote")
            return null
        }
        val quoteText = quoteMatcher.group(1) ?: ""
        val quoteSource = "―${quoteMatcher.group(2) ?: ""}"
        return QuoteData(quoteText, quoteSource)
    }

    override val characterType: Int
        get() = CHARACTER_TYPE_CJK
}