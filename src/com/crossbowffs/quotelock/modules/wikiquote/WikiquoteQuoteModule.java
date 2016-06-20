package com.crossbowffs.quotelock.modules.wikiquote;

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

public class WikiquoteQuoteModule implements QuoteModule {
    private static final String TAG = WikiquoteQuoteModule.class.getSimpleName();

    @Override
    public String getDisplayName(Context context) {
        return context.getString(R.string.module_wikiquote_name);
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
        String html = IOUtils.downloadString("https://zh.m.wikiquote.org/zh-cn/Wikiquote:%E9%A6%96%E9%A1%B5");
        Document document = Jsoup.parse(html);
        String quoteAllText = document.select("#mf-qotd td").get(1).text();
        Xlog.d(TAG, "Downloaded text: %s", quoteAllText);

        Matcher quoteMatcher = Pattern.compile("(.*?)\\s*[\\u2500\\u2014\\u002D]{2}\\s*(.*?)").matcher(quoteAllText);
        if (!quoteMatcher.matches()) {
            Xlog.e(TAG, "Failed to parse quote");
            return null;
        }

        String quoteText = quoteMatcher.group(1);
        String quoteSource = String.format("―%s", quoteMatcher.group(2));
        return new QuoteData(quoteText, quoteSource);
    }
}
