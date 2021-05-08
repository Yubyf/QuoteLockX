package com.crossbowffs.quotelock.modules.brainyquote;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import com.crossbowffs.quotelock.R;
import com.crossbowffs.quotelock.api.QuoteData;
import com.crossbowffs.quotelock.api.QuoteModule;
import com.crossbowffs.quotelock.modules.brainyquote.app.BrainyQuoteConfigActivity;
import com.crossbowffs.quotelock.utils.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

import static com.crossbowffs.quotelock.modules.brainyquote.consts.BrainyQuotePrefKeys.PREF_BRAINY;
import static com.crossbowffs.quotelock.modules.brainyquote.consts.BrainyQuotePrefKeys.PREF_BRAINY_TYPE_STRING;

public class BrainyQuoteQuoteModule implements QuoteModule {

    @Override
    public String getDisplayName(Context context) {
        return context.getString(R.string.module_brainy_name);
    }

    @Override
    public ComponentName getConfigActivity(Context context) {
        return new ComponentName(context, BrainyQuoteConfigActivity.class);
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
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_BRAINY, Context.MODE_PRIVATE);
        String type = sharedPreferences.getString(PREF_BRAINY_TYPE_STRING, "BR");
        String URL = String.format("http://feeds.feedburner.com/brainyquote/QUOTE%s", type);

        String rssXml = IOUtils.downloadString(URL);
        Document document = Jsoup.parse(rssXml);

        String quoteText = document.select("item > description").first().text();
        String quoteSource = String.format("― %s", document.select("item > title").first().text());

        return new QuoteData(quoteText.substring(1, quoteText.length()-1), quoteSource);
    }

    @Override
    public int getCharacterType() {
        return CHARACTER_TYPE_LATIN;
    }
}
