package com.crossbowffs.quotelock.data

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.crossbowffs.quotelock.consts.*
import com.crossbowffs.quotelock.data.api.CardStyle
import com.crossbowffs.quotelock.di.CardStyleDataStore
import com.crossbowffs.quotelock.di.IoDispatcher
import com.crossbowffs.quotelock.utils.getValueByDefault
import com.yubyf.datastore.DataStoreDelegate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Singleton
class CardStyleRepository @Inject internal constructor(
    @CardStyleDataStore private val cardStyleDataStore: DataStoreDelegate,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) {

    class DataStoreValue<T>(private val key: String, private val default: T) :
        ReadWriteProperty<CardStyleRepository, T> {
        override fun getValue(thisRef: CardStyleRepository, property: KProperty<*>): T {
            return runBlocking {
                thisRef.cardStyleDataStore.getValueByDefault(key, default)
            }
        }

        override fun setValue(thisRef: CardStyleRepository, property: KProperty<*>, value: T) {
            thisRef.cardStyleDataStore.put(key, value)
        }
    }

    var quoteSize: Int by DataStoreValue(PREF_CARD_STYLE_FONT_SIZE_TEXT,
        PREF_CARD_STYLE_FONT_SIZE_TEXT_DEFAULT)

    var sourceSize: Int by DataStoreValue(PREF_CARD_STYLE_FONT_SIZE_SOURCE,
        PREF_CARD_STYLE_FONT_SIZE_SOURCE_DEFAULT)

    var lineSpacing: Int by DataStoreValue(PREF_CARD_STYLE_LINE_SPACING,
        PREF_CARD_STYLE_LINE_SPACING_DEFAULT)

    var cardPadding: Int by DataStoreValue(PREF_CARD_STYLE_CARD_PADDING,
        PREF_CARD_STYLE_CARD_PADDING_DEFAULT)

    var fontFamily: String by DataStoreValue(PREF_CARD_STYLE_FONT_FAMILY,
        PREF_CARD_STYLE_FONT_FAMILY_DEFAULT_SANS_SERIF)

    var shareWatermark: Boolean by DataStoreValue(PREF_CARD_STYLE_SHARE_WATERMARK,
        PREF_CARD_STYLE_SHARE_WATERMARK_DEFAULT)

    val cardStyle: CardStyle
        get() = CardStyle(
            quoteSize = quoteSize,
            sourceSize = sourceSize,
            fontFamily = fontFamily,
            lineSpacing = lineSpacing,
            cardPadding = cardPadding,
            shareWatermark = shareWatermark,
        )

    private val _cardStyleFlow = MutableStateFlow(cardStyle)
    val cardStyleFlow = _cardStyleFlow.asStateFlow()

    init {
        cardStyleDataStore.collectIn(CoroutineScope(dispatcher)) { preferences, key ->
            when (key?.name) {
                PREF_CARD_STYLE_FONT_FAMILY -> {
                    val font =
                        preferences[stringPreferencesKey(PREF_CARD_STYLE_FONT_FAMILY)]
                            ?: PREF_CARD_STYLE_FONT_FAMILY_DEFAULT_SANS_SERIF
                    _cardStyleFlow.update { it.copy(fontFamily = font) }
                }
                PREF_CARD_STYLE_FONT_SIZE_TEXT -> {
                    val quoteSize =
                        preferences[intPreferencesKey(PREF_CARD_STYLE_FONT_SIZE_TEXT)]
                            ?: PREF_CARD_STYLE_FONT_SIZE_TEXT_DEFAULT
                    _cardStyleFlow.update { it.copy(quoteSize = quoteSize) }
                }
                PREF_CARD_STYLE_FONT_SIZE_SOURCE -> {
                    val sourceSize =
                        preferences[intPreferencesKey(PREF_CARD_STYLE_FONT_SIZE_SOURCE)]
                            ?: PREF_CARD_STYLE_FONT_SIZE_SOURCE_DEFAULT
                    _cardStyleFlow.update { it.copy(sourceSize = sourceSize) }
                }
                PREF_CARD_STYLE_LINE_SPACING -> {
                    val lineSpacing =
                        preferences[intPreferencesKey(PREF_CARD_STYLE_LINE_SPACING)]
                            ?: PREF_CARD_STYLE_LINE_SPACING_DEFAULT
                    _cardStyleFlow.update { it.copy(lineSpacing = lineSpacing) }
                }
                PREF_CARD_STYLE_CARD_PADDING -> {
                    val cardPadding =
                        preferences[intPreferencesKey(PREF_CARD_STYLE_CARD_PADDING)]
                            ?: PREF_CARD_STYLE_CARD_PADDING_DEFAULT
                    _cardStyleFlow.update { it.copy(cardPadding = cardPadding) }
                }
                PREF_CARD_STYLE_SHARE_WATERMARK -> {
                    val shareWatermark =
                        preferences[booleanPreferencesKey(PREF_CARD_STYLE_SHARE_WATERMARK)]
                            ?: PREF_CARD_STYLE_SHARE_WATERMARK_DEFAULT
                    _cardStyleFlow.update { it.copy(shareWatermark = shareWatermark) }
                }
            }
        }
    }
}