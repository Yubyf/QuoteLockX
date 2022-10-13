@file:JvmName("TypefaceUtils")
@file:OptIn(ExperimentalTextApi::class)

package com.crossbowffs.quotelock.utils

import android.graphics.fonts.FontVariationAxis
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.crossbowffs.quotelock.consts.PREF_COMMON_FONT_FAMILY_DEFAULT_SANS_SERIF
import com.crossbowffs.quotelock.consts.PREF_COMMON_FONT_FAMILY_DEFAULT_SERIF
import com.crossbowffs.quotelock.consts.PREF_COMMON_FONT_FAMILY_LEGACY_DEFAULT
import java.io.File

@RequiresApi(Build.VERSION_CODES.O)
fun getFontVariationSettings(
    weight: FontWeight = FontWeight.Normal,
    italic: Float = FontStyle.Normal.value.toFloat(),
    slant: Float = Float.NaN,
): Array<FontVariationAxis> = arrayOf(
    FontVariationAxis("wght", weight.weight.toFloat()),
    if (slant.isNaN()) FontVariationAxis("ital", italic)
    else FontVariationAxis("slnt", slant)
)

fun loadComposeFont(
    fontPath: String,
    weight: FontWeight = FontWeight.Normal,
    style: FontStyle = FontStyle.Normal,
    slant: Float = Float.NaN,
): FontFamily = runCatching {
    if (!File(fontPath).exists()) throw Exception("Font file not found")
    val settings =
        if (!slant.isNaN()) arrayOf(FontVariation.Setting("slnt", slant)) else emptyArray()
    FontFamily(
        Font(
            File(fontPath),
            weight,
            FontStyle.Normal,
            FontVariation.Settings(weight, style, *settings)
        )
    )
}.onFailure {
    Xlog.e("FontLoader", "Failed to load compose font: $fontPath", it)
}.getOrDefault(FontFamily.Default)

fun loadComposeFontWithSystem(
    fontPath: String,
    weight: FontWeight = FontWeight.Normal,
    style: FontStyle = FontStyle.Normal,
    slant: Float = Float.NaN,
): FontFamily = when (fontPath) {
    PREF_COMMON_FONT_FAMILY_LEGACY_DEFAULT,
    PREF_COMMON_FONT_FAMILY_DEFAULT_SANS_SERIF,
    -> FontFamily.SansSerif

    PREF_COMMON_FONT_FAMILY_DEFAULT_SERIF,
    -> FontFamily.Serif

    else -> loadComposeFont(fontPath, weight, style, slant)
}