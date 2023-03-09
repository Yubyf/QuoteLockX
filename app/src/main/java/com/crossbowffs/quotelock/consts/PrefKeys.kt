package com.crossbowffs.quotelock.consts

import android.content.res.Configuration
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.crossbowffs.quotelock.app.App
import com.crossbowffs.quotelock.data.api.TextFontStyle
import com.crossbowffs.quotelock.data.modules.hitokoto.HitokotoQuoteModule
import com.yubyf.quotelockx.BuildConfig
import com.yubyf.quotelockx.R
import java.util.Locale

const val PREF_COMMON = "common"
const val PREF_COMMON_DISPLAY_ON_AOD = "pref_common_display_on_aod"
const val PREF_COMMON_REFRESH_RATE = "pref_common_refresh_rate"
const val PREF_COMMON_REFRESH_RATE_DEFAULT = "900"
const val PREF_COMMON_UNMETERED_ONLY = "pref_common_unmetered_only"
const val PREF_COMMON_UNMETERED_ONLY_DEFAULT = false
const val PREF_COMMON_QUOTE_MODULE = "pref_common_quote_module"

val PREF_COMMON_QUOTE_MODULE_DEFAULT = HitokotoQuoteModule::class.qualifiedName ?: ""
const val PREF_COMMON_REQUIRES_INTERNET = "pref_common_requires_internet"
const val PREF_COMMON_REFRESH_RATE_OVERRIDE = "pref_common_refresh_rate_override"
const val PREF_COMMON_FONT_SIZE_TEXT = "pref_common_font_size_text"
const val PREF_COMMON_FONT_SIZE_TEXT_DEFAULT = "20"
const val PREF_COMMON_FONT_SIZE_SOURCE = "pref_common_font_size_source"
const val PREF_COMMON_FONT_SIZE_SOURCE_DEFAULT = "18"
const val PREF_COMMON_FONT_STYLE_TEXT = "pref_common_font_style_group_text"
const val PREF_COMMON_FONT_STYLE_SOURCE = "pref_common_font_style_group_source"

@Deprecated("Use PREF_COMMON_FONT_STYLE_TEXT and PREF_COMMON_FONT_STYLE_SOURCE instead")
const val PREF_COMMON_FONT_LEGACY_FAMILY = "pref_common_font_family"

@Deprecated("Use PREF_COMMON_FONT_FAMILY_DEFAULT_SANS_SERIF and PREF_COMMON_FONT_FAMILY_DEFAULT_SERIF instead")
const val PREF_COMMON_FONT_FAMILY_LEGACY_DEFAULT = "system"
const val PREF_COMMON_FONT_FAMILY_DEFAULT_SANS_SERIF = "sans-serif"
const val PREF_COMMON_FONT_FAMILY_DEFAULT_SERIF = "serif"
const val PREF_COMMON_FONT_SUPPORTED_FEATURES_DEFAULT = 0
const val PREF_COMMON_FONT_SUPPORTED_FEATURES_WEIGHT = 0x0001
const val PREF_COMMON_FONT_SUPPORTED_FEATURES_SLANT = 0x0010
val PREF_COMMON_FONT_WEIGHT_TEXT_DEFAULT = FontWeight.Normal
val PREF_COMMON_FONT_ITALIC_TEXT_DEFAULT = FontStyle.Normal.value.toFloat()
val PREF_COMMON_FONT_WEIGHT_SOURCE_DEFAULT = FontWeight.Normal
val PREF_COMMON_FONT_ITALIC_SOURCE_DEFAULT = FontStyle.Normal.value.toFloat()
val PREF_COMMON_FONT_STYLE_TEXT_DEFAULT = TextFontStyle(
    weight = PREF_COMMON_FONT_WEIGHT_TEXT_DEFAULT,
    italic = PREF_COMMON_FONT_ITALIC_TEXT_DEFAULT
)
val PREF_COMMON_FONT_STYLE_SOURCE_DEFAULT = TextFontStyle(
    weight = PREF_COMMON_FONT_WEIGHT_SOURCE_DEFAULT,
    italic = PREF_COMMON_FONT_ITALIC_SOURCE_DEFAULT
)
const val PREF_COMMON_QUOTE_SPACING = "pref_common_quote_spacing"
const val PREF_COMMON_QUOTE_SPACING_DEFAULT = "0"
const val PREF_COMMON_PADDING_TOP = "pref_common_padding_top"
const val PREF_COMMON_PADDING_TOP_DEFAULT = "8"
const val PREF_COMMON_PADDING_BOTTOM = "pref_common_padding_bottom"
const val PREF_COMMON_PADDING_BOTTOM_DEFAULT = "8"
const val PREF_QUOTES = "quotes"
const val PREF_QUOTES_CONTENTS = "pref_quotes_contents"
const val PREF_QUOTES_COLLECTION_STATE = "pref_quotes_collection_state"
const val PREF_QUOTES_LAST_UPDATED = "pref_quotes_last_updated"
const val PREF_BOOT_NOTIFY_FLAG = "boot_notify_flag"

