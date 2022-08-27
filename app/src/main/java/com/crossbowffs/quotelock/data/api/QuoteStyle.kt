package com.crossbowffs.quotelock.data.api

import android.graphics.Typeface

data class QuoteStyle(
    val quoteSize: Int,
    val sourceSize: Int,
    // Font properties
    val quoteTypeface: Typeface?,
    val quoteStyle: Int,
    val sourceTypeface: Typeface?,
    val sourceStyle: Int,
    // Quote spacing
    val quoteSpacing: Int,
    // Layout padding
    val paddingTop: Int,
    val paddingBottom: Int,
)