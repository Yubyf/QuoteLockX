package com.crossbowffs.quotelock.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import com.crossbowffs.quotelock.api.QuoteData;
import com.crossbowffs.quotelock.api.QuoteModule;
import com.crossbowffs.quotelock.modules.ModuleManager;
import com.crossbowffs.quotelock.preferences.PrefKeys;
import com.crossbowffs.quotelock.utils.Xlog;

public class QuoteDownloaderTask extends AsyncTask<Void, Void, QuoteData> {
    private static final String TAG = QuoteDownloaderTask.class.getSimpleName();

    protected final Context mContext;
    private final QuoteModule mModule;

    public QuoteDownloaderTask(Context context) {
        mContext = context;
        SharedPreferences preferences = context.getSharedPreferences(PrefKeys.PREF_COMMON, Context.MODE_PRIVATE);
        String moduleClsName = preferences.getString(PrefKeys.PREF_COMMON_QUOTE_MODULE, PrefKeys.PREF_COMMON_QUOTE_MODULE_DEFAULT);
        mModule = ModuleManager.getModule(moduleClsName);
    }

    @Override
    protected QuoteData doInBackground(Void... params) {
        Xlog.i(TAG, "Attempting to download new quote...");
        Xlog.i(TAG, "Source: %s", mModule.getDisplayName(mContext));
        try {
            return mModule.getQuote(mContext);
        } catch (Exception e) {
            Xlog.e(TAG, "Quote download failed", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(QuoteData quote) {
        if (quote != null) {
            Xlog.i(TAG, "Downloaded new quote");
            Xlog.i(TAG, "Text: %s", quote.getQuoteText());
            Xlog.i(TAG, "Source: %s", quote.getQuoteSource());
            mContext.getSharedPreferences(PrefKeys.PREF_QUOTES, Context.MODE_PRIVATE)
                .edit()
                .putString(PrefKeys.PREF_QUOTES_TEXT, quote.getQuoteText())
                .putString(PrefKeys.PREF_QUOTES_SOURCE, quote.getQuoteSource())
                .apply();
        }
    }
}
