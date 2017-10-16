package com.crossbowffs.quotelock.modules.hitokoto;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;

import com.crossbowffs.quotelock.R;
import com.crossbowffs.quotelock.api.QuoteData;
import com.crossbowffs.quotelock.api.QuoteModule;
import com.crossbowffs.quotelock.modules.hitokoto.app.HitkotoConfigActivity;
import com.crossbowffs.quotelock.utils.IOUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static com.crossbowffs.quotelock.modules.hitokoto.consts.HitokotoPrefKeys.PREF_HITOKOTO;
import static com.crossbowffs.quotelock.modules.hitokoto.consts.HitokotoPrefKeys.PREF_HITOKOTO_TYPE_STRING;

public class HitokotoQuoteModule implements QuoteModule {

    @Override
    public String getDisplayName(Context context) {
        return context.getString(R.string.module_hitokoto_name);
    }

    @Override
    public ComponentName getConfigActivity(Context context) {
        return new ComponentName(context, HitkotoConfigActivity.class);
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
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_HITOKOTO, Context.MODE_PRIVATE);
        String type = sharedPreferences.getString(PREF_HITOKOTO_TYPE_STRING, "r");
        String URL = String.format("https://sslapi.hitokoto.cn/?c=%s", type);

        String quoteJson = IOUtils.downloadString(URL);

        JSONObject quoteJsonObject = new JSONObject(quoteJson);
        String quoteText = quoteJsonObject.getString("hitokoto");
        String quoteSource = "―" + quoteJsonObject.getString("from");

        return new QuoteData(quoteText, quoteSource);
    }
}
