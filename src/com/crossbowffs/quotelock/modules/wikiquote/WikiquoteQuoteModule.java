package com.crossbowffs.quotelock.modules.wikiquote;

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
    public QuoteData getQuote(Context context) throws IOException {
        String quotePage = IOUtils.downloadString("https://zh.m.wikiquote.org/zh-cn/Wikiquote:%E9%A6%96%E9%A1%B5");

        Matcher quoteAllTextMatcher = Pattern.compile("(?<=<td>).*?(?=</td>)").matcher(quotePage);
        if (!quoteAllTextMatcher.find()) {
            Xlog.e(TAG, "Failed to parse quote data");
            return null;
        }
        String quoteAllText = quoteAllTextMatcher.group(0).replaceAll("<.*?>", "");

        Matcher quoteTextMatcher = Pattern.compile("^.*?(?=(\\s|)(——|--))").matcher(quoteAllText);
        if (!quoteTextMatcher.find()) {
            Xlog.e(TAG, "Failed to parse quote text");
            return null;
        }

        Matcher quoteSourceMatcher = Pattern.compile("(?<=(——|--)\\s).*?$").matcher(quoteAllText);
        if (!quoteSourceMatcher.find()) {
            Xlog.e(TAG, "Failed to parse quote source");
            return null;
        }

        String quoteText = quoteTextMatcher.group(0);
        String quoteSource = String.format("―%s", quoteSourceMatcher.group(0));
        return new QuoteData(quoteText, quoteSource);
    }
}
