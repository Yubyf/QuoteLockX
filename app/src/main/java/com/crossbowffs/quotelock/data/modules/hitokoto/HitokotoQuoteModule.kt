package com.crossbowffs.quotelock.data.modules.hitokoto

import android.content.Context
import com.crossbowffs.quotelock.app.configs.hitokoto.HitkotoNavigation
import com.crossbowffs.quotelock.app.configs.hitokoto.HitokotoPrefKeys.PREF_HITOKOTO
import com.crossbowffs.quotelock.app.configs.hitokoto.HitokotoPrefKeys.PREF_HITOKOTO_LEGACY_TYPE_STRING
import com.crossbowffs.quotelock.app.configs.hitokoto.HitokotoPrefKeys.PREF_HITOKOTO_TYPES_STRING
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.data.api.QuoteModule
import com.crossbowffs.quotelock.data.api.QuoteModule.Companion.CHARACTER_TYPE_CJK
import com.crossbowffs.quotelock.di.QuoteModuleEntryPoint
import com.crossbowffs.quotelock.utils.downloadUrl
import com.yubyf.quotelockx.R
import dagger.hilt.android.EntryPointAccessors
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class HitokotoQuoteModule : QuoteModule {
    override fun getDisplayName(context: Context): String {
        return context.getString(R.string.module_hitokoto_name)
    }

    override fun getConfigRoute(): String = HitkotoNavigation.route

    override fun getMinimumRefreshInterval(context: Context): Int {
        return 0
    }

    override fun requiresInternetConnectivity(context: Context): Boolean {
        return true
    }

    @Throws(IOException::class, JSONException::class)
    override suspend fun getQuote(context: Context): QuoteData {
        val dataStore =
            EntryPointAccessors.fromApplication<QuoteModuleEntryPoint>(context.applicationContext)
                .hitokotoDataStore()
        val types = dataStore.getStringSetSuspend(PREF_HITOKOTO_TYPES_STRING) ?: run {
            dataStore.getStringSuspend(PREF_HITOKOTO_LEGACY_TYPE_STRING)?.let { setOf(it) }
        } ?: setOf("a")
        val url = "https://v1.hitokoto.cn/?${types.joinToString("&") { "c=$it" }}"
        val quoteJson = url.downloadUrl()
        val quoteJsonObject = JSONObject(quoteJson)
        val quoteText = quoteJsonObject.getString("hitokoto")
        val quoteSource = quoteJsonObject.getString("from")
        val quoteAuthor = quoteJsonObject.getString("from_who")
        return QuoteData(
            quoteText = quoteText,
            quoteSource = quoteSource ?: "",
            quoteAuthor = if (quoteAuthor == "null") "" else quoteAuthor,
            provider = PREF_HITOKOTO
        )
    }

    override val characterType: Int
        get() = CHARACTER_TYPE_CJK
}