package com.crossbowffs.quotelock.modules.wikiquote;

import android.content.ComponentName;
import android.content.Context;

import com.crossbowffs.quotelock.R;
import com.crossbowffs.quotelock.api.QuoteData;
import com.crossbowffs.quotelock.api.QuoteModule;
import com.crossbowffs.quotelock.modules.goodreadsqotd.GoodreadsQuoteModule;
import com.crossbowffs.quotelock.utils.IOUtils;
import com.crossbowffs.quotelock.utils.Xlog;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
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
        Xlog.i(TAG, "Quote page 提取：%s", quotePage);

        String regexQuoteAllText = "(?<=<td>).*?(?=</td>)";
        String regexRemoveFormat = "<.*?>";
        Matcher quoteAllTextMatcher = Pattern.compile(regexQuoteAllText).matcher(quotePage);
        boolean quoteAllTextStatus = quoteAllTextMatcher.find();
        Xlog.i(TAG, "Quote 带格式文本提取成功？：%s", quoteAllTextStatus);
        Matcher quoteRemoveFormatMatcher = Pattern.compile(regexRemoveFormat).matcher(quoteAllTextMatcher.group(0));
        String quoteAllText = quoteRemoveFormatMatcher.replaceAll("");
        boolean quoteRemoveFormatStatus = quoteRemoveFormatMatcher.find();
        Xlog.i(TAG, "Quote 去除格式文本提取成功？：%s", quoteRemoveFormatStatus);
        Xlog.i(TAG, "Quote 去除格式文本提取：%s", quoteAllText);

        String regexQuoteText = "^.*?(?=( |)(——|--))";
        Matcher quoteTextMatcher = Pattern.compile(regexQuoteText).matcher(quoteAllText);
        boolean quoteTextStatus = quoteTextMatcher.find();
        Xlog.i(TAG, "Quote Text 提取成功？：%s", quoteTextStatus);
        Xlog.i(TAG, "Quote Text：%s", quoteTextMatcher.group(0));

        String regexQuoteCharacter = "(?<=(——|--)( |)).*?$";
        Matcher quoteCharacterMatcher = Pattern.compile(regexQuoteCharacter).matcher(quoteAllText);
        boolean quoteCharacterStatus = quoteCharacterMatcher.find();
        Xlog.i(TAG, "Quote Character 提取成功？：%s", quoteCharacterStatus);
        Xlog.i(TAG, "Quote Character ：%s", quoteCharacterMatcher.group(0));

        if (quoteTextStatus || quoteCharacterStatus) {
            String quoteText = quoteTextMatcher.group(0);
            String quoteSource = String.format("―%s", quoteCharacterMatcher.group(0));
            return new QuoteData(quoteText, quoteSource);
        } else {
            return null;
        }
    }
}
