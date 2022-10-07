package com.crossbowffs.quotelock.data.api

import com.crossbowffs.quotelock.consts.PREF_QUOTE_SOURCE_PREFIX
import com.crossbowffs.quotelock.utils.decodeHex
import com.crossbowffs.quotelock.utils.hexString
import java.nio.ByteBuffer

/**
 * Holds the data for a quote.
 *
 * @param quoteText The first line (text) of the quote. The parameters should not be `null`.
 * @param quoteSource The second line (source part) of the quote. The parameters should not be `null`.
 * @param quoteAuthor The second line (author part) of the quote. The parameters should not be `null`.
 */
data class QuoteData(
    val quoteText: String = "",
    val quoteSource: String = "",
    val quoteAuthor: String = "",
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
            val bufferSize =
                Int.SIZE_BYTES + textBytes.size + Int.SIZE_BYTES + sourceBytes.size +
                        Int.SIZE_BYTES + authorBytes.size
            val byteBuffer = ByteBuffer.allocate(bufferSize)
                .putInt(textBytes.size)
                .put(textBytes)
                .putInt(sourceBytes.size)
                .put(sourceBytes)
                .putInt(authorBytes.size)
                .put(authorBytes)
            return byteBuffer.array().hexString()
        }

    companion object {
        fun fromByteString(byteString: String): QuoteData {
            return byteString.decodeHex().let {
                val buffer = ByteBuffer.wrap(it)
                if (buffer.remaining() < Int.SIZE_BYTES) {
                    return QuoteData()
                }
                val textBytes = ByteArray(buffer.int)
                buffer.get(textBytes)
                val sourceBytes = ByteArray(buffer.int)
                buffer.get(sourceBytes)
                val authorBytes = ByteArray(buffer.int)
                buffer.get(authorBytes)
                QuoteData(
                    quoteText = String(textBytes),
                    quoteSource = String(sourceBytes),
                    quoteAuthor = String(authorBytes),
                )
            }
        }
    }
}

data class QuoteDataWithCollectState(
    val quoteText: String = "",
    val quoteSource: String = "",
    val quoteAuthor: String = "",
    val collectState: Boolean? = null,
) {
    val readableSource: String
        get() = buildReadableSource(quoteSource, quoteAuthor)

    val readableSourceWithPrefix: String
        get() = readableSource.takeIf { it.isNotBlank() }?.let { PREF_QUOTE_SOURCE_PREFIX + it }
            .orEmpty()
}

fun QuoteData.withCollectState(state: Boolean? = null) = QuoteDataWithCollectState(
    quoteText = quoteText,
    quoteSource = quoteSource,
    quoteAuthor = quoteAuthor,
    collectState = state
)

fun QuoteDataWithCollectState.toQuoteData() = QuoteData(
    quoteText = quoteText,
    quoteSource = quoteSource,
    quoteAuthor = quoteAuthor
)