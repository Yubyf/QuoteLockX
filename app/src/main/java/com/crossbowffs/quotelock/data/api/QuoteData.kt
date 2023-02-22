package com.crossbowffs.quotelock.data.api

import com.crossbowffs.quotelock.consts.PREF_QUOTE_SOURCE_PREFIX
import com.crossbowffs.quotelock.utils.decodeHex
import com.crossbowffs.quotelock.utils.hexString
import com.crossbowffs.quotelock.utils.md5
import java.nio.ByteBuffer

/**
 * Holds the data for a quote.
 *
 * @param quoteText The first line (text) of the quote. The parameters should not be `null`.
 * @param quoteSource The second line (source part) of the quote. The parameters should not be `null`.
 * @param quoteAuthor The second line (author part) of the quote. The parameters should not be `null`.
 * @param provider The provider of the quote. The parameters should not be `null`.
 * @param uid The unique ID of the quote. Default value is the MD5 hash of the quote text, source, author and provider.
 */
data class QuoteData(
    val quoteText: String = "",
    val quoteSource: String = "",
    val quoteAuthor: String = "",
    val provider: String = "",
    val uid: String = "$quoteText$quoteSource$quoteAuthor$provider".md5(),
) {

    val readableSource: String
        get() = buildReadableSource(quoteSource, quoteAuthor)

    val readableSourceWithPrefix: String
        get() = readableSource.takeIf { it.isNotBlank() }?.let { PREF_QUOTE_SOURCE_PREFIX + it }
            .orEmpty()

    val byteString: String
        get() {
            val textBytes = quoteText.toByteArray()
            val sourceBytes = quoteSource.toByteArray()
            val authorBytes = quoteAuthor.toByteArray()
            val providerBytes = provider.toByteArray()
            val uidBytes = uid.toByteArray()
            val bufferSize =
                Int.SIZE_BYTES + textBytes.size + Int.SIZE_BYTES + sourceBytes.size +
                        Int.SIZE_BYTES + authorBytes.size + Int.SIZE_BYTES + providerBytes.size +
                        Int.SIZE_BYTES + uidBytes.size
            val byteBuffer = ByteBuffer.allocate(bufferSize)
                .putInt(textBytes.size)
                .put(textBytes)
                .putInt(sourceBytes.size)
                .put(sourceBytes)
                .putInt(authorBytes.size)
                .put(authorBytes)
                .putInt(providerBytes.size)
                .put(providerBytes)
                .putInt(uidBytes.size)
                .put(uidBytes)
            return byteBuffer.array().hexString()
        }

    companion object {
        fun fromByteString(byteString: String): QuoteData {
            return byteString.decodeHex().runCatching {
                val buffer = ByteBuffer.wrap(this)
                val textBytes = ByteArray(buffer.int)
                buffer.get(textBytes)
                val sourceBytes = ByteArray(buffer.int)
                buffer.get(sourceBytes)
                val authorBytes = ByteArray(buffer.int)
                buffer.get(authorBytes)
                if (buffer.remaining() < Int.SIZE_BYTES) {
                    QuoteData(
                        quoteText = String(textBytes),
                        quoteSource = String(sourceBytes),
                        quoteAuthor = String(authorBytes),
                    )
                } else {
                    val providerBytes = ByteArray(buffer.int)
                    buffer.get(providerBytes)
                    val uidBytes = ByteArray(buffer.int)
                    buffer.get(uidBytes)
                    QuoteData(
                        quoteText = String(textBytes),
                        quoteSource = String(sourceBytes),
                        quoteAuthor = String(authorBytes),
                        provider = String(providerBytes),
                        uid = String(uidBytes),
                    )
                }
            }.getOrDefault(QuoteData())
        }
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