const val PREF_CARD_STYLE = "card_style"
const val PREF_CARD_STYLE_FONT_SIZE_TEXT = "pref_card_style_font_size_text"
const val PREF_CARD_STYLE_FONT_SIZE_TEXT_DEFAULT = 36
const val PREF_CARD_STYLE_FONT_SIZE_TEXT_MIN = 24
const val PREF_CARD_STYLE_FONT_SIZE_TEXT_MAX = 48
const val PREF_CARD_STYLE_FONT_SIZE_TEXT_STEP = 4
const val PREF_CARD_STYLE_FONT_SIZE_SOURCE = "pref_card_style_font_size_source"
const val PREF_CARD_STYLE_FONT_SIZE_SOURCE_DEFAULT = 16
const val PREF_CARD_STYLE_FONT_SIZE_SOURCE_MIN = 8
const val PREF_CARD_STYLE_FONT_SIZE_SOURCE_MAX = 24
const val PREF_CARD_STYLE_FONT_SIZE_SOURCE_STEP = 4
const val PREF_CARD_STYLE_LINE_SPACING = "pref_card_style_line_spacing"
const val PREF_CARD_STYLE_LINE_SPACING_DEFAULT = 24
const val PREF_CARD_STYLE_LINE_SPACING_MIN = 16
const val PREF_CARD_STYLE_LINE_SPACING_MAX = 32
const val PREF_CARD_STYLE_LINE_SPACING_STEP = 4
const val PREF_CARD_STYLE_CARD_PADDING = "pref_card_style_card_padding"
const val PREF_CARD_STYLE_CARD_PADDING_DEFAULT = 24
const val PREF_CARD_STYLE_CARD_PADDING_MIN = 16
const val PREF_CARD_STYLE_CARD_PADDING_MAX = 32
const val PREF_CARD_STYLE_CARD_PADDING_STEP = 4

@Deprecated("Use PREF_CARD_STYLE_FONT_STYLE_TEXT and PREF_CARD_STYLE_FONT_STYLE_SOURCE instead")
const val PREF_CARD_STYLE_FONT_LEGACY_FAMILY = "pref_card_style_font_family"
const val PREF_CARD_STYLE_FONT_STYLE_TEXT = "pref_card_style_font_style_group_text"
const val PREF_CARD_STYLE_FONT_STYLE_SOURCE = "pref_card_style_font_style_group_source"
val PREF_CARD_STYLE_FONT_WEIGHT_TEXT_DEFAULT = FontWeight.Normal
val PREF_CARD_STYLE_FONT_ITALIC_TEXT_DEFAULT = FontStyle.Normal.value.toFloat()
val PREF_CARD_STYLE_FONT_WEIGHT_SOURCE_DEFAULT = FontWeight.Normal
val PREF_CARD_STYLE_FONT_ITALIC_SOURCE_DEFAULT = FontStyle.Normal.value.toFloat()
val PREF_CARD_STYLE_FONT_STYLE_TEXT_DEFAULT = TextFontStyle(
    weight = PREF_CARD_STYLE_FONT_WEIGHT_TEXT_DEFAULT,
    italic = PREF_CARD_STYLE_FONT_ITALIC_TEXT_DEFAULT
)
val PREF_CARD_STYLE_FONT_STYLE_SOURCE_DEFAULT = TextFontStyle(
    weight = PREF_CARD_STYLE_FONT_WEIGHT_SOURCE_DEFAULT,
    italic = PREF_CARD_STYLE_FONT_ITALIC_SOURCE_DEFAULT
)

const val PREF_QUOTE_SOURCE_PREFIX = "―"

const val PREF_QUOTE_CARD_ELEVATION_DP = 4

const val PREF_SHARE_FILE_AUTHORITY = "${BuildConfig.APPLICATION_ID}.fileprovider"
const val PREF_SHARE_IMAGE_FRAME_WIDTH = 36F
const val PREF_SHARE_IMAGE_NAME_PREFIX = "quotelockx_export_"
const val PREF_SHARE_IMAGE_EXTENSION = ".png"
const val PREF_SHARE_IMAGE_CHILD_PATH = "share/"
const val PREF_SHARE_IMAGE_MIME_TYPE = "image/png"
const val PREF_SHARE_IMAGE_WATERMARK_TEXT_SIZE_PX = 36F
const val PREF_SHARE_IMAGE_WATERMARK_PADDING = PREF_SHARE_IMAGE_WATERMARK_TEXT_SIZE_PX * 2F

const val PREF_WIDGET_IMAGE_CHILD_PATH = "widget/"
const val PREF_WIDGET_IMAGE_EXTENSION = ".png"

val PREF_PUBLIC_RELATIVE_PATH by lazy {
    // Get english application name for the default export path
    App.instance.let {
        it.createConfigurationContext(Configuration(it.resources.configuration).apply {
            setLocale(Locale.ENGLISH)
        }).resources.getString(R.string.quotelockx)
    }
}
