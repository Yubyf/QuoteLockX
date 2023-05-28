package com.crossbowffs.quotelock.data

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.crossbowffs.quotelock.consts.PREF_COMMON_DISPLAY_ON_AOD
import com.crossbowffs.quotelock.consts.PREF_COMMON_FONT_FAMILY_DEFAULT_SANS_SERIF
import com.crossbowffs.quotelock.consts.PREF_COMMON_FONT_LEGACY_FAMILY
import com.crossbowffs.quotelock.consts.PREF_COMMON_FONT_SIZE_SOURCE
import com.crossbowffs.quotelock.consts.PREF_COMMON_FONT_SIZE_SOURCE_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_COMMON_FONT_SIZE_TEXT
import com.crossbowffs.quotelock.consts.PREF_COMMON_FONT_SIZE_TEXT_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_COMMON_FONT_STYLE_SOURCE
import com.crossbowffs.quotelock.consts.PREF_COMMON_FONT_STYLE_SOURCE_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_COMMON_FONT_STYLE_TEXT
import com.crossbowffs.quotelock.consts.PREF_COMMON_FONT_STYLE_TEXT_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_COMMON_NIGHT_MODE
import com.crossbowffs.quotelock.consts.PREF_COMMON_NIGHT_MODE_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_COMMON_PADDING_BOTTOM
import com.crossbowffs.quotelock.consts.PREF_COMMON_PADDING_BOTTOM_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_COMMON_PADDING_TOP
import com.crossbowffs.quotelock.consts.PREF_COMMON_PADDING_TOP_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_COMMON_QUOTE_MODULE
import com.crossbowffs.quotelock.consts.PREF_COMMON_QUOTE_MODULE_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_COMMON_QUOTE_SPACING
import com.crossbowffs.quotelock.consts.PREF_COMMON_QUOTE_SPACING_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_COMMON_REFRESH_RATE
import com.crossbowffs.quotelock.consts.PREF_COMMON_REFRESH_RATE_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_COMMON_REFRESH_RATE_OVERRIDE
import com.crossbowffs.quotelock.consts.PREF_COMMON_REQUIRES_INTERNET
import com.crossbowffs.quotelock.consts.PREF_COMMON_UNMETERED_ONLY
import com.crossbowffs.quotelock.consts.PREF_COMMON_UNMETERED_ONLY_DEFAULT
import com.crossbowffs.quotelock.data.api.QuoteConfigs
import com.crossbowffs.quotelock.data.api.QuoteModuleData
import com.crossbowffs.quotelock.data.api.QuoteStyle
import com.crossbowffs.quotelock.data.api.TextFontStyle
import com.crossbowffs.quotelock.di.CommonDataStore
import com.crossbowffs.quotelock.di.IoDispatcher
import com.crossbowffs.quotelock.utils.getValueByDefault
import com.yubyf.datastore.DataStoreDelegate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Singleton
class ConfigurationRepository @Inject internal constructor(
    @CommonDataStore private val commonDataStore: DataStoreDelegate,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) {

    class DataStoreValue<T>(private val key: String, private val default: T) :
        ReadWriteProperty<ConfigurationRepository, T> {
        override fun getValue(thisRef: ConfigurationRepository, property: KProperty<*>): T {
            return runBlocking {
                thisRef.commonDataStore.getValueByDefault(key, default)
            }
        }

        override fun setValue(thisRef: ConfigurationRepository, property: KProperty<*>, value: T) {
            thisRef.commonDataStore.put(key, value)
        }
    }

    var nightMode: Int by DataStoreValue(PREF_COMMON_NIGHT_MODE, PREF_COMMON_NIGHT_MODE_DEFAULT)

    var displayOnAod: Boolean by DataStoreValue(PREF_COMMON_DISPLAY_ON_AOD, false)

    var currentModuleName: String by DataStoreValue(
        PREF_COMMON_QUOTE_MODULE,
        PREF_COMMON_QUOTE_MODULE_DEFAULT
    )

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

    var isUnmeteredNetworkOnly: Boolean by DataStoreValue(
        PREF_COMMON_UNMETERED_ONLY,
        PREF_COMMON_UNMETERED_ONLY_DEFAULT
    )

    private var _quoteSize: String by DataStoreValue(
        PREF_COMMON_FONT_SIZE_TEXT,
        PREF_COMMON_FONT_SIZE_TEXT_DEFAULT
    )

    var quoteSize: Int
        get() = _quoteSize.toInt()
        set(value) {
            _quoteSize = value.toString()
        }

    private var _sourceSize: String by DataStoreValue(
        PREF_COMMON_FONT_SIZE_SOURCE,
        PREF_COMMON_FONT_SIZE_SOURCE_DEFAULT
    )

    var sourceSize: Int
        get() = _sourceSize.toInt()
        set(value) {
            _sourceSize = value.toString()
        }

    val fontFamily: String by DataStoreValue(
        PREF_COMMON_FONT_LEGACY_FAMILY,
        PREF_COMMON_FONT_FAMILY_DEFAULT_SANS_SERIF
    )

    private var _quoteFontStyle: String? by DataStoreValue(
        PREF_COMMON_FONT_STYLE_TEXT,
        null
    )

    var quoteFontStyle: TextFontStyle
        get() = _quoteFontStyle?.let { TextFontStyle.fromByteString(it) }
            ?: TextFontStyle(family = fontFamily)
        set(value) {
            _quoteFontStyle = value.byteString
        }

    private var _sourceFontStyle: String? by DataStoreValue(
        PREF_COMMON_FONT_STYLE_SOURCE,
        null
    )

    var sourceFontStyle: TextFontStyle
        get() = _sourceFontStyle?.let { TextFontStyle.fromByteString(it) }
            ?: TextFontStyle(family = fontFamily)
        set(value) {
            _sourceFontStyle = value.byteString
        }

    private var _quoteSpacing: String by DataStoreValue(
        PREF_COMMON_QUOTE_SPACING,
        PREF_COMMON_QUOTE_SPACING_DEFAULT
    )

    var quoteSpacing: Int
        get() = _quoteSpacing.toInt()
        set(value) {
            _quoteSpacing = value.toString()
        }

    private var _paddingTop: String by DataStoreValue(
        PREF_COMMON_PADDING_TOP,
        PREF_COMMON_PADDING_TOP_DEFAULT
    )

    var paddingTop: Int
        get() = _paddingTop.toInt()
        set(value) {
            _paddingTop = value.toString()
        }

    private var _paddingBottom: String by DataStoreValue(
        PREF_COMMON_PADDING_BOTTOM,
        PREF_COMMON_PADDING_BOTTOM_DEFAULT
    )

    var paddingBottom: Int
        get() = _paddingBottom.toInt()
        set(value) {
            _paddingBottom = value.toString()
        }

    private val quoteStyle: QuoteStyle
        get() = QuoteStyle(
            quoteSize = quoteSize,
            sourceSize = sourceSize,
            quoteFontStyle = quoteFontStyle,
            sourceFontStyle = sourceFontStyle,
            quoteSpacing = quoteSpacing,
            paddingTop = paddingTop,
            paddingBottom = paddingBottom
        )

    private val _quoteStyleFlow = MutableStateFlow(quoteStyle)
    val quoteStyleFlow = _quoteStyleFlow.asStateFlow()

    private val _quoteConfigsFlow = MutableStateFlow(QuoteConfigs())
    val quoteConfigsFlow = _quoteConfigsFlow.asStateFlow()

    private val _quoteModuleNotifyFlow = MutableSharedFlow<Unit>()
    val quoteModuleNotifyFlow = _quoteModuleNotifyFlow.asSharedFlow()

    private val _quoteRefreshRateNotifyFlow = MutableSharedFlow<Unit>()
    val quoteRefreshRateNotifyFlow = _quoteRefreshRateNotifyFlow.asSharedFlow()

    init {
        commonDataStore.collectIn(CoroutineScope(dispatcher)) { preferences, key ->
            when (key?.name) {
                PREF_COMMON_FONT_SIZE_TEXT -> _quoteStyleFlow.update { currentState ->
                    currentState.copy(
                        quoteSize = (preferences[stringPreferencesKey(PREF_COMMON_FONT_SIZE_TEXT)]
                            ?: PREF_COMMON_FONT_SIZE_TEXT_DEFAULT).toInt()
                    )
                }

                PREF_COMMON_FONT_SIZE_SOURCE -> _quoteStyleFlow.update { currentState ->
                    currentState.copy(
                        sourceSize = (preferences[stringPreferencesKey(PREF_COMMON_FONT_SIZE_SOURCE)]
                            ?: PREF_COMMON_FONT_SIZE_SOURCE_DEFAULT).toInt()
                    )
                }

                PREF_COMMON_FONT_STYLE_TEXT -> {
                    val quoteFontStyle =
                        preferences[stringPreferencesKey(PREF_COMMON_FONT_STYLE_TEXT)]?.let {
                            TextFontStyle.fromByteString(it)
                        } ?: PREF_COMMON_FONT_STYLE_TEXT_DEFAULT
                    _quoteStyleFlow.update { currentState ->
                        currentState.copy(quoteFontStyle = quoteFontStyle)
                    }
                }

                PREF_COMMON_FONT_STYLE_SOURCE -> {
                    val sourceFontStyle =
                        preferences[stringPreferencesKey(PREF_COMMON_FONT_STYLE_SOURCE)]?.let {
                            TextFontStyle.fromByteString(it)
                        } ?: PREF_COMMON_FONT_STYLE_SOURCE_DEFAULT
                    _quoteStyleFlow.update { currentState ->
                        currentState.copy(sourceFontStyle = sourceFontStyle)
                    }
                }

                PREF_COMMON_QUOTE_SPACING -> _quoteStyleFlow.update { currentState ->
                    currentState.copy(
                        quoteSpacing = (preferences[stringPreferencesKey(PREF_COMMON_QUOTE_SPACING)]
                            ?: PREF_COMMON_QUOTE_SPACING_DEFAULT).toInt()
                    )
                }

                PREF_COMMON_PADDING_TOP -> _quoteStyleFlow.update { currentState ->
                    currentState.copy(
                        paddingTop = (preferences[stringPreferencesKey(PREF_COMMON_PADDING_TOP)]
                            ?: PREF_COMMON_PADDING_TOP_DEFAULT).toInt()
                    )
                }

                PREF_COMMON_PADDING_BOTTOM -> _quoteStyleFlow.update { currentState ->
                    currentState.copy(
                        paddingBottom = (preferences[stringPreferencesKey(PREF_COMMON_PADDING_BOTTOM)]
                            ?: PREF_COMMON_PADDING_BOTTOM_DEFAULT).toInt()
                    )
                }

                PREF_COMMON_QUOTE_MODULE -> {
                    _quoteConfigsFlow.update { currentState ->
                        currentState.copy(
                            module = preferences[stringPreferencesKey(PREF_COMMON_QUOTE_MODULE)]
                                ?: PREF_COMMON_QUOTE_MODULE_DEFAULT
                        )
                    }
                    _quoteModuleNotifyFlow.emit(Unit)
                }

                PREF_COMMON_REFRESH_RATE -> {
                    _quoteConfigsFlow.update { currentState ->
                        currentState.copy(
                            refreshRate = (preferences[stringPreferencesKey(PREF_COMMON_REFRESH_RATE)]
                                ?: PREF_COMMON_REFRESH_RATE_DEFAULT).toInt()
                        )
                    }
                    _quoteRefreshRateNotifyFlow.emit(Unit)
                }

                PREF_COMMON_REFRESH_RATE_OVERRIDE -> {
                    _quoteConfigsFlow.update { currentState ->
                        currentState.copy(
                            refreshRateOverride = preferences[intPreferencesKey(
                                PREF_COMMON_REFRESH_RATE_OVERRIDE
                            )]
                        )
                    }
                    _quoteRefreshRateNotifyFlow.emit(Unit)
                }

                PREF_COMMON_DISPLAY_ON_AOD -> _quoteConfigsFlow.update { currentState ->
                    currentState.copy(
                        displayOnAod = preferences[booleanPreferencesKey(
                            PREF_COMMON_DISPLAY_ON_AOD
                        )] ?: false
                    )
                }

                PREF_COMMON_UNMETERED_ONLY -> _quoteConfigsFlow.update { currentState ->
                    currentState.copy(
                        unmeteredOnly = preferences[booleanPreferencesKey(PREF_COMMON_UNMETERED_ONLY)]
                            ?: PREF_COMMON_UNMETERED_ONLY_DEFAULT
                    )
                }

                else -> {}
            }
        }
    }

    fun updateConfiguration(moduleData: QuoteModuleData) {
        if (moduleData.minimumRefreshInterval != 0) {
            commonDataStore.put(
                PREF_COMMON_REFRESH_RATE_OVERRIDE,
                moduleData.minimumRefreshInterval
            )
        } else {
            commonDataStore.remove(PREF_COMMON_REFRESH_RATE_OVERRIDE)
        }

        isRequireInternet = moduleData.requiresInternetConnectivity
    }
}