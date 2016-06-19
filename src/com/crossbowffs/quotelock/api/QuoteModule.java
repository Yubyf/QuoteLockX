package com.crossbowffs.quotelock.api;

import android.content.ComponentName;
import android.content.Context;

public interface QuoteModule {
    /**
     * Gets the user-friendly name of the quote provider that this module uses.
     * Must not return {@code null}.
     */
    String getDisplayName(Context context);

    /**
     * Gets the configuration {@link android.app.Activity} for this module.
     * Returns {@code null} if there is no configuration activity.
     */
    ComponentName getConfigActivity(Context context);

    /**
     * Returns a minimum refresh interval (in seconds) for the quote source.
     * If there is no minimum refresh interval, returns 0. If the quote should
     * never automatically be refreshed, returns {@link Integer#MAX_VALUE}.
     */
    int getMinimumRefreshInterval(Context context);

    /**
     * Gets a new quote from the quote provider. This method is executed on a
     * background thread, so you should not need to use any async calls.
     * May return {@code null} or throw an exception in the case of an error.
     */
    QuoteData getQuote(Context context) throws Exception;
}
