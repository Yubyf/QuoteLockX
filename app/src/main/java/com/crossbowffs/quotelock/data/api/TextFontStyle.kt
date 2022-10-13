package com.crossbowffs.quotelock.data.api

import android.graphics.Typeface
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.crossbowffs.quotelock.app.font.FontInfo
import com.crossbowffs.quotelock.app.font.FontManager
import com.crossbowffs.quotelock.consts.PREF_COMMON_FONT_FAMILY_DEFAULT_SANS_SERIF
import com.crossbowffs.quotelock.consts.PREF_COMMON_FONT_FAMILY_DEFAULT_SERIF
import com.crossbowffs.quotelock.consts.PREF_COMMON_FONT_FAMILY_LEGACY_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_COMMON_FONT_ITALIC_TEXT_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_COMMON_FONT_SUPPORTED_FEATURES_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_COMMON_FONT_SUPPORTED_FEATURES_SLANT
import com.crossbowffs.quotelock.consts.PREF_COMMON_FONT_SUPPORTED_FEATURES_WEIGHT
import com.crossbowffs.quotelock.consts.PREF_COMMON_FONT_WEIGHT_TEXT_DEFAULT
import com.crossbowffs.quotelock.utils.decodeHex
import com.crossbowffs.quotelock.utils.hexString
import com.crossbowffs.quotelock.utils.loadComposeFontWithSystem
import java.nio.ByteBuffer
import kotlin.math.roundToInt

