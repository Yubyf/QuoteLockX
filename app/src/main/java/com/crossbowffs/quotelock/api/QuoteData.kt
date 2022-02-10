package com.crossbowffs.quotelock.api

/**
 * Holds the data for a quote.
 *
 * @param quoteText The first line (text) of the quote. The parameters should not be `null`.
 * @param quoteSource The second line (author) of the quote. The parameters should not be `null`.
 */
data class QuoteData(val quoteText: String = "", val quoteSource: String = "")