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

        String quoteAllText = getRss();

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

        String quoteText = quoteTextMatcher.group(0);
        String quoteSource = String.format(Locale.ENGLISH, "― %s", quoteCharacterMatcher.group(0));
        return new QuoteData(quoteText, quoteSource);
    }

    /**
     * Get the quotes_of_the_day rss feed from the Goodreads.com
     *
     * @return the string of xml
     * @throws IOException
     */
    public String getRss() throws IOException {
        URL urlToHandle;
        int responsecode;
        HttpURLConnection urlConnection;
        urlToHandle = new URL("http://www.goodreads.com/quotes_of_the_day/rss");
        urlConnection = (HttpURLConnection) urlToHandle.openConnection();
        responsecode = urlConnection.getResponseCode();
        if (responsecode == 200) {
            String xml = IOUtils.streamToString(urlConnection.getInputStream());
            Xlog.i(TAG, "获取到的源码：%s", xml);
            return xml;
        } else {
            Xlog.i(TAG, "获取不到网页的源码，服务器响应代码为：%s", responsecode);
            return null;
        }
    }
}
