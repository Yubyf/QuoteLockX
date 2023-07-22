package com.crossbowffs.quotelock.data.modules.custom

import android.content.Context
import com.crossbowffs.quotelock.app.configs.custom.CustomQuoteDestination
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.data.api.QuoteModule
import com.crossbowffs.quotelock.data.api.QuoteModule.Companion.CHARACTER_TYPE_DEFAULT
import com.crossbowffs.quotelock.data.modules.custom.database.CustomQuoteContract
import com.yubyf.quotelockx.R
import org.koin.core.component.get

class CustomQuoteModule : QuoteModule {
    override fun getDisplayName(context: Context): String {
        return context.getString(R.string.module_custom_name)
    }

    override fun getConfigRoute(): String = CustomQuoteDestination.route

    override fun getMinimumRefreshInterval(context: Context): Int {
        return 0
    }

    override fun requiresInternetConnectivity(context: Context): Boolean {
        return false
    }

    @Throws(Exception::class)
    override suspend fun Context.getQuote(): QuoteData {
        val repository: CustomQuoteRepository = get()
        return repository.getRandomItem()?.let {
            QuoteData(quoteText = it.text, quoteSource = it.source, provider = it.provider)
        } ?: QuoteData(
            quoteText = getString(R.string.module_custom_setup_line1),
            quoteSource = getString(R.string.module_custom_setup_line2),
            provider = CustomQuoteContract.PROVIDER_VALUE,
        )
    }

    override val characterType: Int
        get() = CHARACTER_TYPE_DEFAULT
}