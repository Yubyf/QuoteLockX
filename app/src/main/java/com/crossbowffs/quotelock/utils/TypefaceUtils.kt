@file:JvmName("TypefaceUtils")

package com.crossbowffs.quotelock.utils

import android.graphics.Typeface
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

fun getTypefaceStyle(styles: Set<String>?): Int {
    var style = Typeface.NORMAL
    if (styles != null) {
        if (styles.contains("bold") && styles.contains("italic")) {
            style = Typeface.BOLD_ITALIC
        } else if (styles.contains("bold")) {
            style = Typeface.BOLD
        } else if (styles.contains("italic")) {
            style = Typeface.ITALIC
        }
    }
    return style
}

fun getComposeFontStyle(styles: Set<String>?): FontStyle {
    return if (styles != null && styles.contains("italic")) {
        FontStyle.Italic
    } else {
        FontStyle.Normal
    }
}

fun getComposeFontWeight(styles: Set<String>?): FontWeight {
    return if (styles != null && styles.contains("bold")) {
        FontWeight.Bold
    } else {
        FontWeight.Normal
    }
}