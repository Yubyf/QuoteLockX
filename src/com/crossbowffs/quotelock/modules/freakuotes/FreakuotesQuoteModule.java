package com.crossbowffs.quotelock.modules.freakuotes;

import android.content.ComponentName;
import android.content.Context;
import android.text.TextUtils;
import com.crossbowffs.quotelock.R;
import com.crossbowffs.quotelock.api.QuoteData;
import com.crossbowffs.quotelock.api.QuoteModule;
import com.crossbowffs.quotelock.utils.IOUtils;
import com.crossbowffs.quotelock.utils.Xlog;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class FreakuotesQuoteModule implements QuoteModule {
    private static final String TAG = FreakuotesQuoteModule.class.getSimpleName();

    @Override
    public String getDisplayName(Context context) {
        return context.getString(R.string.module_freakuotes_name);
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
    public QuoteData getQuote(Context context) throws Exception {
        String html = IOUtils.downloadString("https://freakuotes.com/frase/aleatoria");
        Document document = Jsoup.parse(html);
        Element quoteContainer = document.select(".quote-container > blockquote").first();

        String quoteText = quoteContainer.getElementsByTag("p").text();
        if (TextUtils.isEmpty(quoteText)) {
            Xlog.e(TAG, "Failed to find quote text");
            return null;
        }

        String sourceLeft = quoteContainer.select("footer > span").text();
        String sourceRight = quoteContainer.select("footer > cite").attr("title");
        String quoteSource;
        if (TextUtils.isEmpty(sourceLeft) && TextUtils.isEmpty(sourceRight)) {
            Xlog.w(TAG, "Quote source not found");
            quoteSource = "";
        } else if (TextUtils.isEmpty(sourceLeft)) {
            quoteSource = "―" + sourceRight;
        } else if (TextUtils.isEmpty(sourceRight)) {
            quoteSource = "―" + sourceLeft;
        } else {
            quoteSource = String.format("―%s, %s", sourceLeft, sourceRight);
        }

        return new QuoteData(quoteText, quoteSource);
    }
}
