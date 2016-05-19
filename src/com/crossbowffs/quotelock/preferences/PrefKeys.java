package com.crossbowffs.quotelock.preferences;

import com.crossbowffs.quotelock.modules.vnaas.VnaasQuoteModule;

public class PrefKeys {
    public static final String PREF_COMMON = "common";

    public static final String PREF_COMMON_QUOTE_MODULE = "pref_common_quote_module";
    public static final String PREF_COMMON_QUOTE_MODULE_DEFAULT = VnaasQuoteModule.class.getName();

    public static final String PREF_COMMON_MODULE_PREFERENCES = "pref_module_preferences";

    public static final String PREF_COMMON_UNMETERED_ONLY = "pref_common_unmetered_only";
    public static final boolean PREF_COMMON_UNMETERED_ONLY_DEFAULT = false;

    public static final String PREF_COMMON_REFRESH_RATE = "pref_common_refresh_rate";
    public static final String PREF_COMMON_REFRESH_RATE_DEFAULT = "300";

    public static final String PREF_QUOTES = "quotes";
    public static final String PREF_QUOTES_TEXT = "pref_quotes_text";
    public static final String PREF_QUOTES_SOURCE = "pref_quotes_source";

    public static final String PREF_ABOUT_VERSION = "pref_about_version";
    public static final String PREF_ABOUT_TWITTER = "pref_about_twitter";
    public static final String PREF_ABOUT_GITHUB = "pref_about_github";
    public static final String PREF_ABOUT_VNAAS = "pref_about_vnaas";
}
