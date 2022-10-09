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
    val italic: Float,
) {

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
                    if (supportedFeatures and PREF_CARD_STYLE_FONT_SUPPORTED_FEATURES_WEIGHT != 0)
                        weight else FontWeight.Normal,
                    if (supportedFeatures and PREF_CARD_STYLE_FONT_SUPPORTED_FEATURES_SLANT != 0)
                        FontStyle.Normal.value.toFloat() else italic,
                    if (supportedFeatures and PREF_CARD_STYLE_FONT_SUPPORTED_FEATURES_SLANT != 0)
                        italic else 0F
                )
            }.getOrNull()
        }

    val byteString: String
        get() {
            val familyBytes = family.toByteArray()
            val bufferSize =
                Int.SIZE_BYTES + familyBytes.size + Int.SIZE_BYTES + Float.SIZE_BYTES
            val byteBuffer = ByteBuffer.allocate(bufferSize)
                .put(familyBytes)
                .putInt(supportedFeatures)
                .putInt(weight.weight)
                .putFloat(italic)
            return byteBuffer.array().hexString()
        }

    companion object {
        fun fromByteString(byteString: String): TextFontStyle {
            return byteString.decodeHex().let {
                val buffer = ByteBuffer.wrap(it)
                val familyBytes =
                    ByteArray(it.size - Int.SIZE_BYTES - Int.SIZE_BYTES - Float.SIZE_BYTES)
                buffer.get(familyBytes)
                val fontFamily = String(familyBytes)
                val fontSupportedFeatures = buffer.int
                val fontWeight = FontWeight(buffer.int)
                val fontItalic = buffer.float
                TextFontStyle(fontFamily, fontSupportedFeatures, fontWeight, fontItalic)
            }
        }
    }
}