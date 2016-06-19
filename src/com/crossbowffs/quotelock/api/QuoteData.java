package com.crossbowffs.quotelock.api;

public class QuoteData {
    private final String mQuoteText;
    private final String mQuoteSource;

    public QuoteData(String quoteText, String quoteSource) {
        mQuoteText = quoteText;
        mQuoteSource = quoteSource;
    }

    public String getQuoteText() {
        String text = mQuoteText;
        if (text == null) {
            text = "";
        }
        return text;
    }

    public String getQuoteSource() {
        String source = mQuoteSource;
        if (source == null) {
            source = "";
        }
        return source;
    }
}
