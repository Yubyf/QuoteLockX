package com.crossbowffs.quotelock.data.api

import android.graphics.Typeface
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.crossbowffs.quotelock.consts.*

data class QuoteStyle(
    val quoteSize: Int = PREF_COMMON_FONT_SIZE_TEXT_DEFAULT.toInt(),
    val sourceSize: Int = PREF_COMMON_FONT_SIZE_SOURCE_DEFAULT.toInt(),
    // Font properties
    val quoteTypeface: Typeface? = Typeface.SANS_SERIF,
    val quoteFontWeight: FontWeight = FontWeight.Normal,
    val quoteFontStyle: FontStyle = FontStyle.Normal,
    val sourceTypeface: Typeface? = Typeface.SANS_SERIF,
    val sourceFontWeight: FontWeight = FontWeight.Normal,
    val sourceFontStyle: FontStyle = FontStyle.Normal,
    // Quote spacing
    val quoteSpacing: Int = PREF_COMMON_QUOTE_SPACING_DEFAULT.toInt(),
    // Layout padding
    val paddingTop: Int = PREF_COMMON_PADDING_TOP_DEFAULT.toInt(),
    val paddingBottom: Int = PREF_COMMON_PADDING_BOTTOM_DEFAULT.toInt(),
)