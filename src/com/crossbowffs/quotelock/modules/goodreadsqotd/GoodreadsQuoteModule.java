package com.crossbowffs.quotelock.modules.goodreadsqotd;

import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import com.crossbowffs.quotelock.R;
import com.crossbowffs.quotelock.api.QuoteData;
import com.crossbowffs.quotelock.api.QuoteModule;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoodreadsQuoteModule implements QuoteModule {

    @Override
    public String getDisplayName(Context context) {
        return context.getString(R.string.module_goodreadsQotd_name);
    }

    @Override
    public ComponentName getConfigActivity(Context context) {
        return null;
    }

    @Override
    public QuoteData getQuote(Context context) throws Exception {

        String quoteAllText = getRss();

        String regexQuoteText = "(?<=<div>\n).*?(?=\n</div>)";
        Pattern quoteTextPattern = Pattern.compile(regexQuoteText);
        Matcher quoteTextMatcher = quoteTextPattern.matcher(quoteAllText);

        String regexQuoteCharacter = "(?<=\">).+?(?=</a>)";
        Pattern quoteCharacterPattern = Pattern.compile(regexQuoteCharacter);
        Matcher quoteCharacterMatcher = quoteCharacterPattern.matcher(quoteAllText);

        boolean quoteTextStatus = quoteTextMatcher.find();
        boolean quoteCharacterStatus = quoteCharacterMatcher.find();
        Log.i("Quote Text 提取成功？：", "" + quoteTextStatus);
        Log.i("Quote Text：", "" + quoteTextMatcher.group(0));
        Log.i("Quote Character 提取成功？：", "" + quoteCharacterStatus);
        Log.i("Quote Character ：", "" + quoteCharacterMatcher.group(0));

        String quoteText = quoteTextMatcher.group(0);
        String quoteSource = String.format(Locale.ENGLISH, "― %s", quoteCharacterMatcher.group(0));
        return new QuoteData(quoteText, quoteSource);
    }

    /**
     * Get the quotes_of_the_day rss feed from the Goodreads.com
     *
     * @return the string of xml
     * @throws Exception
     */
    public String getRss() throws Exception {
        URL urlToHandle;
        int responsecode;
        HttpURLConnection urlConnection;
        BufferedReader reader;
        String line;

        try {
            urlToHandle = new URL("http://www.goodreads.com/quotes_of_the_day/rss");
            urlConnection = (HttpURLConnection) urlToHandle.openConnection();
            responsecode = urlConnection.getResponseCode();
            if (responsecode == 200) {
                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "utf-8"));
                StringBuilder sb = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                String xml = sb.toString();
                Log.i("获取到的源码", "" + xml);
                return xml;
            } else {
                Log.i("获取不到网页的源码，服务器响应代码为：", "" + responsecode);
                return ("" + responsecode);
            }
        } catch (Exception e) {
            Log.i("获取不到网页的源码, 出现异常：", "" + e);
            return ("" + e);
        }
    }
}
