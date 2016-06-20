package com.crossbowffs.quotelock.modules.vnaas;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import com.crossbowffs.quotelock.R;
import com.crossbowffs.quotelock.api.QuoteData;
import com.crossbowffs.quotelock.api.QuoteModule;
import com.crossbowffs.quotelock.modules.vnaas.api.VnaasApiManager;
import com.crossbowffs.quotelock.modules.vnaas.api.VnaasQuoteQueryParams;
import com.crossbowffs.quotelock.modules.vnaas.model.VnaasQuote;
import com.crossbowffs.quotelock.modules.vnaas.preferences.VnaasPrefKeys;

import java.io.IOException;

public class VnaasQuoteModule implements QuoteModule {
    @Override
    public String getDisplayName(Context context) {
        return context.getString(R.string.module_vnaas_name);
    }

    @Override
    public ComponentName getConfigActivity(Context context) {
        return null;
    }

    @Override
    public int getMinimumRefreshInterval(Context context) {
        return 0;
    }

    @Override
    public boolean requiresInternetConnectivity(Context context) {
        return true;
    }

    @Override
    public QuoteData getQuote(Context context) throws IOException {
        VnaasQuoteQueryParams query = new VnaasQuoteQueryParams();
        SharedPreferences preferences = context.getSharedPreferences(VnaasPrefKeys.PREF_VNAAS, Context.MODE_PRIVATE);

        String novelIdsStr = preferences.getString(VnaasPrefKeys.PREF_VNAAS_ENABLED_NOVELS, null);
        if (novelIdsStr != null) {
            query.setNovels(splitLongString(novelIdsStr));
        }

        String characterIdsStr = preferences.getString(VnaasPrefKeys.PREF_VNAAS_ENABLED_CHARACTERS, null);
        if (characterIdsStr != null) {
            query.setCharacters(splitLongString(characterIdsStr));
        }

        String contains = preferences.getString(VnaasPrefKeys.PREF_VNAAS_QUOTE_CONTAINS, null);
        if (contains != null) {
            query.setContains(contains);
        }

        String apiUrl = preferences.getString(VnaasPrefKeys.PREF_VNAAS_API_URL, VnaasPrefKeys.PREF_VNAAS_API_URL_DEFAULT);
        VnaasApiManager apiManager = new VnaasApiManager(apiUrl);
        VnaasQuote quote = apiManager.getRandomQuote(query);
        String quoteText = quote.getText().replaceAll("\\[(.+?)\\|(.+?)\\]", "$2");
        String charName = quote.getCharacter().getName();
        String novelName = quote.getNovel().getName();
        String sourceFormat = preferences.getString(VnaasPrefKeys.PREF_VNAAS_SOURCE_FORMAT, VnaasPrefKeys.PREF_VNAAS_SOURCE_FORMAT_DEFAULT);
        String quoteSource = String.format(sourceFormat, charName, novelName);
        return new QuoteData(quoteText, quoteSource);
    }

    private long[] splitLongString(String str) {
        String[] strSplit = str.split(",");
        long[] longs = new long[strSplit.length];
        for (int i = 0; i < longs.length; ++i) {
            longs[i] = Long.parseLong(strSplit[i]);
        }
        return longs;
    }
}
