package com.crossbowffs.quotelock.data

import androidx.datastore.preferences.core.Preferences
import com.crossbowffs.quotelock.app.font.FontManager
import com.crossbowffs.quotelock.consts.*
import com.crossbowffs.quotelock.data.api.QuoteModuleData
import com.crossbowffs.quotelock.data.api.QuoteStyle
import com.crossbowffs.quotelock.di.CommonDataStore
import com.crossbowffs.quotelock.utils.getComposeFontStyle
import com.crossbowffs.quotelock.utils.getComposeFontWeight
import com.yubyf.datastore.DataStoreDelegate
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Singleton
class ConfigurationRepository @Inject internal constructor(
    @CommonDataStore private val commonDataStore: DataStoreDelegate,
) {

    class DataStoreValue<T>(private val key: String, private val default: T) :
        ReadWriteProperty<ConfigurationRepository, T> {
        @Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
        override fun getValue(thisRef: ConfigurationRepository, property: KProperty<*>): T {
            return runBlocking {
                with(thisRef.commonDataStore) {
                    when (default) {
                        is Int -> getIntSuspend(key, default)
                        is String,
                        is String?,
                        -> getStringSuspend(key) ?: default
                        is Boolean -> getBooleanSuspend(key, default)
                        is Float -> getFloatSuspend(key, default)
                        is Long -> getLongSuspend(key, default)
                        is Set<*>?,
                        is Set<*>,
                        -> getStringSetSuspend(key)
                            ?: default as Set<String>
                        else -> throw IllegalArgumentException("Type not supported: ${default?.let { it::class } ?: "null"}")
                    } as T
                }
            }
        }

        override fun setValue(thisRef: ConfigurationRepository, property: KProperty<*>, value: T) {
            thisRef.commonDataStore.put(key, value)
        }
    }

    var displayOnAod: Boolean by DataStoreValue(PREF_COMMON_DISPLAY_ON_AOD, false)

    var currentModuleName: String by DataStoreValue(PREF_COMMON_QUOTE_MODULE,
        PREF_COMMON_QUOTE_MODULE_DEFAULT)

    var refreshInterval: Int
        get() = runBlocking {
            var refreshInterval =
                commonDataStore.getIntSuspend(PREF_COMMON_REFRESH_RATE_OVERRIDE, 0)
            if (refreshInterval == 0) {
                val refreshIntervalStr =
                    commonDataStore.getStringSuspend(PREF_COMMON_REFRESH_RATE)
                        ?: PREF_COMMON_REFRESH_RATE_DEFAULT
                refreshInterval = refreshIntervalStr.toInt()
            }
            refreshInterval
        }
        set(value) = commonDataStore.put(PREF_COMMON_REFRESH_RATE, value.toString())

    var isRequireInternet: Boolean by DataStoreValue(PREF_COMMON_REQUIRES_INTERNET, true)

    var isUnmeteredNetworkOnly: Boolean by DataStoreValue(PREF_COMMON_UNMETERED_ONLY,
        PREF_COMMON_UNMETERED_ONLY_DEFAULT)

    private var _quoteSize: String by DataStoreValue(PREF_COMMON_FONT_SIZE_TEXT,
        PREF_COMMON_FONT_SIZE_TEXT_DEFAULT)

    var quoteSize: Int
        get() = _quoteSize.toInt()
        set(value) {
            _quoteSize = value.toString()
        }

    private var _sourceSize: String by DataStoreValue(PREF_COMMON_FONT_SIZE_SOURCE,
        PREF_COMMON_FONT_SIZE_SOURCE_DEFAULT)

    var sourceSize: Int
        get() = _sourceSize.toInt()
        set(value) {
            _sourceSize = value.toString()
        }

    var quoteStyles: Set<String>? by DataStoreValue(PREF_COMMON_FONT_STYLE_TEXT, emptySet())

    var sourceStyles: Set<String>? by DataStoreValue(PREF_COMMON_FONT_STYLE_SOURCE, emptySet())

    var fontFamily: String by DataStoreValue(PREF_COMMON_FONT_FAMILY,
        PREF_COMMON_FONT_FAMILY_DEFAULT)

    private var _quoteSpacing: String by DataStoreValue(PREF_COMMON_QUOTE_SPACING,
        PREF_COMMON_QUOTE_SPACING_DEFAULT)

    var quoteSpacing: Int
        get() = _quoteSpacing.toInt()
        set(value) {
            _quoteSpacing = value.toString()
        }

    private var _paddingTop: String by DataStoreValue(PREF_COMMON_PADDING_TOP,
        PREF_COMMON_PADDING_TOP_DEFAULT)

    var paddingTop: Int
        get() = _paddingTop.toInt()
        set(value) {
            _paddingTop = value.toString()
        }

    private var _paddingBottom: String by DataStoreValue(PREF_COMMON_PADDING_BOTTOM,
        PREF_COMMON_PADDING_BOTTOM_DEFAULT)

    var paddingBottom: Int
        get() = _paddingBottom.toInt()
        set(value) {
            _paddingBottom = value.toString()
        }

    fun updateConfiguration(moduleData: QuoteModuleData) {
        if (moduleData.minimumRefreshInterval != 0) {
            commonDataStore.put(PREF_COMMON_REFRESH_RATE_OVERRIDE,
                moduleData.minimumRefreshInterval)
        } else {
            commonDataStore.remove(PREF_COMMON_REFRESH_RATE_OVERRIDE)
        }

        isRequireInternet = moduleData.requiresInternetConnectivity
    }

    val quoteStyle: QuoteStyle
        get() {
            // Font properties
            val quoteFontStyle = getComposeFontStyle(quoteStyles)
            val sourceFontStyle = getComposeFontStyle(sourceStyles)
            val quoteFontWeight = getComposeFontWeight(quoteStyles)
            val sourceFontWeight = getComposeFontWeight(sourceStyles)
            fontFamily.takeIf { PREF_COMMON_FONT_FAMILY_DEFAULT != it }?.runCatching {
                FontManager.loadTypeface(this)
            }?.getOrNull()
            val typeface =
                fontFamily.takeIf { PREF_COMMON_FONT_FAMILY_DEFAULT != it }?.runCatching {
                    FontManager.loadTypeface(this)
                }?.getOrNull()

            return QuoteStyle(
                quoteSize = quoteSize,
                sourceSize = sourceSize,
                quoteTypeface = typeface,
                quoteFontWeight = quoteFontWeight,
                quoteFontStyle = quoteFontStyle,
                sourceTypeface = typeface,
                sourceFontWeight = sourceFontWeight,
                sourceFontStyle = sourceFontStyle,
                quoteSpacing = quoteSpacing,
                paddingTop = paddingTop,
                paddingBottom = paddingBottom
            )
        }

    suspend fun observeConfigurationDataStore(collector: suspend (Preferences, Preferences.Key<*>?) -> Unit) =
        commonDataStore.collectSuspend(collector)
}