package com.crossbowffs.quotelock.data.api

import com.crossbowffs.quotelock.consts.PREF_COMMON_QUOTE_MODULE_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_COMMON_REFRESH_RATE_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_COMMON_UNMETERED_ONLY_DEFAULT

data class QuoteConfigs(
    val module: String = PREF_COMMON_QUOTE_MODULE_DEFAULT,
    val displayOnAod: Boolean = false,
    val refreshRate: Int = PREF_COMMON_REFRESH_RATE_DEFAULT.toInt(),
    val unmeteredOnly: Boolean = PREF_COMMON_UNMETERED_ONLY_DEFAULT,
    val refreshRateOverride: Int? = null,
)