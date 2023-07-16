@file:OptIn(ExperimentalSerializationApi::class)

package com.crossbowffs.quotelock.data.modules.openai.chat

import com.crossbowffs.quotelock.app.configs.openai.OpenAIPrefKeys
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenAIChatInput(
    @SerialName("model")
    @EncodeDefault
    val model: String = OpenAIPrefKeys.PREF_OPENAI_MODEL_DEFAULT,
    @SerialName("messages")
    val messages: List<OpenAIChatMessage>,
    @SerialName("temperature")
    @EncodeDefault
    val temperature: Float = 0.9F,
    @SerialName("top_p")
    @EncodeDefault
    val topP: Float = 1.0F,
    @SerialName("max_tokens")
    @EncodeDefault
    val maxTokens: Int = 4096,
)

@Serializable
data class OpenAIChatMessage(
    @SerialName("role")
    val role: String,
    @SerialName("content")
    val content: String,
) {
    companion object {
        fun system(content: String) = OpenAIChatMessage("system", content)
        fun user(content: String) = OpenAIChatMessage("user", content)
        fun assistant(content: String) = OpenAIChatMessage("assistant", content)
    }
}