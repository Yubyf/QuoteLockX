package com.crossbowffs.quotelock.data.api

import com.crossbowffs.quotelock.consts.PREF_COMMON_FONT_SIZE_SOURCE_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_COMMON_FONT_SIZE_TEXT_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_COMMON_FONT_STYLE_SOURCE_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_COMMON_FONT_STYLE_TEXT_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_COMMON_PADDING_BOTTOM_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_COMMON_PADDING_TOP_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_COMMON_QUOTE_SPACING_DEFAULT

data class QuoteStyle(
    val quoteSize: Int = PREF_COMMON_FONT_SIZE_TEXT_DEFAULT.toInt(),
    val sourceSize: Int = PREF_COMMON_FONT_SIZE_SOURCE_DEFAULT.toInt(),
    // Font properties
    val quoteFontStyle: TextFontStyle = PREF_COMMON_FONT_STYLE_TEXT_DEFAULT,
    val sourceFontStyle: TextFontStyle = PREF_COMMON_FONT_STYLE_SOURCE_DEFAULT,
    // Quote spacing
    val quoteSpacing: Int = PREF_COMMON_QUOTE_SPACING_DEFAULT.toInt(),
    // Layout padding
    val paddingTop: Int = PREF_COMMON_PADDING_TOP_DEFAULT.toInt(),
    val paddingBottom: Int = PREF_COMMON_PADDING_BOTTOM_DEFAULT.toInt(),
)