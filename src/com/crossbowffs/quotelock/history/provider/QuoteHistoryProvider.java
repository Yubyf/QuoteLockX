package com.crossbowffs.quotelock.history.provider;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

import com.crossbowffs.quotelock.provider.AutoContentProvider;

/**
 * @author Yubyf
 */
public class QuoteHistoryProvider extends AutoContentProvider {
    public QuoteHistoryProvider() {
        super(QuoteHistoryContract.AUTHORITY, new ProviderTable[]{
                new ProviderTable(
                        QuoteHistoryContract.Histories.TABLE,
                        QuoteHistoryContract.Histories.CONTENT_ITEM_TYPE,
                        QuoteHistoryContract.Histories.CONTENT_TYPE)
        });
    }

    @Override
    protected SQLiteOpenHelper createDatabaseHelper(Context context) {
        return new QuoteHistoryHelper(context);
    }
}
