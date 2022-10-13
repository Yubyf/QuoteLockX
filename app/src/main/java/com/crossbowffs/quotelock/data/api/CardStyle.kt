package com.crossbowffs.quotelock.data.api

import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_CARD_PADDING_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_FONT_SIZE_SOURCE_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_FONT_SIZE_TEXT_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_FONT_STYLE_SOURCE_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_FONT_STYLE_TEXT_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_LINE_SPACING_DEFAULT

data class CardStyle(
    val quoteSize: Int = PREF_CARD_STYLE_FONT_SIZE_TEXT_DEFAULT,
    val sourceSize: Int = PREF_CARD_STYLE_FONT_SIZE_SOURCE_DEFAULT,
    val lineSpacing: Int = PREF_CARD_STYLE_LINE_SPACING_DEFAULT,
    val cardPadding: Int = PREF_CARD_STYLE_CARD_PADDING_DEFAULT,
    val quoteFontStyle: TextFontStyle = PREF_CARD_STYLE_FONT_STYLE_TEXT_DEFAULT,
    val sourceFontStyle: TextFontStyle = PREF_CARD_STYLE_FONT_STYLE_SOURCE_DEFAULT,
) {
    val fontFamily: String
        get() = quoteFontStyle.family
}