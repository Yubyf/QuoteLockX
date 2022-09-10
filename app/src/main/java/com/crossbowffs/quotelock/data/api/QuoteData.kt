package com.crossbowffs.quotelock.data.api

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
}

data class QuoteDataWithCollectState(
    val quoteText: String = "",
    val quoteSource: String = "",
    val quoteAuthor: String = "",
    val collectState: Boolean? = null,
) {
    val readableSource: String
        get() = buildReadableSource(quoteSource, quoteAuthor)
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