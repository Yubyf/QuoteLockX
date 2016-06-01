package com.crossbowffs.quotelock.modules.goodreadsqotd;

import android.content.ComponentName;
import android.content.Context;

import com.crossbowffs.quotelock.R;
import com.crossbowffs.quotelock.api.QuoteData;
import com.crossbowffs.quotelock.api.QuoteModule;
import com.crossbowffs.quotelock.utils.IOUtils;
import com.crossbowffs.quotelock.utils.Xlog;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
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

        String regexQuoteText = "(?<=<div>\n).*?(?=\n</div>)";
        Pattern quoteTextPattern = Pattern.compile(regexQuoteText);
        Matcher quoteTextMatcher = quoteTextPattern.matcher(quoteAllText);

        String regexQuoteCharacter = "(?<=\">).+?(?=</a>)";
        Pattern quoteCharacterPattern = Pattern.compile(regexQuoteCharacter);
        Matcher quoteCharacterMatcher = quoteCharacterPattern.matcher(quoteAllText);

        boolean quoteTextStatus = quoteTextMatcher.find();
        boolean quoteCharacterStatus = quoteCharacterMatcher.find();
        Xlog.i(TAG, "Quote Text 提取成功？：%s", quoteTextStatus);
        Xlog.i(TAG, "Quote Text：%s", quoteTextMatcher.group(0));
        Xlog.i(TAG, "Quote Character 提取成功？：%s", quoteCharacterStatus);
        Xlog.i(TAG, "Quote Character ：%s", quoteCharacterMatcher.group(0));

        if (quoteTextStatus || quoteCharacterStatus) {
            String quoteText = quoteTextMatcher.group(0);
            String quoteSource = String.format("― %s", quoteCharacterMatcher.group(0));
            return new QuoteData(quoteText, quoteSource);
        } else {
            return  null;
        }
    }
}
