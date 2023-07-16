package com.crossbowffs.quotelock.data.modules.openai.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenAISubscriptionResponse(
    @SerialName("has_payment_method")
    val hasPaymentMethod: Boolean,
    @SerialName("soft_limit_usd")
    val softLimitUsd: Double,
    @SerialName("hard_limit_usd")
    val hardLimitUsd: Double,
)
