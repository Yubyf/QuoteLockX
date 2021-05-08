package com.crossbowffs.quotelock.api;

import android.content.ComponentName;
import android.content.Context;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Provides an API for querying the information of a
 * quote provider and fetching quotes from that provider. Note that
 * all functions other than {@link QuoteModule#getQuote(Context)}
 * should return immediately, since they will be called on the
 * UI thread.
 */
public interface QuoteModule {
    /**
     * Gets the user-friendly name of the quote provider that this module uses.
     * Must not return {@code null}.
     */
    String getDisplayName(Context context);

    /**
     * Gets a {@link ComponentName} representing the configuration
     * {@link android.app.Activity} for this module.
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
     * Whether the provider needs to download data from the internet.
     * Returns {@code false} for providers which store data locally on the
     * device.
     */
    boolean requiresInternetConnectivity(Context context);

    /**
     * Gets a new quote from the quote provider. This method is executed on a
     * background thread, so you should not need to use any async calls.
     * May return {@code null} or throw an exception in the case of an error.
     */
    QuoteData getQuote(Context context) throws Exception;

    int CHARACTER_TYPE_DEFAULT = 0;
    int CHARACTER_TYPE_LATIN = 1;
    int CHARACTER_TYPE_CJK = 2;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({CHARACTER_TYPE_DEFAULT, CHARACTER_TYPE_LATIN, CHARACTER_TYPE_CJK})
    @interface CharacterType {}

    /**
     * @return 0 - Default, 1 - Latin, 2 - CJK.
     */
    @CharacterType
    int getCharacterType();
}
