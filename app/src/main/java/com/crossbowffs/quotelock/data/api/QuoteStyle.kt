package com.crossbowffs.quotelock.data.api

import android.graphics.Typeface
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

data class QuoteStyle(
    val quoteSize: Int,
    val sourceSize: Int,
    // Font properties
    val quoteTypeface: Typeface?,
    val quoteFontWeight: FontWeight,
    val quoteFontStyle: FontStyle,
    val sourceTypeface: Typeface?,
    val sourceFontWeight: FontWeight,
    val sourceFontStyle: FontStyle,
    // Quote spacing
    val quoteSpacing: Int,
    // Layout padding
    val paddingTop: Int,
    val paddingBottom: Int,
)