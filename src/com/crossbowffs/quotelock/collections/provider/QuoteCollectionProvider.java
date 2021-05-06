package com.crossbowffs.quotelock.collections.provider;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

import com.crossbowffs.quotelock.provider.AutoContentProvider;

/**
 * @author Yubyf
 */
public class QuoteCollectionProvider extends AutoContentProvider {
    public QuoteCollectionProvider() {
        super(QuoteCollectionContract.AUTHORITY, new ProviderTable[] {
                new ProviderTable(
                        QuoteCollectionContract.Collections.TABLE,
                        QuoteCollectionContract.Collections.CONTENT_ITEM_TYPE,
                        QuoteCollectionContract.Collections.CONTENT_TYPE)
        });
    }

    @Override
    protected SQLiteOpenHelper createDatabaseHelper(Context context) {
        return new QuoteCollectionHelper(context);
    }
}
