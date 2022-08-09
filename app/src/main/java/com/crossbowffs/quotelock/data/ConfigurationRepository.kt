package com.crossbowffs.quotelock.data

import androidx.datastore.preferences.core.Preferences
import com.crossbowffs.quotelock.app.font.FontManager
import com.crossbowffs.quotelock.consts.*
import com.crossbowffs.quotelock.data.api.QuoteModuleData
import com.crossbowffs.quotelock.data.api.QuoteStyle
import com.crossbowffs.quotelock.data.datastore.PreferenceDataStoreAdapter
import com.crossbowffs.quotelock.di.CommonDataStore
import com.crossbowffs.quotelock.utils.dp2px
import com.crossbowffs.quotelock.utils.getTypefaceStyle
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigurationRepository @Inject internal constructor(
    @CommonDataStore private val commonDataStore: PreferenceDataStoreAdapter,
) {

    fun getCurrentModuleName() =
        commonDataStore.getString(PREF_COMMON_QUOTE_MODULE, PREF_COMMON_QUOTE_MODULE_DEFAULT)!!

    fun updateConfiguration(moduleData: QuoteModuleData) {
        if (moduleData.minimumRefreshInterval != 0) {
            commonDataStore.putInt(PREF_COMMON_REFRESH_RATE_OVERRIDE,
                moduleData.minimumRefreshInterval)
        } else {
            commonDataStore.remove(PREF_COMMON_REFRESH_RATE_OVERRIDE)
        }

        if (!moduleData.requiresInternetConnectivity) {
            commonDataStore.putBoolean(PREF_COMMON_REQUIRES_INTERNET, false)
        } else {
            commonDataStore.remove(PREF_COMMON_REQUIRES_INTERNET)
        }
    }

    fun getRefreshInterval(): Int {
        var refreshInterval = commonDataStore.getInt(PREF_COMMON_REFRESH_RATE_OVERRIDE, 0)
        if (refreshInterval == 0) {
            val refreshIntervalStr =
                commonDataStore.getString(PREF_COMMON_REFRESH_RATE,
                    PREF_COMMON_REFRESH_RATE_DEFAULT)!!
            refreshInterval = refreshIntervalStr.toInt()
        }
        return refreshInterval
    }

    fun isRequireInternet() = commonDataStore.getBoolean(PREF_COMMON_REQUIRES_INTERNET, true)

    fun isUnmeteredNetworkOnly() = commonDataStore.getBoolean(PREF_COMMON_UNMETERED_ONLY,
        PREF_COMMON_UNMETERED_ONLY_DEFAULT)

    fun getQuoteStyle(): QuoteStyle {
        val quoteSize = commonDataStore.getString(PREF_COMMON_FONT_SIZE_TEXT,
            PREF_COMMON_FONT_SIZE_TEXT_DEFAULT)!!.toFloat()
        val sourceSize = commonDataStore.getString(PREF_COMMON_FONT_SIZE_SOURCE,
            PREF_COMMON_FONT_SIZE_SOURCE_DEFAULT)!!.toFloat()
        // Font properties
        val quoteStyles = commonDataStore.getStringSet(PREF_COMMON_FONT_STYLE_TEXT, null)
        val sourceStyles = commonDataStore.getStringSet(PREF_COMMON_FONT_STYLE_SOURCE, null)
        val quoteStyle = getTypefaceStyle(quoteStyles)
        val sourceStyle = getTypefaceStyle(sourceStyles)
        val font = commonDataStore.getString(
            PREF_COMMON_FONT_FAMILY, PREF_COMMON_FONT_FAMILY_DEFAULT)
        val typeface = if (PREF_COMMON_FONT_FAMILY_DEFAULT != font) {
            font?.let {
                runCatching { FontManager.loadTypeface(font) }.getOrNull()
            }
        } else {
            null
        }

        // Quote spacing
        val quoteSpacing = commonDataStore.getString(PREF_COMMON_QUOTE_SPACING,
            PREF_COMMON_QUOTE_SPACING_DEFAULT)!!.toInt().dp2px().toInt()
        // Layout padding
        val paddingTop = commonDataStore.getString(PREF_COMMON_PADDING_TOP,
            PREF_COMMON_PADDING_TOP_DEFAULT)!!.toInt().dp2px().toInt()
        val paddingBottom = commonDataStore.getString(PREF_COMMON_PADDING_BOTTOM,
            PREF_COMMON_PADDING_BOTTOM_DEFAULT)!!.toInt().dp2px().toInt()
        return QuoteStyle(quoteSize,
            sourceSize,
            typeface,
            quoteStyle,
            typeface,
            sourceStyle,
            quoteSpacing,
            paddingTop,
            paddingBottom)
    }

    suspend fun observeConfigurationDataStore(collector: suspend (Preferences, Preferences.Key<*>?) -> Unit) =
        commonDataStore.collectSuspend(collector)
}