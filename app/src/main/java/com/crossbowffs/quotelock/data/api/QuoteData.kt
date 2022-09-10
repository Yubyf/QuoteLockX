package com.crossbowffs.quotelock.data.api

import com.crossbowffs.quotelock.consts.PREF_QUOTE_SOURCE_PREFIX

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
        get() = PREF_QUOTE_SOURCE_PREFIX + readableSource
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
        get() = PREF_QUOTE_SOURCE_PREFIX + readableSource
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