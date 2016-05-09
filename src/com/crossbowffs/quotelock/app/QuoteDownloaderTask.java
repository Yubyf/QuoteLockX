package com.crossbowffs.quotelock.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import com.crossbowffs.quotelock.api.VnaasApiManager;
import com.crossbowffs.quotelock.api.VnaasQuoteQueryParams;
import com.crossbowffs.quotelock.model.VnaasQuote;
import com.crossbowffs.quotelock.preferences.PrefKeys;
import com.crossbowffs.quotelock.utils.Xlog;

import java.io.IOException;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;

public class QuoteDownloaderTask extends AsyncTask<Void, Void, VnaasQuote> {
    private final Context mContext;
    private final VnaasApiManager mApiManager;

    public QuoteDownloaderTask(Context context) {
        mContext = context;
        SharedPreferences preferences = context.getSharedPreferences(PrefKeys.PREF_COMMON, MODE_PRIVATE);
        String apiUrl = preferences.getString(PrefKeys.PREF_COMMON_API_URL, PrefKeys.PREF_COMMON_API_URL_DEFAULT);
        mApiManager = new VnaasApiManager(apiUrl);
    }

    @Override
    protected VnaasQuote doInBackground(Void... params) {
        Xlog.i(TAG, "Attempting to download new VNaaS quote...");

        VnaasQuoteQueryParams query = new VnaasQuoteQueryParams();
        SharedPreferences preferences =  mContext.getSharedPreferences(PrefKeys.PREF_COMMON, MODE_PRIVATE);

        String novelIdsStr = preferences.getString(PrefKeys.PREF_COMMON_ENABLED_NOVELS, null);
        if (novelIdsStr != null) {
            String[] novelIdsSplit = novelIdsStr.split(",");
            long[] novelIds = new long[novelIdsSplit.length];
            for (int i = 0; i < novelIds.length; ++i) {
                novelIds[i] = Long.parseLong(novelIdsSplit[i]);
            }
            query.setNovels(novelIds);
        }

        String characterIdsStr = preferences.getString(PrefKeys.PREF_COMMON_ENABLED_CHARACTERS, null);
        if (characterIdsStr != null) {
            String[] characterIdsSplit = characterIdsStr.split(",");
            long[] characterIds = new long[characterIdsSplit.length];
            for (int i = 0; i < characterIds.length; ++i) {
                characterIds[i] = Long.parseLong(characterIdsSplit[i]);
            }
            query.setCharacters(characterIds);
        }

        String contains = preferences.getString(PrefKeys.PREF_COMMON_QUOTE_CONTAINS, null);
        if (contains != null) {
            query.setContains(contains);
        }

        try {
            return mApiManager.getRandomQuote(query);
        } catch (IOException e) {
            Xlog.e(TAG, "Quote download failed", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(VnaasQuote vnaasQuote) {
        if (vnaasQuote != null) {
            Xlog.i(TAG, "Downloaded new VNaaS quote");
            mContext.getSharedPreferences(PrefKeys.PREF_QUOTES, MODE_PRIVATE)
                .edit()
                .putString(PrefKeys.PREF_QUOTES_TEXT, vnaasQuote.getText())
                .putString(PrefKeys.PREF_QUOTES_CHARACTER, vnaasQuote.getCharacter().getName())
                .putString(PrefKeys.PREF_QUOTES_NOVEL, vnaasQuote.getNovel().getName())
                .apply();
        }
    }
}
