package com.crossbowffs.quotelock.modules.hitokoto;

import android.content.ComponentName;
import android.content.Context;
import com.crossbowffs.quotelock.R;
import com.crossbowffs.quotelock.api.QuoteData;
import com.crossbowffs.quotelock.api.QuoteModule;
import com.crossbowffs.quotelock.utils.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class HitokotoQuoteModule implements QuoteModule {
    private static final String TAG = HitokotoQuoteModule.class.getSimpleName();

    @Override
    public String getDisplayName(Context context) {
        return context.getString(R.string.module_hitokoto_name);
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
    public QuoteData getQuote(Context context) throws IOException, JSONException {
        String quoteJson = IOUtils.downloadString("https://sslapi.hitokoto.cn/");

        JSONObject quoteJsonObject = new JSONObject(quoteJson);
        String quoteText = quoteJsonObject.getString("hitokoto");
        String quoteSource = "―" + quoteJsonObject.getString("from");

        return new QuoteData(quoteText, quoteSource);
    }
}
