package com.crossbowffs.quotelock.data.api

import android.graphics.Typeface
import com.crossbowffs.quotelock.app.font.FontManager
import com.crossbowffs.quotelock.consts.*

data class CardStyle(
    val quoteSize: Int = PREF_CARD_STYLE_FONT_SIZE_TEXT_DEFAULT,
    val sourceSize: Int = PREF_CARD_STYLE_FONT_SIZE_SOURCE_DEFAULT,
    val fontFamily: String = PREF_CARD_STYLE_FONT_FAMILY_DEFAULT,
    val lineSpacing: Int = PREF_CARD_STYLE_LINE_SPACING_DEFAULT,
    val cardPadding: Int = PREF_CARD_STYLE_CARD_PADDING_DEFAULT,
) {

    val typeface: Typeface?
        get() = fontFamily.takeIf { PREF_COMMON_FONT_FAMILY_DEFAULT != it }?.runCatching {
            FontManager.loadTypeface(this)
        }?.getOrNull()
}