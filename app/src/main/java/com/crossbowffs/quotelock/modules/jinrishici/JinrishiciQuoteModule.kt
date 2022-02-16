package com.crossbowffs.quotelock.modules.jinrishici

import android.content.ComponentName
import android.content.Context
import com.crossbowffs.quotelock.R
import com.crossbowffs.quotelock.api.QuoteData
import com.crossbowffs.quotelock.api.QuoteModule
import com.crossbowffs.quotelock.api.QuoteModule.Companion.CHARACTER_TYPE_CJK
import com.crossbowffs.quotelock.modules.jinrishici.consts.JinrishiciPrefKeys.PREF_JINRISHICI
import com.crossbowffs.quotelock.modules.jinrishici.consts.JinrishiciPrefKeys.PREF_JINRISHICI_SENTENCE_URL
import com.crossbowffs.quotelock.modules.jinrishici.consts.JinrishiciPrefKeys.PREF_JINRISHICI_TOKEN
import com.crossbowffs.quotelock.modules.jinrishici.consts.JinrishiciPrefKeys.PREF_JINRISHICI_TOKEN_URL
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.className
import com.crossbowffs.quotelock.utils.downloadUrl
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class JinrishiciQuoteModule : QuoteModule {

    companion object {
        private val TAG = className<JinrishiciQuoteModule>()
    }

    override fun getDisplayName(context: Context): String {
        return context.getString(R.string.module_jinrishici_name)
    }

    override fun getConfigActivity(context: Context): ComponentName? {
        return null
    }

    override fun getMinimumRefreshInterval(context: Context): Int {
        return 0
    }

    override fun requiresInternetConnectivity(context: Context): Boolean {
        return true
    }

    @Throws(IOException::class, JSONException::class)
    override fun getQuote(context: Context): QuoteData? {
        return try {
            val sharedPreferences = context.getSharedPreferences(PREF_JINRISHICI,
                Context.MODE_PRIVATE)
            var token = sharedPreferences.getString(PREF_JINRISHICI_TOKEN, null)
            if (token.isNullOrBlank()) {
                val tokenJson = PREF_JINRISHICI_TOKEN_URL.downloadUrl()
                Xlog.d(TAG, "tokenJson $tokenJson")
                val tokenJsonObject = JSONObject(tokenJson)
                token = tokenJsonObject.getString("data")
                if (token.isNullOrEmpty()) {
                    Xlog.e(TAG, "Failed to get Jinrishici token.")
                    return null
                } else {
                    sharedPreferences.edit().putString(PREF_JINRISHICI_TOKEN, token).apply()
                }
            }
            val headers = mapOf<String, String?>("X-User-Token" to token)
            val poetrySentenceJson = PREF_JINRISHICI_SENTENCE_URL.downloadUrl(headers)
            Xlog.d(TAG, "poetrySentenceJson $poetrySentenceJson")
            val poetrySentenceJsonObject = JSONObject(poetrySentenceJson)
            val status = poetrySentenceJsonObject.getString("status")
            if (status != "success") {
                val errorCode = poetrySentenceJsonObject.getString("errcode")
                Xlog.e(TAG, "Failed to get Jinrishici result, error code - $errorCode")
                return null
            }
            val poetrySentenceData = poetrySentenceJsonObject.getJSONObject("data")

            // Content
            val quoteText = poetrySentenceData.getString("content")
            if (quoteText.isNullOrEmpty()) {
                return null
            }

            // Source
            val originData = poetrySentenceData.getJSONObject("origin")
            val dynasty = originData.getString("dynasty")
            val author = originData.getString("author")
            val title = originData.getString("title")
            var quoteSource = ""
            if (!dynasty.isNullOrEmpty()) {
                quoteSource += "―$dynasty"
            }
            if (!author.isNullOrEmpty()) {
                quoteSource += "·$author"
            }
            if (!title.isNullOrEmpty()) {
                quoteSource += " 《$title》"
            }
            QuoteData(quoteText, quoteSource)
        } catch (e: NullPointerException) {
            Xlog.e(TAG, "Failed to get Jinrishici result.", e)
            null
        }
    }

    override val characterType: Int
        get() = CHARACTER_TYPE_CJK
}