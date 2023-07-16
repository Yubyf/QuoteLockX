package com.crossbowffs.quotelock.data.modules.openai.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenAIChatResponse(
    @SerialName("id")
    val id: String,
    @SerialName("object")
    val objectX: String,
    @SerialName("created")
    val created: Long,
    @SerialName("model")
    val model: String,
    @SerialName("choices")
    val choices: List<OpenAIChatChoice>,
    @SerialName("usage")
    val usage: OpenAIChatUsage,
)

@Serializable
data class OpenAIChatChoice(
    @SerialName("index")
    val index: Int,
    @SerialName("message")
    val message: OpenAIChatMessage,
    @SerialName("finish_reason")
    val finishReason: String,
)

@Serializable
data class OpenAIChatUsage(
    @SerialName("prompt_tokens")
    val promptTokens: Int,
    @SerialName("completion_tokens")
    val completionTokens: Int,
    @SerialName("total_tokens")
    val totalTokens: Int,
)
