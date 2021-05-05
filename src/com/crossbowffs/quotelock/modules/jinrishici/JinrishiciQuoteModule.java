package com.crossbowffs.quotelock.modules.jinrishici;

import android.content.ComponentName;
import android.content.Context;
import android.text.TextUtils;

import com.crossbowffs.quotelock.R;
import com.crossbowffs.quotelock.api.QuoteData;
import com.crossbowffs.quotelock.api.QuoteModule;
import com.crossbowffs.quotelock.utils.Xlog;
import com.jinrishici.sdk.android.JinrishiciClient;
import com.jinrishici.sdk.android.model.DataBean;
import com.jinrishici.sdk.android.model.JinrishiciRuntimeException;
import com.jinrishici.sdk.android.model.OriginBean;
import com.jinrishici.sdk.android.model.PoetySentence;

import java.io.IOException;

public class JinrishiciQuoteModule implements QuoteModule {
    private static final String TAG = JinrishiciQuoteModule.class.getSimpleName();

    @Override
    public String getDisplayName(Context context) {
        return context.getString(R.string.module_jinrishici_name);
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
    public QuoteData getQuote(Context context) throws IOException {
        try {
            PoetySentence poetySentence = JinrishiciClient.getInstance().getOneSentence();
            // Content
            DataBean data = poetySentence.getData();
            if (TextUtils.isEmpty(data.getContent())) {
                return null;
            }
            String quoteText = data.getContent();

            // Source
            OriginBean origin = data.getOrigin();
            String dynasty = origin.getDynasty();
            String author = origin.getAuthor();
            String title = origin.getTitle();
            String quoteSource = "";
            if (!TextUtils.isEmpty(dynasty)) {
                quoteSource += "―" + dynasty;
            }
            if (!TextUtils.isEmpty(author)) {
                quoteSource += "·" + author;
            }
            if (!TextUtils.isEmpty(title)) {
                quoteSource += " 《" + title + "》";
            }
            return new QuoteData(quoteText, quoteSource);
        } catch (JinrishiciRuntimeException | NullPointerException e) {
            Xlog.e(TAG, "Failed to get Jinrishici result.", e);
            return null;
        }
    }
}
