package com.crossbowffs.quotelock.modules.brainyquote;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.crossbowffs.quotelock.R;
import com.crossbowffs.quotelock.api.QuoteData;
import com.crossbowffs.quotelock.api.QuoteModule;
import com.crossbowffs.quotelock.modules.brainyquote.app.BrainyquoteQuoteConfigActivity;
import com.crossbowffs.quotelock.utils.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;

import static com.crossbowffs.quotelock.consts.PrefKeys.PREF_QUOTES;
import static com.crossbowffs.quotelock.consts.PrefKeys.PREF_QUOTES_BRAINY_TYPE_STRING;

public class BrainyquoteQuoteModule implements QuoteModule {

    @Override
    public String getDisplayName(Context context) {
        return context.getString(R.string.module_brainy_name);
    }

    @Override
    public ComponentName getConfigActivity(Context context) {
        return new ComponentName(context, BrainyquoteQuoteConfigActivity.class);
    }

    @Override
    public int getMinimumRefreshInterval(Context context) {
        return 86400;
    }

    @Override
    public boolean requiresInternetConnectivity(Context context) {
        return true;
    }

    @Override
    public QuoteData getQuote(Context context) throws IOException {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_QUOTES, Context.MODE_PRIVATE);

        String URL = String.format("http://feeds.feedburner.com/brainyquote/QUOTE%s",
                sharedPreferences.getString(PREF_QUOTES_BRAINY_TYPE_STRING, "BR"));

        String rssXml = IOUtils.downloadString(URL);
        Document document = Jsoup.parse(rssXml);

        String quoteText = document.select("item > description").first().text();
        String quoteSource = String.format("― %s", document.select("item > title").first().text());

        return new QuoteData(quoteText.substring(1, quoteText.length()-1), quoteSource);
    }
}
