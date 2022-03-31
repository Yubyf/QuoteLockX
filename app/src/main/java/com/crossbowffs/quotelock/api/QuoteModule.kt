package com.crossbowffs.quotelock.api

import android.content.ComponentName
import android.content.Context
import androidx.annotation.IntDef

/**
 * Provides an API for querying the information of a
 * quote provider and fetching quotes from that provider. Note that
 * all functions other than [QuoteModule.getQuote]
 * should return immediately, since they will be called on the
 * UI thread.
 */
interface QuoteModule {
    /**
     * Gets the user-friendly name of the quote provider that this module uses.
     * Must not return `null`.
     */
    fun getDisplayName(context: Context): String

    /**
     * Gets a [ComponentName] representing the configuration
     * [android.app.Activity] for this module.
     * Returns `null` if there is no configuration activity.
     */
    fun getConfigActivity(context: Context): ComponentName?

    /**
     * Returns a minimum refresh interval (in seconds) for the quote source.
     * If there is no minimum refresh interval, returns 0. If the quote should
     * never automatically be refreshed, returns [Integer.MAX_VALUE].
     */
    fun getMinimumRefreshInterval(context: Context): Int

    /**
     * Whether the provider needs to download data from the internet.
     * Returns `false` for providers which store data locally on the
     * device.
     */
    fun requiresInternetConnectivity(context: Context): Boolean

    /**
     * Gets a new quote from the quote provider. This function is a suspending function
     * executed on a  background thread, so you should not need to use any async calls.
     * May return `null` or throw an exception in the case of an error.
     */
    @Throws(Exception::class)
    suspend fun getQuote(context: Context): QuoteData?

    companion object {
        const val CHARACTER_TYPE_DEFAULT = 0
        const val CHARACTER_TYPE_LATIN = 1
        const val CHARACTER_TYPE_CJK = 2
    }

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(CHARACTER_TYPE_DEFAULT, CHARACTER_TYPE_LATIN, CHARACTER_TYPE_CJK)
    annotation class CharacterType

    /**
     * @return 0 - Default, 1 - Latin, 2 - CJK.
     */
    @get:CharacterType
    val characterType: Int
}