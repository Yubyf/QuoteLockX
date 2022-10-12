package com.crossbowffs.quotelock.data.api

import android.graphics.Typeface
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.crossbowffs.quotelock.app.font.FontManager
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_CARD_PADDING_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_FONT_FAMILY_DEFAULT_SANS_SERIF
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_FONT_FAMILY_DEFAULT_SERIF
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_FONT_FAMILY_LEGACY_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_FONT_SIZE_SOURCE_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_FONT_SIZE_TEXT_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_FONT_STYLE_SOURCE_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_FONT_STYLE_TEXT_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_FONT_SUPPORTED_FEATURES_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_FONT_SUPPORTED_FEATURES_SLANT
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_FONT_SUPPORTED_FEATURES_WEIGHT
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_LINE_SPACING_DEFAULT
import com.crossbowffs.quotelock.utils.decodeHex
import com.crossbowffs.quotelock.utils.hexString
import java.nio.ByteBuffer

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

data class TextFontStyle(
    val family: String = PREF_CARD_STYLE_FONT_FAMILY_DEFAULT_SANS_SERIF,
    val supportedFeatures: Int = PREF_CARD_STYLE_FONT_SUPPORTED_FEATURES_DEFAULT,
    val weight: FontWeight,
    val minWeight: FontWeight = FontWeight.Normal,
    val maxWeight: FontWeight = FontWeight.Normal,
    val italic: Float,
    val minSlant: Float = 0f,
    val maxSlant: Float = 0f,
) {

    val supportVariableWeight: Boolean
        get() = supportedFeatures and PREF_CARD_STYLE_FONT_SUPPORTED_FEATURES_WEIGHT != 0

    val supportVariableSlant: Boolean
        get() = supportedFeatures and PREF_CARD_STYLE_FONT_SUPPORTED_FEATURES_SLANT != 0

    val typeface: Typeface?
        get() = when (family) {
            PREF_CARD_STYLE_FONT_FAMILY_LEGACY_DEFAULT,
            PREF_CARD_STYLE_FONT_FAMILY_DEFAULT_SANS_SERIF,
            -> Typeface.SANS_SERIF

            PREF_CARD_STYLE_FONT_FAMILY_DEFAULT_SERIF,
            -> Typeface.SERIF

            else -> runCatching {
                FontManager.loadTypeface(
                    family,
                    if (supportVariableWeight) weight else FontWeight.Normal,
                    if (supportVariableSlant) FontStyle.Normal.value.toFloat() else italic,
                    if (supportVariableSlant) italic else 0F
                )
            }.getOrNull()
        }

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