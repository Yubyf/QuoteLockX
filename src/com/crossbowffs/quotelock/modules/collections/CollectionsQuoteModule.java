package com.crossbowffs.quotelock.modules.collections;

import android.content.ComponentName;
import android.content.Context;
import android.database.Cursor;

import com.crossbowffs.quotelock.R;
import com.crossbowffs.quotelock.api.QuoteData;
import com.crossbowffs.quotelock.api.QuoteModule;
import com.crossbowffs.quotelock.collections.app.QuoteCollectionActivity;
import com.crossbowffs.quotelock.collections.provider.QuoteCollectionContract;

/**
 * @author Yubyf
 */
public class CollectionsQuoteModule implements QuoteModule {
    @Override
    public String getDisplayName(Context context) {
        return context.getString(R.string.module_collections_name);
    }

    @Override
    public ComponentName getConfigActivity(Context context) {
        return new ComponentName(context, QuoteCollectionActivity.class);
    }

    @Override
    public int getMinimumRefreshInterval(Context context) {
        return 0;
    }

    @Override
    public boolean requiresInternetConnectivity(Context context) {
        return false;
    }

    @Override
    public QuoteData getQuote(Context context) throws Exception {
        Cursor cursor = context.getContentResolver().query(
                QuoteCollectionContract.Collections.CONTENT_URI,
                new String[]{QuoteCollectionContract.Collections.TEXT,
                        QuoteCollectionContract.Collections.SOURCE}, null, null,
                "RANDOM() LIMIT 1"); // Hack to pass row limit through ContentProvider
        try {
            if (cursor != null && cursor.moveToFirst()) {
                return new QuoteData(cursor.getString(0), cursor.getString(1));
            } else {
                return new QuoteData(
                        context.getString(R.string.module_custom_setup_line1),
                        context.getString(R.string.module_custom_setup_line2));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public int getCharacterType() {
        return CHARACTER_TYPE_DEFAULT;
    }
}
