package com.crossbowffs.quotelock.modules.collections

import android.content.ComponentName
import android.content.Context
import com.crossbowffs.quotelock.R
import com.crossbowffs.quotelock.api.QuoteData
import com.crossbowffs.quotelock.api.QuoteModule
import com.crossbowffs.quotelock.api.QuoteModule.Companion.CHARACTER_TYPE_DEFAULT
import com.crossbowffs.quotelock.collections.app.QuoteCollectionActivity
import com.crossbowffs.quotelock.collections.provider.QuoteCollectionContract

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
    override fun getQuote(context: Context): QuoteData {
        val cursor = context.contentResolver.query(
            QuoteCollectionContract.Collections.CONTENT_URI, arrayOf(
                QuoteCollectionContract.Collections.TEXT,
                QuoteCollectionContract.Collections.SOURCE
            ), null, null,
            "RANDOM() LIMIT 1"
        ) // Hack to pass row limit through ContentProvider
        return cursor.use {
            if (it?.moveToFirst() == true) {
                QuoteData(it.getString(0), it.getString(1))
            } else {
                QuoteData(
                    context.getString(R.string.module_custom_setup_line1),
                    context.getString(R.string.module_custom_setup_line2)
                )
            }
        }
    }

    override val characterType: Int
        get() = CHARACTER_TYPE_DEFAULT
}