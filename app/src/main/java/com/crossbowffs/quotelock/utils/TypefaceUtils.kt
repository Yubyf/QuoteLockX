@file:JvmName("TypefaceUtils")
@file:OptIn(ExperimentalTextApi::class)

package com.crossbowffs.quotelock.utils

import android.graphics.Typeface
import android.graphics.fonts.FontVariationAxis
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.crossbowffs.quotelock.consts.PREF_COMMON_FONT_FAMILY_DEFAULT_SANS_SERIF
import com.crossbowffs.quotelock.consts.PREF_COMMON_FONT_FAMILY_DEFAULT_SERIF
import com.crossbowffs.quotelock.consts.PREF_COMMON_FONT_FAMILY_LEGACY_DEFAULT
import java.io.File

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

@RequiresApi(Build.VERSION_CODES.O)
fun getFontVariationSettings(
    style: Int = Typeface.NORMAL,
): Array<FontVariationAxis> = arrayOf(
    FontVariationAxis("wght",
        when (style) {
            Typeface.BOLD,
            Typeface.BOLD_ITALIC,
            -> 700f
            Typeface.NORMAL -> 400f
            else -> 400f
        }
    ),
    FontVariationAxis("ital",
        when (style) {
            Typeface.ITALIC,
            Typeface.BOLD_ITALIC,
            -> 1f
            else -> 0f
        }
    )
)

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

fun loadComposeFont(
    fontPath: String,
    weight: FontWeight = FontWeight.Normal,
    style: FontStyle = FontStyle.Normal,
): FontFamily = runCatching {
    FontFamily(Font(File(fontPath), weight, style))
}.onFailure {
    Xlog.e("FontLoader", "Failed to load compose font: $fontPath", it)
}.getOrDefault(FontFamily.Default)

fun loadComposeFontWithSystem(
    fontPath: String,
    weight: FontWeight = FontWeight.Normal,
    style: FontStyle = FontStyle.Normal,
): FontFamily = when (fontPath) {
    PREF_COMMON_FONT_FAMILY_LEGACY_DEFAULT,
    PREF_COMMON_FONT_FAMILY_DEFAULT_SANS_SERIF,
    -> FontFamily.SansSerif
    PREF_COMMON_FONT_FAMILY_DEFAULT_SERIF,
    -> FontFamily.Serif
    else -> loadComposeFont(fontPath, weight, style)
}