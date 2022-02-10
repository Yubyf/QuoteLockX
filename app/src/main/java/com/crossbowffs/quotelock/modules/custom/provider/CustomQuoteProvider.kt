package com.crossbowffs.quotelock.modules.custom.provider

import android.content.Context
import android.database.sqlite.SQLiteOpenHelper
import com.crossbowffs.quotelock.provider.AutoContentProvider

class CustomQuoteProvider : AutoContentProvider(
    CustomQuoteContract.AUTHORITY, arrayOf(
        ProviderTable(
            CustomQuoteContract.Quotes.TABLE,
            CustomQuoteContract.Quotes.CONTENT_ITEM_TYPE,
            CustomQuoteContract.Quotes.CONTENT_TYPE
        )
    )
) {
    override fun createDatabaseHelper(context: Context): SQLiteOpenHelper {
        return CustomQuoteHelper(context)
    }
}