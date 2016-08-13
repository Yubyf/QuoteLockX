package com.crossbowffs.quotelock.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import com.crossbowffs.quotelock.api.QuoteData;
import com.crossbowffs.quotelock.api.QuoteModule;
import com.crossbowffs.quotelock.modules.ModuleManager;
import com.crossbowffs.quotelock.consts.PrefKeys;
import com.crossbowffs.quotelock.modules.ModuleNotFoundException;
import com.crossbowffs.quotelock.utils.Xlog;

public class QuoteDownloaderTask extends AsyncTask<Void, Void, QuoteData> {
    private static final String TAG = QuoteDownloaderTask.class.getSimpleName();

    protected final Context mContext;
    private final String mModuleName;

    public QuoteDownloaderTask(Context context) {
        mContext = context;
        SharedPreferences preferences = context.getSharedPreferences(PrefKeys.PREF_COMMON, Context.MODE_PRIVATE);
        mModuleName = preferences.getString(PrefKeys.PREF_COMMON_QUOTE_MODULE, PrefKeys.PREF_COMMON_QUOTE_MODULE_DEFAULT);
    }

    @Override
    protected QuoteData doInBackground(Void... params) {
        Xlog.i(TAG, "Attempting to download new quote...");
        QuoteModule module;
        try {
            module = ModuleManager.getModule(mContext, mModuleName);
        } catch (ModuleNotFoundException e) {
            Xlog.e(TAG, "Selected module not found", e);
            return null;
        }
        Xlog.i(TAG, "Provider: %s", module.getDisplayName(mContext));
        try {
            return module.getQuote(mContext);
        } catch (Exception e) {
            Xlog.e(TAG, "Quote download failed", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(QuoteData quote) {
        Xlog.d(TAG, "QuoteDownloaderTask#onPostExecute called, success: %s", (quote != null));
        if (quote != null) {
            Xlog.i(TAG, "Text: %s", quote.getQuoteText());
            Xlog.i(TAG, "Source: %s", quote.getQuoteSource());
            mContext.getSharedPreferences(PrefKeys.PREF_QUOTES, Context.MODE_PRIVATE)
                .edit()
                .putString(PrefKeys.PREF_QUOTES_TEXT, quote.getQuoteText())
                .putString(PrefKeys.PREF_QUOTES_SOURCE, quote.getQuoteSource())
                .putLong(PrefKeys.PREF_QUOTES_LAST_UPDATED, System.currentTimeMillis())
                .apply();
        }
    }

    @Override
    protected void onCancelled(QuoteData quoteData) {
        Xlog.d(TAG, "QuoteDownloaderTask#onCancelled called");
    }
}
