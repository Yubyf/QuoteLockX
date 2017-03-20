package com.crossbowffs.quotelock.consts;

import com.crossbowffs.quotelock.modules.vnaas.VnaasQuoteModule;

public final class PrefKeys {
    public static final String PREF_COMMON = "common";
    public static final String PREF_COMMON_REFRESH_RATE = "pref_common_refresh_rate";
    public static final String PREF_COMMON_REFRESH_RATE_DEFAULT = "900";
    public static final String PREF_COMMON_UNMETERED_ONLY = "pref_common_unmetered_only";
    public static final boolean PREF_COMMON_UNMETERED_ONLY_DEFAULT = false;
    public static final String PREF_COMMON_QUOTE_MODULE = "pref_common_quote_module";
    public static final String PREF_COMMON_QUOTE_MODULE_DEFAULT = VnaasQuoteModule.class.getName();
    public static final String PREF_COMMON_MODULE_PREFERENCES = "pref_module_preferences";
    public static final String PREF_COMMON_REQUIRES_INTERNET = "pref_common_requires_internet";
    public static final String PREF_COMMON_REFRESH_RATE_OVERRIDE = "pref_common_refresh_rate_override";
    public static final String PREF_COMMON_FONT_SIZE_TEXT = "pref_common_font_size_text";
    public static final String PREF_COMMON_FONT_SIZE_TEXT_DEFAULT = "20";
    public static final String PREF_COMMON_FONT_SIZE_SOURCE = "pref_common_font_size_source";
    public static final String PREF_COMMON_FONT_SIZE_SOURCE_DEFAULT = "18";

    public static final String PREF_QUOTES = "quotes";
    public static final String PREF_QUOTES_TEXT = "pref_quotes_text";
    public static final String PREF_QUOTES_SOURCE = "pref_quotes_source";
    public static final String PREF_QUOTES_LAST_UPDATED = "pref_quotes_last_updated";
    public static final String PREF_QUOTES_BRAINY_TYPE_INT = "pref_quotes_brainy_type_int";
    public static final String PREF_QUOTES_BRAINY_TYPE_STRING = "pref_quotes_brainy_type_string";

    public static final String PREF_ABOUT_AUTHOR_CROSSBOWFFS = "pref_about_author_crossbowffs";
    public static final String PREF_ABOUT_AUTHOR_YUBYF = "pref_about_author_yubyf";
    public static final String PREF_ABOUT_GITHUB = "pref_about_github";
    public static final String PREF_ABOUT_VERSION = "pref_about_version";

    private PrefKeys() { }
}
