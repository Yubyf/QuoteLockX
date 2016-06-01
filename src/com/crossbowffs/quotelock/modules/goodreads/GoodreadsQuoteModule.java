package com.crossbowffs.quotelock.modules.goodreads;

import android.content.ComponentName;
import android.content.Context;

import com.crossbowffs.quotelock.R;
import com.crossbowffs.quotelock.api.QuoteData;
import com.crossbowffs.quotelock.api.QuoteModule;
import com.crossbowffs.quotelock.utils.IOUtils;
import com.crossbowffs.quotelock.utils.Xlog;

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
    public QuoteData getQuote(Context context) throws IOException {
        String quoteAllText = IOUtils.downloadString("http://www.goodreads.com/quotes_of_the_day/rss");

        Matcher quoteTextMatcher = Pattern.compile("(?<=<div>\n).*?(?=\n</div>)").matcher(quoteAllText);
        if (!quoteTextMatcher.find()) {
            Xlog.e(TAG, "Failed to parse quote text");
            return null;
        }

        Matcher quoteSourceMatcher = Pattern.compile("(?<=\">).+?(?=</a>)").matcher(quoteAllText);
        if (!quoteSourceMatcher.find()) {
            Xlog.e(TAG, "Failed to parse quote source");
            return null;
        }

        String quoteText = quoteTextMatcher.group(0);
        String quoteSource = String.format("― %s", quoteSourceMatcher.group(0));
        return new QuoteData(quoteText, quoteSource);
    }
}
