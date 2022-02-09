package com.crossbowffs.quotelock.collections.provider

import android.content.Context
import android.database.sqlite.SQLiteOpenHelper
import com.crossbowffs.quotelock.provider.AutoContentProvider

/**
 * @author Yubyf
 */
class QuoteCollectionProvider : AutoContentProvider(
    QuoteCollectionContract.AUTHORITY, arrayOf(
        ProviderTable(
            QuoteCollectionContract.Collections.TABLE,
            QuoteCollectionContract.Collections.CONTENT_ITEM_TYPE,
            QuoteCollectionContract.Collections.CONTENT_TYPE
        )
    )
) {
    override fun createDatabaseHelper(context: Context): SQLiteOpenHelper {
        return QuoteCollectionHelper(context)
    }
}