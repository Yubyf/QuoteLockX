package com.crossbowffs.quotelock.modules.custom.provider;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import com.crossbowffs.quotelock.provider.AutoContentProvider;

public class CustomQuoteProvider extends AutoContentProvider {
    public CustomQuoteProvider() {
        super(CustomQuoteContract.AUTHORITY, new ProviderTable[] {
            new ProviderTable(CustomQuoteContract.Quotes.TABLE, CustomQuoteContract.Quotes.CONTENT_ITEM_TYPE, CustomQuoteContract.Quotes.CONTENT_TYPE)
        });
    }

    @Override
    protected SQLiteOpenHelper createDatabaseHelper(Context context) {
        return new CustomQuoteHelper(context);
    }
}
