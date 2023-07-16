package com.crossbowffs.quotelock.data.api

data class OpenAIAccount(
    val apiKey: String,
    val hasPaymentMethod: Boolean,
    val softLimitUsd: Double,
    val hardLimitUsd: Double,
    val usageUsd: Double,
)