data class TextFontStyle(
    val family: String = PREF_COMMON_FONT_FAMILY_DEFAULT_SANS_SERIF,
    val supportedFeatures: Int = PREF_COMMON_FONT_SUPPORTED_FEATURES_DEFAULT,
    val weight: FontWeight = PREF_COMMON_FONT_WEIGHT_TEXT_DEFAULT,
    val minWeight: FontWeight = FontWeight.Normal,
    val maxWeight: FontWeight = FontWeight.Normal,
    val italic: Float = PREF_COMMON_FONT_ITALIC_TEXT_DEFAULT,
    val minSlant: Float = 0f,
    val maxSlant: Float = 0f,
) {

    val supportVariableWeight: Boolean
        get() = supportedFeatures and PREF_COMMON_FONT_SUPPORTED_FEATURES_WEIGHT != 0

    val supportVariableSlant: Boolean
        get() = supportedFeatures and PREF_COMMON_FONT_SUPPORTED_FEATURES_SLANT != 0

    val isNonVariableWeightBold: Boolean
        get() = !supportVariableWeight && weight == FontWeight.Bold

    val isNonVariableSlantItalic: Boolean
        get() = !supportVariableSlant && italic.roundToInt() == FontStyle.Italic.value

    val composeFontStyle: FontStyle
        get() = if (supportVariableSlant) FontStyle.Normal else FontStyle(italic.roundToInt())

    val byteString: String
        get() {
            val familyBytes = family.toByteArray()
            val bufferSize =
                familyBytes.size + Int.SIZE_BYTES + Int.SIZE_BYTES * 3 + Float.SIZE_BYTES * 3
            val byteBuffer = ByteBuffer.allocate(bufferSize)
                .put(familyBytes)
                .putInt(supportedFeatures)
                .putInt(weight.weight)
                .putInt(minWeight.weight)
                .putInt(maxWeight.weight)
                .putFloat(italic)
                .putFloat(minSlant)
                .putFloat(maxSlant)
            return byteBuffer.array().hexString()
        }

    fun migrateTo(fontInfo: FontInfo): TextFontStyle {
        val supportedFeatures = (if (!fontInfo.supportVariableWeight) 0
        else PREF_COMMON_FONT_SUPPORTED_FEATURES_WEIGHT) or (if (!fontInfo.supportVariableSlant) 0
        else PREF_COMMON_FONT_SUPPORTED_FEATURES_SLANT)

        val minWeight = FontWeight(
            fontInfo.variableWeight?.range?.start ?: FontWeight.Normal.weight
        )
        val maxWeight = FontWeight(
            fontInfo.variableWeight?.range?.endInclusive ?: FontWeight.Normal.weight
        )
        val minSlant = fontInfo.variableSlant?.range?.start ?: 0f
        val maxSlant = fontInfo.variableSlant?.range?.endInclusive ?: 0f
        val newWeight = if (fontInfo.supportVariableWeight) {
            when {
                weight < minWeight -> minWeight
                weight > maxWeight -> maxWeight
                else -> weight
            }
        } else if (weight >= FontWeight.Bold) FontWeight.Bold else FontWeight.Normal
        val newItalic = if (fontInfo.supportVariableSlant) {
            if (!supportVariableSlant) {
                if (italic.roundToInt() != FontStyle.Italic.value) {
                    fontInfo.variableSlant?.default ?: 0F
                } else minSlant
            } else {
                when {
                    italic < minSlant -> minSlant
                    italic > maxSlant -> maxSlant
                    else -> italic
                }
            }
        } else {
            if (!supportVariableSlant) {
                if (italic.roundToInt() != FontStyle.Italic.value) {
                    FontStyle.Normal.value.toFloat()
                } else {
                    FontStyle.Italic.value.toFloat()
                }
            } else {
                if (italic.roundToInt() == 0) {
                    FontStyle.Normal.value.toFloat()
                } else {
                    FontStyle.Italic.value.toFloat()
                }
            }
        }
        return TextFontStyle(
            family = fontInfo.path,
            supportedFeatures = supportedFeatures,
            weight = newWeight,
            minWeight = minWeight,
            maxWeight = maxWeight,
            italic = newItalic,
            minSlant = minSlant,
            maxSlant = maxSlant
        )
    }

    companion object {
        fun fromByteString(byteString: String): TextFontStyle {
            return byteString.decodeHex().let {
                val buffer = ByteBuffer.wrap(it)
                val familyBytes =
                    ByteArray(it.size - Int.SIZE_BYTES - Int.SIZE_BYTES * 3 - Float.SIZE_BYTES * 3)
                buffer.get(familyBytes)
                val fontFamily = String(familyBytes)
                val fontSupportedFeatures = buffer.int
                val fontWeight = FontWeight(buffer.int)
                val minFontWeight = FontWeight(buffer.int)
                val maxFontWeight = FontWeight(buffer.int)
                val fontItalic = buffer.float
                val minFontSlant = buffer.float
                val maxFontSlant = buffer.float
                TextFontStyle(
                    fontFamily, fontSupportedFeatures, fontWeight, minFontWeight,
                    maxFontWeight, fontItalic, minFontSlant, maxFontSlant
                )
            }
        }
    }
}

val TextFontStyle.typeface: Typeface?
    get() = when (family) {
        PREF_COMMON_FONT_FAMILY_LEGACY_DEFAULT,
        PREF_COMMON_FONT_FAMILY_DEFAULT_SANS_SERIF,
        -> Typeface.SANS_SERIF

        PREF_COMMON_FONT_FAMILY_DEFAULT_SERIF,
        -> Typeface.SERIF

        else -> runCatching {
            FontManager.loadTypeface(
                family,
                if (supportVariableWeight) weight else FontWeight.Normal,
                if (supportVariableSlant) FontStyle.Italic.value.toFloat()
                else FontStyle.Normal.value.toFloat(),
                if (supportVariableSlant) italic else Float.NaN
            )
        }.getOrNull()
    }

val TextFontStyle.typefaceStyle: Int
    get() = when {
        isNonVariableWeightBold && isNonVariableSlantItalic -> Typeface.BOLD_ITALIC
        isNonVariableWeightBold -> Typeface.BOLD
        isNonVariableSlantItalic -> Typeface.ITALIC
        else -> Typeface.NORMAL
    }

val TextFontStyle.composeFontFamily: FontFamily
    get() = loadComposeFontWithSystem(
        family,
        if (supportVariableWeight) weight else FontWeight.Normal,
        if (supportVariableSlant) FontStyle.Italic else FontStyle.Normal,
        if (supportVariableSlant) italic else Float.NaN
    )