package com.crossbowffs.quotelock.modules.hitokoto

import android.content.ComponentName
import android.content.Context
import com.crossbowffs.quotelock.R
import com.crossbowffs.quotelock.api.QuoteData
import com.crossbowffs.quotelock.api.QuoteModule
import com.crossbowffs.quotelock.api.QuoteModule.Companion.CHARACTER_TYPE_CJK
import com.crossbowffs.quotelock.modules.hitokoto.app.HitkotoConfigActivity
import com.crossbowffs.quotelock.modules.hitokoto.consts.HitokotoPrefKeys.PREF_HITOKOTO
import com.crossbowffs.quotelock.modules.hitokoto.consts.HitokotoPrefKeys.PREF_HITOKOTO_TYPE_STRING
import com.crossbowffs.quotelock.utils.downloadUrl
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class HitokotoQuoteModule : QuoteModule {
    override fun getDisplayName(context: Context): String? {
        return context.getString(R.string.module_hitokoto_name)
    }

    override fun getConfigActivity(context: Context): ComponentName? {
        return ComponentName(context, HitkotoConfigActivity::class.java)
    }

    override fun getMinimumRefreshInterval(context: Context): Int {
        return 0
    }

    override fun requiresInternetConnectivity(context: Context): Boolean {
        return true
    }

    @Throws(IOException::class, JSONException::class)
    override fun getQuote(context: Context): QuoteData {
        val sharedPreferences = context.getSharedPreferences(PREF_HITOKOTO, Context.MODE_PRIVATE)
        val type = sharedPreferences.getString(PREF_HITOKOTO_TYPE_STRING, "a")
        val url = "https://v1.hitokoto.cn/?c=$type"
        val quoteJson = url.downloadUrl()
        val quoteJsonObject = JSONObject(quoteJson)
        val quoteText = quoteJsonObject.getString("hitokoto")
        var quoteSource = "―"
        val quoteSourceFrom = quoteJsonObject.getString("from")
        val quoteSourceAuthor = quoteJsonObject.getString("from_who")
        quoteSource +=
            if (quoteSourceAuthor.isNullOrEmpty()) {
                quoteSourceFrom
            } else {
                "$quoteSourceAuthor $quoteSourceFrom"
            }
        return QuoteData(quoteText, quoteSource)
    }

    override val characterType: Int
        get() = CHARACTER_TYPE_CJK
}