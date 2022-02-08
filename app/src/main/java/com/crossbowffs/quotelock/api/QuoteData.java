package com.crossbowffs.quotelock.api;

/**
 * Holds the data for a quote. Really just an overglorified
 * String tuple class.
 */
public class QuoteData {
    private final String mQuoteText;
    private final String mQuoteSource;

    /**
     * Creates a new quote data object. The parameters may be {@code null},
     * in which case they are replaced with an empty string.
     *
     * @param quoteText The first line (text) of the quote.
     * @param quoteSource The second line (author) of the quote.
     */
    public QuoteData(String quoteText, String quoteSource) {
        mQuoteText = quoteText;
        mQuoteSource = quoteSource;
    }

    /**
     * Gets the first line of the quote. Will not be {@code null}.
     */
    public String getQuoteText() {
        String text = mQuoteText;
        if (text == null) {
            text = "";
        }
        return text;
    }

    /**
     * Gets the second line of the quote. Will not be {@code null}.
     */
    public String getQuoteSource() {
        String source = mQuoteSource;
        if (source == null) {
            source = "";
        }
        return source;
    }
}
