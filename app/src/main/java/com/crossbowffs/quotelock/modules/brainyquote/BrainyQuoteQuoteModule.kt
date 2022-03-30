package com.crossbowffs.quotelock.modules.brainyquote

import android.content.ComponentName
import android.content.Context
import com.crossbowffs.quotelock.R
import com.crossbowffs.quotelock.api.QuoteData
import com.crossbowffs.quotelock.api.QuoteModule
import com.crossbowffs.quotelock.api.QuoteModule.Companion.CHARACTER_TYPE_LATIN
import com.crossbowffs.quotelock.app.App
import com.crossbowffs.quotelock.consts.PREF_QUOTE_SOURCE_PREFIX
import com.crossbowffs.quotelock.modules.brainyquote.app.BrainyQuoteConfigActivity
import com.crossbowffs.quotelock.modules.brainyquote.consts.BrainyQuotePrefKeys.PREF_BRAINY
import com.crossbowffs.quotelock.modules.brainyquote.consts.BrainyQuotePrefKeys.PREF_BRAINY_TYPE_STRING
import com.crossbowffs.quotelock.utils.downloadUrl
import com.yubyf.datastore.DataStoreDelegate.Companion.getDataStoreDelegate
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import java.io.IOException

internal val brainyDataStore = App.INSTANCE.getDataStoreDelegate(PREF_BRAINY, migrate = true)

class BrainyQuoteQuoteModule : QuoteModule {
    override fun getDisplayName(context: Context): String {
        return context.getString(R.string.module_brainy_name)
    }

    override fun getConfigActivity(context: Context): ComponentName {
        return ComponentName(context, BrainyQuoteConfigActivity::class.java)
    }

    override fun getMinimumRefreshInterval(context: Context): Int {
        return 86400
    }

    override fun requiresInternetConnectivity(context: Context): Boolean {
        return true
    }

    @Throws(IOException::class)
    override fun getQuote(context: Context): QuoteData {
        val type = runBlocking { brainyDataStore.getStringSuspend(PREF_BRAINY_TYPE_STRING, "BR") }
        val url = "https://feeds.feedburner.com/brainyquote/QUOTE$type"
        val rssXml = url.downloadUrl()
        val document = Jsoup.parse(rssXml)
        val quoteText = document.select("item > description").first().text()
        val quoteSource =
            "$PREF_QUOTE_SOURCE_PREFIX ${document.select("item > title").first().text()}"
        return QuoteData(quoteText.substring(1, quoteText.length - 1), quoteSource)
    }

    override val characterType: Int
        get() = CHARACTER_TYPE_LATIN
}