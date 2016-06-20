package com.crossbowffs.quotelock.modules.goodreads;

import android.content.ComponentName;
import android.content.Context;
import com.crossbowffs.quotelock.R;
import com.crossbowffs.quotelock.api.QuoteData;
import com.crossbowffs.quotelock.api.QuoteModule;
import com.crossbowffs.quotelock.utils.IOUtils;
import com.crossbowffs.quotelock.utils.Xlog;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoodreadsQuoteModule implements QuoteModule {
    private static final String TAG = GoodreadsQuoteModule.class.getSimpleName();

    @Override
    public String getDisplayName(Context context) {
        return context.getString(R.string.module_goodreads_name);
    }

    @Override
    public ComponentName getConfigActivity(Context context) {
        return null;
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
        String rssXml = IOUtils.downloadString("http://www.goodreads.com/quotes_of_the_day/rss");
        Document document = Jsoup.parse(rssXml);
        String quoteXml = document.select("item > description").first().text();
        Document quoteDocument = Jsoup.parse(quoteXml);
        String quoteAllText = quoteDocument.text();
        Xlog.d(TAG, "Downloaded text: %s", quoteAllText);

        Matcher quoteMatcher = Pattern.compile("(.*?) - (.*?)").matcher(quoteAllText);
        if (!quoteMatcher.matches()) {
            Xlog.e(TAG, "Failed to parse quote");
            return null;
        }

        String quoteText = quoteMatcher.group(1);
        String quoteSource = "―" + quoteMatcher.group(2);
        return new QuoteData(quoteText, quoteSource);
    }
}
