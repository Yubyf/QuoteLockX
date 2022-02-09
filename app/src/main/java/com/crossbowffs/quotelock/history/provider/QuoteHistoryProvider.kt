package com.crossbowffs.quotelock.history.provider

import android.content.Context
import android.database.sqlite.SQLiteOpenHelper
import com.crossbowffs.quotelock.provider.AutoContentProvider

/**
 * @author Yubyf
 */
class QuoteHistoryProvider : AutoContentProvider(
    QuoteHistoryContract.AUTHORITY, arrayOf(
        ProviderTable(
            QuoteHistoryContract.Histories.TABLE,
            QuoteHistoryContract.Histories.CONTENT_ITEM_TYPE,
            QuoteHistoryContract.Histories.CONTENT_TYPE
        )
    )
) {
    override fun createDatabaseHelper(context: Context): SQLiteOpenHelper {
        return QuoteHistoryHelper(context)
    }
}