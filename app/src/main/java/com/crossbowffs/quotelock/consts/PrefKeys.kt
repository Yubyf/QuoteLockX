@file:JvmName("PrefKeys")

package com.crossbowffs.quotelock.consts

import com.crossbowffs.quotelock.modules.hitokoto.HitokotoQuoteModule

const val PREF_COMMON = "common"
const val PREF_COMMON_DISPLAY_ON_AOD = "pref_common_display_on_aod"
const val PREF_COMMON_REFRESH_RATE = "pref_common_refresh_rate"
const val PREF_COMMON_REFRESH_RATE_DEFAULT = "900"
const val PREF_COMMON_UNMETERED_ONLY = "pref_common_unmetered_only"
const val PREF_COMMON_UNMETERED_ONLY_DEFAULT = false
const val PREF_COMMON_QUOTE_MODULE = "pref_common_quote_module"

// TODO: Remove field compatibility annotation for java
@JvmField
val PREF_COMMON_QUOTE_MODULE_DEFAULT = HitokotoQuoteModule::class.qualifiedName
const val PREF_COMMON_MODULE_PREFERENCES = "pref_module_preferences"
const val PREF_COMMON_REQUIRES_INTERNET = "pref_common_requires_internet"
const val PREF_COMMON_REFRESH_RATE_OVERRIDE = "pref_common_refresh_rate_override"
const val PREF_COMMON_FONT_SIZE_TEXT = "pref_common_font_size_text"
const val PREF_COMMON_FONT_SIZE_TEXT_DEFAULT = "20"
const val PREF_COMMON_FONT_SIZE_SOURCE = "pref_common_font_size_source"
const val PREF_COMMON_FONT_SIZE_SOURCE_DEFAULT = "18"
const val PREF_COMMON_FONT_STYLE_TEXT = "pref_common_font_style_text"
const val PREF_COMMON_FONT_STYLE_SOURCE = "pref_common_font_style_source"
const val PREF_COMMON_UPDATE_INFO = "pref_common_update_info"
const val PREF_COMMON_FONT_FAMILY = "pref_common_font_family"
const val PREF_COMMON_FONT_FAMILY_DEFAULT = "system"
const val PREF_COMMON_PADDING_TOP = "pref_common_padding_top"
const val PREF_COMMON_PADDING_TOP_DEFAULT = "8"
const val PREF_COMMON_PADDING_BOTTOM = "pref_common_padding_bottom"
const val PREF_COMMON_PADDING_BOTTOM_DEFAULT = "8"
const val PREF_QUOTES = "quotes"
const val PREF_QUOTES_TEXT = "pref_quotes_text"
const val PREF_QUOTES_SOURCE = "pref_quotes_source"
const val PREF_QUOTES_COLLECTION_STATE = "pref_quotes_collection_state"
const val PREF_QUOTES_LAST_UPDATED = "pref_quotes_last_updated"
const val PREF_FEATURES_COLLECTION = "pref_collection"
const val PREF_FEATURES_HISTORY = "pref_history"
const val PREF_ABOUT_CREDITS = "pref_about_credits"
const val PREF_ABOUT_GITHUB = "pref_about_github"
const val PREF_ABOUT_GITHUB_CURRENT = "pref_about_github_current"
const val PREF_ABOUT_VERSION = "pref_about_version"
const val PREF_BOOT_NOTIFY_FLAG = "boot_notify_flag"