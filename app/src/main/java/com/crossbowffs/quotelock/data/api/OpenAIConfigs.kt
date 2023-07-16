package com.crossbowffs.quotelock.data.api

import com.crossbowffs.quotelock.app.configs.openai.OpenAIPrefKeys.PREF_OPENAI_LANGUAGE_DEFAULT
import com.crossbowffs.quotelock.app.configs.openai.OpenAIPrefKeys.PREF_OPENAI_MODEL_DEFAULT
import com.crossbowffs.quotelock.app.configs.openai.OpenAIPrefKeys.PREF_OPENAI_QUOTE_TYPE_DEFAULT

data class OpenAIConfigs(
    val language: String = PREF_OPENAI_LANGUAGE_DEFAULT,
    val model: String = PREF_OPENAI_MODEL_DEFAULT,
    val quoteType: Int = PREF_OPENAI_QUOTE_TYPE_DEFAULT,
    val apiHost: String? = null,
    val apiKey: String? = null,
)