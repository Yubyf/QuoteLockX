package com.crossbowffs.quotelock.modules.hitokoto;

import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import com.crossbowffs.quotelock.R;
import com.crossbowffs.quotelock.api.QuoteData;
import com.crossbowffs.quotelock.api.QuoteModule;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HitokotoQuoteModule implements QuoteModule {
    @Override
    public String getDisplayName(Context context) {
        return context.getString(R.string.module_Hitokoto_name);
    }

    @Override
    public ComponentName getConfigActivity(Context context) {
        return null;
    }

    @Override
    public QuoteData getQuote(Context context) throws Exception {

        String quoteJson = getHitokotoApi();

        Gson gsonQuote = new Gson();
        quoteInformation resolvedQuote = gsonQuote.fromJson(quoteJson, quoteInformation.class);
        String quoteSourceProject = resolvedQuote.source;
        String quoteSource = "";
        String quoteText = resolvedQuote.hitokoto;

        if (quoteSourceProject == null || quoteSourceProject.isEmpty()) {
            Log.i("Hitokoto Quote:", "来源为空");
        } else {
            quoteSource = String.format("―%1$s", quoteSourceProject);
        }

        return new QuoteData(quoteText, quoteSource);
    }

    /**
     * Get the json from the api of Hitokoto.us.
     *
     * @return the string of json
     * @throws Exception
     */
    public String getHitokotoApi() throws Exception {
        URL urlToHandle;
        int responsecode;
        HttpURLConnection urlConnection;
        BufferedReader reader;
        String line;

        try {
            urlToHandle = new URL("http://api.hitokoto.us/rand");
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

    private class quoteInformation {
        private String hitokoto;
        private String source;
    }
}
