package com.crossbowffs.quotelock.modules.collections

import android.content.ComponentName
import android.content.Context
import com.crossbowffs.quotelock.R
import com.crossbowffs.quotelock.api.QuoteData
import com.crossbowffs.quotelock.api.QuoteModule
import com.crossbowffs.quotelock.api.QuoteModule.Companion.CHARACTER_TYPE_DEFAULT
import com.crossbowffs.quotelock.collections.app.QuoteCollectionActivity
import com.crossbowffs.quotelock.collections.database.quoteCollectionDatabase
import kotlinx.coroutines.flow.firstOrNull

/**
 * @author Yubyf
 */
class CollectionsQuoteModule : QuoteModule {
    override fun getDisplayName(context: Context): String {
        return context.getString(R.string.module_collections_name)
    }

    override fun getConfigActivity(context: Context): ComponentName {
        return ComponentName(context, QuoteCollectionActivity::class.java)
    }

    override fun getMinimumRefreshInterval(context: Context): Int {
        return 0
    }

    override fun requiresInternetConnectivity(context: Context): Boolean {
        return false
    }

    @Throws(Exception::class)
    override suspend fun getQuote(context: Context): QuoteData {
        return quoteCollectionDatabase.dao().getRandomItem().firstOrNull()?.let {
            QuoteData(it.text, it.source)
        } ?: QuoteData(
            context.getString(R.string.module_collections_setup_line1),
            context.getString(R.string.module_collections_setup_line2)
        )
    }

    override val characterType: Int
        get() = CHARACTER_TYPE_DEFAULT
}