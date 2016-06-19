package com.crossbowffs.quotelock.modules.custom;

import android.content.ComponentName;
import android.content.Context;
import android.database.Cursor;
import com.crossbowffs.quotelock.R;
import com.crossbowffs.quotelock.api.QuoteData;
import com.crossbowffs.quotelock.api.QuoteModule;
import com.crossbowffs.quotelock.modules.custom.app.CustomQuoteConfigActivity;
import com.crossbowffs.quotelock.modules.custom.provider.CustomQuoteContract;

public class CustomQuoteModule implements QuoteModule {
    @Override
    public String getDisplayName(Context context) {
        return context.getString(R.string.module_custom_name);
    }

    @Override
    public ComponentName getConfigActivity(Context context) {
        return new ComponentName(context, CustomQuoteConfigActivity.class);
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
        Cursor cursor = context.getContentResolver().query(CustomQuoteContract.Quotes.CONTENT_URI,
            new String[] {CustomQuoteContract.Quotes.TEXT, CustomQuoteContract.Quotes.SOURCE}, null, null,
            "RANDOM() LIMIT 1"); // Hack to pass row limit through ContentProvider
        try {
            if (cursor != null && cursor.moveToFirst()) {
                return new QuoteData(cursor.getString(0), cursor.getString(1));
            } else {
                return null;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
