package com.crossbowffs.quotelock.data.modules.collections

import android.content.Context
import com.crossbowffs.quotelock.app.collections.CollectionDestination
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.data.api.QuoteModule
import com.crossbowffs.quotelock.data.api.QuoteModule.Companion.CHARACTER_TYPE_DEFAULT
import com.crossbowffs.quotelock.di.QuoteModuleEntryPoint
import com.yubyf.quotelockx.R
import dagger.hilt.android.EntryPointAccessors

/**
 * @author Yubyf
 */
class CollectionsQuoteModule : QuoteModule {
    override fun getDisplayName(context: Context): String {
        return context.getString(R.string.module_collections_name)
    }

    override fun getConfigRoute(): String = CollectionDestination.route

    override fun getMinimumRefreshInterval(context: Context): Int {
        return 0
    }

    override fun requiresInternetConnectivity(context: Context): Boolean {
        return false
    }

    @Throws(Exception::class)
    override suspend fun getQuote(context: Context): QuoteData {
        val repository = EntryPointAccessors.fromApplication<QuoteModuleEntryPoint>(
            context.applicationContext).collectionRepository()
        return repository.getRandomItem()?.let {
            QuoteData(it.text, it.source, it.author)
        } ?: QuoteData(
            context.getString(R.string.module_collections_setup_line1),
            context.getString(R.string.module_collections_setup_line2)
        )
    }

    override val characterType: Int
        get() = CHARACTER_TYPE_DEFAULT
}