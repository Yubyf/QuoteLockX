package com.crossbowffs.quotelock.data.modules.wikiquote

import android.content.Context
import com.crossbowffs.quotelock.app.configs.wikiquote.WikiquoteDestination
import com.crossbowffs.quotelock.app.configs.wikiquote.WikiquotePrefKeys.PREF_WIKIQUOTE
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.data.api.QuoteModule
import com.crossbowffs.quotelock.data.api.QuoteModule.Companion.CHARACTER_TYPE_CJK
import com.crossbowffs.quotelock.utils.className
import com.yubyf.quotelockx.R
import org.koin.core.component.get
import java.io.IOException

class WikiquoteQuoteModule : QuoteModule {

    companion object {
        private val TAG = className<WikiquoteQuoteModule>()
    }

    override fun getDisplayName(context: Context): String {
        return context.getString(R.string.module_wikiquote_name)
    }

    override fun getConfigRoute(): String = WikiquoteDestination.route

    override fun getMinimumRefreshInterval(context: Context): Int {
        return 86400
    }

    override fun requiresInternetConnectivity(context: Context): Boolean {
        return true
    }

    @Throws(IOException::class)
    override suspend fun Context.getQuote(): QuoteData? {
        val wikiquoteRepository: WikiquoteRepository = get()
        return wikiquoteRepository.fetchWikiquote()?.let {
            QuoteData(
                quoteText = it.first,
                quoteSource = it.third,
                quoteAuthor = it.second,
                provider = PREF_WIKIQUOTE
            )
        }
    }

    override val characterType: Int
        get() = CHARACTER_TYPE_CJK
}