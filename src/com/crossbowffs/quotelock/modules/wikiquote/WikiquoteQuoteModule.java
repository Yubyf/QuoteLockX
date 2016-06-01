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

        String regexQuoteAllText = "(?<=<td>).*?(?=</td>)";
        Matcher quoteAllTextMatcher = Pattern.compile(regexQuoteAllText).matcher(quotePage);
        boolean quoteAllTextStatus = quoteAllTextMatcher.find();
        Xlog.i(TAG, "Quote 带格式文本提取成功？：%s", quoteAllTextStatus);
        if (!quoteAllTextStatus)
            return null;
        Xlog.i(TAG, "Quote 带格式文本提取：%s", quoteAllTextMatcher.group(0));

        String regexRemoveFormat = "<.*?>";
        Matcher quoteRemoveFormatMatcher = Pattern.compile(regexRemoveFormat).matcher(quoteAllTextMatcher.group(0));
        String quoteAllText = quoteRemoveFormatMatcher.replaceAll("");
        Xlog.i(TAG, "Quote 去除格式文本提取：%s", quoteAllText);
        boolean quoteRemoveFormatStatus = quoteRemoveFormatMatcher.find();
        Xlog.i(TAG, "Quote 去除格式文本提取成功？：%s", quoteRemoveFormatStatus);
        if (!quoteRemoveFormatStatus)
            return null;

        String regexQuoteText = "^.*?(?=(\\s|)(——|--))";
        Matcher quoteTextMatcher = Pattern.compile(regexQuoteText).matcher(quoteAllText);
        boolean quoteTextStatus = quoteTextMatcher.find();
        Xlog.i(TAG, "Quote Text 提取成功？：%s", quoteTextStatus);

        String regexQuoteCharacter = "(?<=(——|--)\\s).*?$";
        Matcher quoteCharacterMatcher = Pattern.compile(regexQuoteCharacter).matcher(quoteAllText);
        boolean quoteCharacterStatus = quoteCharacterMatcher.find();
        Xlog.i(TAG, "Quote Character 提取成功？：%s", quoteCharacterStatus);

        if (quoteTextStatus && quoteCharacterStatus) {
            String quoteText = quoteTextMatcher.group(0);
            String quoteSource = String.format("― %s", quoteCharacterMatcher.group(0));
            Xlog.i(TAG, "Quote Text：%s", quoteText);
            Xlog.i(TAG, "Quote Source ：%s", quoteSource);
            return new QuoteData(quoteText, quoteSource);
        } else {
            return null;
        }
    }
}
