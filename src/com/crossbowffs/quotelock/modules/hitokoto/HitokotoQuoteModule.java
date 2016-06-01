package com.crossbowffs.quotelock.modules.hitokoto;

import android.content.ComponentName;
import android.content.Context;
import android.text.TextUtils;

import com.crossbowffs.quotelock.R;
import com.crossbowffs.quotelock.api.QuoteData;
import com.crossbowffs.quotelock.api.QuoteModule;
import com.crossbowffs.quotelock.utils.IOUtils;
import com.crossbowffs.quotelock.utils.Xlog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

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
    public QuoteData getQuote(Context context) throws IOException, JSONException {

        String quoteJson = getHitokotoApi();

        JSONObject quoteJsonObject = new JSONObject(quoteJson);
        String quoteSourceProject = quoteJsonObject.getString("source");
        String quoteText = quoteJsonObject.getString("hitokoto");
        String quoteSource = "";

        if (!TextUtils.isEmpty(quoteSourceProject)) {
            quoteSource = String.format("― %s", quoteSourceProject);
        }

        return new QuoteData(quoteText, quoteSource);
    }

    /**
     * Get the json from the api of Hitokoto.us.
     *
     * @return the string of json
     * @throws IOException
     */
    public String getHitokotoApi() throws IOException {
        URL urlToHandle = new URL("http://api.hitokoto.us/rand");
        HttpURLConnection urlConnection = (HttpURLConnection) urlToHandle.openConnection();
        int responseCode = urlConnection.getResponseCode();
        if (responseCode == 200) {
            String xml = IOUtils.streamToString(urlConnection.getInputStream());
            Xlog.i(TAG, "获取到的源码：%s", xml);
            return xml;
        } else {
            Xlog.i(TAG, "获取不到网页的源码，服务器响应代码为：%s", responseCode);
            throw new IOException();
        }

    }
}
