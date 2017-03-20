package com.crossbowffs.quotelock.modules.natune;

import android.content.ComponentName;
import android.content.Context;
import com.crossbowffs.quotelock.R;
import com.crossbowffs.quotelock.api.QuoteData;
import com.crossbowffs.quotelock.api.QuoteModule;
import com.crossbowffs.quotelock.utils.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class NatuneQuoteModule implements QuoteModule {
    @Override
    public String getDisplayName(Context context) {
        return context.getString(R.string.module_natune_name);
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
    public QuoteData getQuote(Context context) throws Exception {
        String html = IOUtils.downloadString("https://natune.net/zitate/Zufalls5");
        Document document = Jsoup.parse(html);
        Element quoteLi = document.select(".quotes > li").first();
        String quoteText = quoteLi.getElementsByClass("quote_text").first().text();
        String quoteAuthor = quoteLi.getElementsByClass("quote_author").first().text();
        return new QuoteData(quoteText, "― " + quoteAuthor);
    }
}
