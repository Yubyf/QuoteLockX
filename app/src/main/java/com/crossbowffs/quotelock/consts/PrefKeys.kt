package com.crossbowffs.quotelock.consts

import com.crossbowffs.quotelock.data.modules.hitokoto.HitokotoQuoteModule
import com.yubyf.quotelockx.BuildConfig

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
const val PREF_COMMON_FONT_STYLE_TEXT = "pref_common_font_style_text"
const val PREF_COMMON_FONT_STYLE_SOURCE = "pref_common_font_style_source"
const val PREF_COMMON_FONT_FAMILY = "pref_common_font_family"
const val PREF_COMMON_FONT_FAMILY_LEGACY_DEFAULT = "system"
const val PREF_COMMON_FONT_FAMILY_DEFAULT_SANS_SERIF = "sans-serif"
const val PREF_COMMON_FONT_FAMILY_DEFAULT_SERIF = "serif"
const val PREF_COMMON_QUOTE_SPACING = "pref_common_quote_spacing"
const val PREF_COMMON_QUOTE_SPACING_DEFAULT = "0"
const val PREF_COMMON_PADDING_TOP = "pref_common_padding_top"
const val PREF_COMMON_PADDING_TOP_DEFAULT = "8"
const val PREF_COMMON_PADDING_BOTTOM = "pref_common_padding_bottom"
const val PREF_COMMON_PADDING_BOTTOM_DEFAULT = "8"
const val PREF_QUOTES = "quotes"
const val PREF_QUOTES_TEXT = "pref_quotes_text"
const val PREF_QUOTES_SOURCE = "pref_quotes_source"
const val PREF_QUOTES_AUTHOR = "pref_quotes_author"
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
const val PREF_CARD_STYLE_FONT_FAMILY = "pref_card_style_font_family"
const val PREF_CARD_STYLE_FONT_FAMILY_LEGACY_DEFAULT = "system"
const val PREF_CARD_STYLE_FONT_FAMILY_DEFAULT_SANS_SERIF = "sans-serif"
const val PREF_CARD_STYLE_FONT_FAMILY_DEFAULT_SERIF = "serif"
const val PREF_CARD_STYLE_SHARE_WATERMARK = "pref_card_style_share_watermark"
const val PREF_CARD_STYLE_SHARE_WATERMARK_DEFAULT = true

const val PREF_QUOTE_SOURCE_PREFIX = "―"

const val PREF_QUOTE_CARD_ELEVATION_DP = 10

const val PREF_SHARE_FILE_AUTHORITY = "${BuildConfig.APPLICATION_ID}.fileprovider"
const val PREF_SHARE_IMAGE_FRAME_WIDTH = 36F
const val PREF_SHARE_IMAGE_EXTENSION = ".png"
const val PREF_SHARE_IMAGE_CHILD_PATH = "share/"
const val PREF_SHARE_IMAGE_MIME_TYPE = "image/png"
const val PREF_SHARE_IMAGE_WATERMARK_TEXT_SIZE_PX = 36F
