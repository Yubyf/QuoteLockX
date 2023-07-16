package com.crossbowffs.quotelock.data.modules.openai.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenAIUsageResponse(
    @SerialName("daily_costs")
    val dailyCosts: List<DailyCost>,
    @SerialName("object")
    val objectX: String,
    @SerialName("total_usage")
    val totalUsage: Double,
)

@Serializable
data class DailyCost(
    @SerialName("line_items")
    val lineItems: List<LineItem>,
    @SerialName("timestamp")
    val timestamp: Double,
)

@Serializable
data class LineItem(
    @SerialName("cost")
    val cost: Double,
    @SerialName("name")
    val name: String,
)