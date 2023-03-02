package com.crossbowffs.quotelock.data.api

import com.crossbowffs.quotelock.consts.PREF_QUOTE_SOURCE_PREFIX
import com.crossbowffs.quotelock.data.modules.jinrishici.JinrishiciQuoteModule
import com.crossbowffs.quotelock.utils.decodeHex
import com.crossbowffs.quotelock.utils.hexString
import com.crossbowffs.quotelock.utils.md5
import com.crossbowffs.quotelock.utils.readLvBytes
import com.crossbowffs.quotelock.utils.readLvString
import com.crossbowffs.quotelock.utils.writeLvBytes
import com.crossbowffs.quotelock.utils.writeLvString
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

/**
 * Holds the data for a quote.
 *
 * @param quoteText The first line (text) of the quote. The parameters should not be `null`.
 * @param quoteSource The second line (source part) of the quote. The parameters should not be `null`.
 * @param quoteAuthor The second line (author part) of the quote. The parameters should not be `null`.
 * @param provider The provider of the quote. The parameters should not be `null`.
 * @param uid The unique ID of the quote. Default value is the MD5 hash of the quote text, source, author and provider.
 * @param extra The extra data of the quote. Default value is `null`.
 */
data class QuoteData(
    val quoteText: String = "",
    val quoteSource: String = "",
    val quoteAuthor: String = "",
    val provider: String = "",
    val uid: String = "$quoteText$quoteSource$quoteAuthor$provider".md5(),
    val extra: ByteArray? = null,
) {

    val readableSource: String
        get() = buildReadableSource(quoteSource, quoteAuthor)

    val readableSourceWithPrefix: String
        get() = readableSource.takeIf { it.isNotBlank() }?.let { PREF_QUOTE_SOURCE_PREFIX + it }
            .orEmpty()

    val byteString: String
        get() = ByteArrayOutputStream().use { stream ->
            stream.run {
                writeLvString(quoteText)
                writeLvString(quoteSource)
                writeLvString(quoteAuthor)
                writeLvString(provider)
                writeLvString(uid)
                extra?.let(::writeLvBytes)
                toByteArray().hexString()
            }
        }

    companion object {
        fun fromByteString(byteString: String): QuoteData = byteString.decodeHex().runCatching {
            ByteBuffer.wrap(this).run {
                val quoteText = readLvString().orEmpty()
                val quoteSource = readLvString().orEmpty()
                val quoteAuthor = readLvString().orEmpty()
                if (remaining() < Int.SIZE_BYTES) {
                    QuoteData(
                        quoteText = quoteText,
                        quoteSource = quoteSource,
                        quoteAuthor = quoteAuthor,
                    )
                } else {
                    val provider = readLvString().orEmpty()
                    val uid = readLvString().orEmpty()
                    val extra = if (remaining() >= Int.SIZE_BYTES) {
                        readLvBytes()
                    } else {
                        null
                    }
                    QuoteData(
                        quoteText = quoteText,
                        quoteSource = quoteSource,
                        quoteAuthor = quoteAuthor,
                        provider = provider,
                        uid = uid,
                        extra = extra
                    )
                }
            }
        }.getOrDefault(QuoteData())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QuoteData

        if (quoteText != other.quoteText) return false
        if (quoteSource != other.quoteSource) return false
        if (quoteAuthor != other.quoteAuthor) return false
        if (provider != other.provider) return false
        if (uid != other.uid) return false
        if (extra != null) {
            if (other.extra == null) return false
            if (!extra.contentEquals(other.extra)) return false
        } else if (other.extra != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = quoteText.hashCode()
        result = 31 * result + quoteSource.hashCode()
        result = 31 * result + quoteAuthor.hashCode()
        result = 31 * result + provider.hashCode()
        result = 31 * result + uid.hashCode()
        result = 31 * result + (extra?.contentHashCode() ?: 0)
        return result
    }
}

data class QuoteDataWithCollectState(
    val quote: QuoteData = QuoteData(),
    val collectState: Boolean? = null,
) {
    val quoteText: String
        get() = quote.quoteText
    val quoteSource: String
        get() = quote.quoteSource
    val quoteAuthor: String
        get() = quote.quoteAuthor
    val provider: String
        get() = quote.provider
    val uid: String
        get() = quote.uid

    val readableSource: String
        get() = quote.readableSource

    val readableSourceWithPrefix: String
        get() = quote.readableSourceWithPrefix
}

fun QuoteData.withCollectState(state: Boolean? = null) = QuoteDataWithCollectState(
    quote = this,
    collectState = state
)

val QuoteData.hasDetailData: Boolean
    get() = extra != null && provider == JinrishiciQuoteModule.PREF_JINRISHICI