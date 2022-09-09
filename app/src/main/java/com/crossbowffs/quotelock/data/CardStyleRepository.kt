package com.crossbowffs.quotelock.data

import androidx.datastore.preferences.core.Preferences
import com.crossbowffs.quotelock.consts.*
import com.crossbowffs.quotelock.data.api.CardStyle
import com.crossbowffs.quotelock.di.CardStyleDataStore
import com.crossbowffs.quotelock.utils.getValueByDefault
import com.yubyf.datastore.DataStoreDelegate
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Singleton
class CardStyleRepository @Inject internal constructor(
    @CardStyleDataStore private val cardStyleDataStore: DataStoreDelegate,
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
        PREF_CARD_STYLE_FONT_FAMILY_DEFAULT)

    val cardStyle: CardStyle
        get() = CardStyle(
            quoteSize = quoteSize,
            sourceSize = sourceSize,
            fontFamily = fontFamily,
            lineSpacing = lineSpacing,
            cardPadding = cardPadding
        )

    suspend fun observeCardStyleDataStore(collector: suspend (Preferences, Preferences.Key<*>?) -> Unit) =
        cardStyleDataStore.collectSuspend(collector)
}