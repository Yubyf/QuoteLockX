package com.crossbowffs.quotelock.app;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.crossbowffs.quotelock.api.QuoteData;
import com.crossbowffs.quotelock.api.QuoteModule;
import com.crossbowffs.quotelock.collection.provider.QuoteCollectionContract;
import com.crossbowffs.quotelock.modules.ModuleManager;
import com.crossbowffs.quotelock.consts.PrefKeys;
import com.crossbowffs.quotelock.modules.ModuleNotFoundException;
import com.crossbowffs.quotelock.utils.Md5Utils;
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
        Xlog.d(TAG, "Attempting to download new quote...");
        QuoteModule module;
        try {
            module = ModuleManager.getModule(mContext, mModuleName);
        } catch (ModuleNotFoundException e) {
            Xlog.e(TAG, "Selected module not found", e);
            return null;
        }
        Xlog.d(TAG, "Provider: %s", module.getDisplayName(mContext));
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
            Xlog.d(TAG, "Text: %s", quote.getQuoteText());
            Xlog.d(TAG, "Source: %s", quote.getQuoteSource());

            boolean collectionState = queryQuote(quote.getQuoteText(),
                    TextUtils.isEmpty(quote.getQuoteSource()) ? "" : quote.getQuoteSource().replace("―", "").trim());
            mContext.getSharedPreferences(PrefKeys.PREF_QUOTES, Context.MODE_PRIVATE)
                .edit()
                .putString(PrefKeys.PREF_QUOTES_TEXT, quote.getQuoteText())
                .putString(PrefKeys.PREF_QUOTES_SOURCE, quote.getQuoteSource())
                .putBoolean(PrefKeys.PREF_QUOTES_COLLECTION_STATE, collectionState)
                .putLong(PrefKeys.PREF_QUOTES_LAST_UPDATED, System.currentTimeMillis())
                .apply();
        }
    }

    private boolean queryQuote(String text, String source) {
        Uri uri = Uri.withAppendedPath(Uri.withAppendedPath(QuoteCollectionContract.Collection.CONTENT_URI, QuoteCollectionContract.Collection.MD5), Md5Utils.md5(text + source));
        String[] columns = {QuoteCollectionContract.Collection._ID};
        try (Cursor cursor = mContext.getContentResolver().query(uri, columns, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int has = cursor.getInt(0);
                return has > 0;
            } else {
                return false;
            }
        }
    }

    private void persistQuote(long rowId, String text, String source) {
        ContentValues values = new ContentValues(3);
        values.put(QuoteCollectionContract.Collection.TEXT, text);
        values.put(QuoteCollectionContract.Collection.SOURCE, source);
        values.put(QuoteCollectionContract.Collection.MD5, Md5Utils.md5(text + source));
        ContentResolver resolver = mContext.getContentResolver();
        if (rowId >= 0) {
            Uri uri = ContentUris.withAppendedId(QuoteCollectionContract.Collection.CONTENT_URI, rowId);
            values.put(QuoteCollectionContract.Collection._ID, rowId);
            resolver.update(uri, values, null, null);
        } else {
            resolver.insert(QuoteCollectionContract.Collection.CONTENT_URI, values);
        }
    }

    @Override
    protected void onCancelled(QuoteData quoteData) {
        Xlog.d(TAG, "QuoteDownloaderTask#onCancelled called");
    }
}
