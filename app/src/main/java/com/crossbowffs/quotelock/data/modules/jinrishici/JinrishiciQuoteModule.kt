package com.crossbowffs.quotelock.data.modules.jinrishici

import android.content.Context
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.data.api.QuoteModule
import com.crossbowffs.quotelock.data.api.QuoteModule.Companion.CHARACTER_TYPE_CJK
import com.crossbowffs.quotelock.data.api.QuoteModule.Companion.httpClient
import com.crossbowffs.quotelock.data.modules.jinrishici.detail.JinrishiciDetailData
import com.crossbowffs.quotelock.di.QuoteModuleEntryPoint
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.className
import com.crossbowffs.quotelock.utils.fetchJson
import com.crossbowffs.quotelock.utils.prefix
import com.yubyf.quotelockx.R
import dagger.hilt.android.EntryPointAccessors
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.json.JSONException
import java.io.IOException

class JinrishiciQuoteModule : QuoteModule {

    companion object {
        private val TAG = className<JinrishiciQuoteModule>()
        const val PREF_JINRISHICI = "jinrishici"
        private const val PREF_JINRISHICI_TOKEN = "pref_jinrishici_token"
        private const val PREF_JINRISHICI_TOKEN_URL = "https://v2.jinrishici.com/token"
        private const val PREF_JINRISHICI_SENTENCE_URL = "https://v2.jinrishici.com/sentence"
    }

    override fun getDisplayName(context: Context): String {
        return context.getString(R.string.module_jinrishici_name)
    }

    override fun getConfigRoute(): String? = null

    override fun getMinimumRefreshInterval(context: Context): Int {
        return 0
    }

    override fun requiresInternetConnectivity(context: Context): Boolean {
        return true
    }

    @Throws(IOException::class, JSONException::class)
    override suspend fun Context.getQuote(): QuoteData? {
        val dataStore =
            EntryPointAccessors.fromApplication<QuoteModuleEntryPoint>(applicationContext)
                .jinrishiciDataStore()
        return try {
            var token = dataStore.getStringSuspend(PREF_JINRISHICI_TOKEN, null)
            if (token.isNullOrBlank()) {
                val tokenResponse = httpClient.fetchJson<TokenResponse>(PREF_JINRISHICI_TOKEN_URL)
                Xlog.d(TAG, "token response $tokenResponse")
                token = tokenResponse.data
                if (token.isEmpty()) {
                    Xlog.e(TAG, "Failed to get Jinrishici token.")
                    return null
                } else {
                    dataStore.put(PREF_JINRISHICI_TOKEN, token)
                }
            }
            val headers = mapOf<String, String?>("X-User-Token" to token)
            val sentence =
                httpClient.fetchJson<SentenceResponse>(
                    PREF_JINRISHICI_SENTENCE_URL,
                    headers = headers
                )
            Xlog.d(TAG, "poetry sentence $sentence")
            val status = sentence.status
            if (status != "success") {
                Xlog.e(TAG, "Failed to get Jinrishici result, error code - ${sentence.errCode}")
                return null
            }
            val sentenceData = sentence.data

            // Uid
            val uid = sentenceData.id

            // Content
            val quoteText = sentenceData.content
            if (quoteText.isEmpty()) {
                return null
            }

            // Source
            val originData = sentenceData.origin
            val dynasty = originData.dynasty
            val author = originData.author
            val title = originData.title
            val originContent = originData.content
            val originTranslate = originData.translate
            val tags = sentenceData.matchTags

            val quoteSource = title.takeIf { it.isNotEmpty() }?.prefix("《")?.plus("》").orEmpty()
            val quoteAuthor =
                // Dynasty
                dynasty.takeIf { it.isNotEmpty() }.orEmpty()
                    // Author
                    .plus(author.takeIf { it.isNotEmpty() }?.prefix("·").orEmpty())
            val detailData = JinrishiciDetailData(
                title,
                dynasty,
                author,
                originContent,
                originTranslate,
                tags
            )
            QuoteData(quoteText, quoteSource, quoteAuthor, PREF_JINRISHICI, uid, detailData.bytes)
        } catch (e: NullPointerException) {
            Xlog.e(TAG, "Failed to get Jinrishici result.", e)
            null
        }
    }

    override val characterType: Int
        get() = CHARACTER_TYPE_CJK

    @Serializable
    data class TokenResponse(
        override val status: String,
        override val data: String,
        @SerialName("errcode")
        override val errCode: String? = null,
    ) : JinrishiciResponse<String>

    @Serializable
    data class SentenceResponse(
        override val status: String,
        override val data: SentenceData,
        @SerialName("errcode")
        override val errCode: String? = null,
    ) : JinrishiciResponse<SentenceData>

    @Serializable
    data class SentenceData(
        val id: String,
        val content: String,
        val popularity: Int,
        val origin: OriginData,
        val matchTags: List<String>,
        val recommendedReason: String,
        val cacheAt: String,
    )

    @Serializable
    data class OriginData(
        val title: String,
        val dynasty: String,
        val author: String,
        val content: List<String>,
        val translate: List<String>? = emptyList(),
    )

    interface JinrishiciResponse<T> {
        val status: String
        val data: T?

        val errCode: String?
    }
}