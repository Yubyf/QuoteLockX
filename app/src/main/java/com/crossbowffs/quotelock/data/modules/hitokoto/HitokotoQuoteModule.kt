package com.crossbowffs.quotelock.data.modules.hitokoto

import android.content.Context
import com.crossbowffs.quotelock.app.configs.hitokoto.HitkotoNavigation
import com.crossbowffs.quotelock.app.configs.hitokoto.HitokotoPrefKeys.PREF_HITOKOTO
import com.crossbowffs.quotelock.app.configs.hitokoto.HitokotoPrefKeys.PREF_HITOKOTO_LEGACY_TYPE_STRING
import com.crossbowffs.quotelock.app.configs.hitokoto.HitokotoPrefKeys.PREF_HITOKOTO_TYPES_STRING
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.data.api.QuoteModule
import com.crossbowffs.quotelock.data.api.QuoteModule.Companion.CHARACTER_TYPE_CJK
import com.crossbowffs.quotelock.data.api.QuoteModule.Companion.httpClient
import com.crossbowffs.quotelock.di.QuoteModuleEntryPoint
import com.crossbowffs.quotelock.utils.fetchJson
import com.yubyf.quotelockx.R
import dagger.hilt.android.EntryPointAccessors
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.json.JSONException
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
    override suspend fun Context.getQuote(): QuoteData {
        val dataStore =
            EntryPointAccessors.fromApplication<QuoteModuleEntryPoint>(applicationContext)
                .hitokotoDataStore()
        val types = dataStore.getStringSetSuspend(PREF_HITOKOTO_TYPES_STRING) ?: run {
            dataStore.getStringSuspend(PREF_HITOKOTO_LEGACY_TYPE_STRING)?.let { setOf(it) }
        } ?: setOf("a")
        val url = "https://v1.hitokoto.cn/?${types.joinToString("&") { "c=$it" }}"
        val hitokotoResponse = httpClient.fetchJson<HitokotoResponse>(url)
        return QuoteData(
            quoteText = hitokotoResponse.hitokoto,
            quoteSource = hitokotoResponse.from.orEmpty(),
            quoteAuthor = hitokotoResponse.fromWho.orEmpty(),
            provider = PREF_HITOKOTO
        )
    }

    override val characterType: Int
        get() = CHARACTER_TYPE_CJK
}

@Serializable
data class HitokotoResponse(
    val id: Int,
    val uuid: String,
    val hitokoto: String,
    val type: String,
    val from: String?,
    @SerialName("from_who")
    val fromWho: String?,
)