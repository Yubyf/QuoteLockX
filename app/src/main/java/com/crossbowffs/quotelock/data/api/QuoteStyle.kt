package com.crossbowffs.quotelock.data.api

import android.graphics.Typeface

data class QuoteStyle(
    val quoteSize: Float,
    val sourceSize: Float,
    val quoteTypeface: Typeface?,
    val quoteStyle: Int,
    val sourceTypeface: Typeface?,
    val sourceStyle: Int,
    val quoteSpacing: Int,
    val paddingTop: Int,
    val paddingBottom: Int,
)