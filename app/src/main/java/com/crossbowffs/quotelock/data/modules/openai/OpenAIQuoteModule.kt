package com.crossbowffs.quotelock.data.modules.openai

import android.content.Context
import com.crossbowffs.quotelock.app.configs.openai.OpenAINavigation
import com.crossbowffs.quotelock.app.configs.openai.OpenAIPrefKeys.PREF_OPENAI
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.data.api.QuoteModule
import com.crossbowffs.quotelock.data.api.QuoteModule.Companion.CHARACTER_TYPE_DEFAULT
import com.crossbowffs.quotelock.di.OpenAIEntryPoint
import com.yubyf.quotelockx.R
import dagger.hilt.android.EntryPointAccessors
import org.json.JSONException
import java.io.IOException

class OpenAIQuoteModule : QuoteModule {
    override fun getDisplayName(context: Context): String {
        return context.getString(R.string.module_openai_name)
    }

    override fun getConfigRoute(): String = OpenAINavigation.route

    override fun getMinimumRefreshInterval(context: Context): Int {
        return 0
    }

    override fun requiresInternetConnectivity(context: Context): Boolean {
        return true
    }

    @Throws(IOException::class, JSONException::class)
    override suspend fun Context.getQuote(): QuoteData {
        val openAIRepository =
            EntryPointAccessors.fromApplication<OpenAIEntryPoint>(applicationContext)
                .openAIRepository()
        if (openAIRepository.apiKey.isNullOrBlank()) {
            return QuoteData(
                quoteText = getString(R.string.module_openai_setup_line1),
                quoteSource = getString(R.string.module_openai_setup_line2),
                provider = PREF_OPENAI,
            )
        }
        val openAIQuote = openAIRepository.requestQuote()
        return openAIQuote.let {
            QuoteData(
                quoteText = it?.quote.orEmpty(),
                quoteSource = it?.source.orEmpty(),
                provider = PREF_OPENAI,
            )
        }
    }

    override val characterType: Int
        get() = CHARACTER_TYPE_DEFAULT
}