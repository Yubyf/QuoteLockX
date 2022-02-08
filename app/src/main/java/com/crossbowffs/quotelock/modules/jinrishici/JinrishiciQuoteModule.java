package com.crossbowffs.quotelock.modules.jinrishici;

import static com.crossbowffs.quotelock.modules.jinrishici.consts.JinrishiciPrefKeys.PREF_JINRISHICI;
import static com.crossbowffs.quotelock.modules.jinrishici.consts.JinrishiciPrefKeys.PREF_JINRISHICI_SENTENCE_URL;
import static com.crossbowffs.quotelock.modules.jinrishici.consts.JinrishiciPrefKeys.PREF_JINRISHICI_TOKEN;
import static com.crossbowffs.quotelock.modules.jinrishici.consts.JinrishiciPrefKeys.PREF_JINRISHICI_TOKEN_URL;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.crossbowffs.quotelock.R;
import com.crossbowffs.quotelock.api.QuoteData;
import com.crossbowffs.quotelock.api.QuoteModule;
import com.crossbowffs.quotelock.utils.IOUtils;
import com.crossbowffs.quotelock.utils.Xlog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
    public QuoteData getQuote(Context context) throws IOException, JSONException {
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_JINRISHICI,
                    Context.MODE_PRIVATE);
            String token = sharedPreferences.getString(PREF_JINRISHICI_TOKEN, null);
            if (TextUtils.isEmpty(token)) {
                String tokenJson = IOUtils.downloadString(PREF_JINRISHICI_TOKEN_URL);
                Xlog.d(TAG, "tokenJson " + tokenJson);
                JSONObject tokenJsonObject = new JSONObject(tokenJson);
                token = tokenJsonObject.getString("data");
                if (TextUtils.isEmpty(token)) {
                    Xlog.e(TAG, "Failed to get Jinrishici token.");
                    return null;
                } else {
                    sharedPreferences.edit().putString(PREF_JINRISHICI_TOKEN, token).apply();
                }
            }

            Map<String, String> headers = new HashMap<>(1);
            headers.put("X-User-Token", token);
            String poetrySentenceJson = IOUtils.downloadString(PREF_JINRISHICI_SENTENCE_URL, headers);
            Xlog.d(TAG, "poetrySentenceJson " + poetrySentenceJson);
            JSONObject poetrySentenceJsonObject = new JSONObject(poetrySentenceJson);
            String status = poetrySentenceJsonObject.getString("status");
            if (!Objects.equals(status, "success")) {
                String errorCode = poetrySentenceJsonObject.getString("errcode");
                Xlog.e(TAG, "Failed to get Jinrishici result, error code - " + errorCode);
                return null;
            }
            JSONObject poetrySentenceData = poetrySentenceJsonObject.getJSONObject("data");

            // Content
            String quoteText = poetrySentenceData.getString("content");
            if (TextUtils.isEmpty(quoteText)) {
                return null;
            }

            // Source
            JSONObject originData = poetrySentenceData.getJSONObject("origin");

            String dynasty = originData.getString("dynasty");
            String author = originData.getString("author");
            String title = originData.getString("title");
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
        } catch (NullPointerException e) {
            Xlog.e(TAG, "Failed to get Jinrishici result.", e);
            return null;
        }
    }

    @Override
    public int getCharacterType() {
        return CHARACTER_TYPE_CJK;
    }
}
