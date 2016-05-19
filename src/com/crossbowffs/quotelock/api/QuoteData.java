package com.crossbowffs.quotelock.api;

public class QuoteData {
    private final String mQuoteText;
    private final String mQuoteSource;

    public QuoteData(String quoteText, String quoteSource) {
        mQuoteText = quoteText;
        mQuoteSource = quoteSource;
    }

    public String getQuoteText() {
        return mQuoteText;
    }

    public String getQuoteSource() {
        return mQuoteSource;
    }
}
