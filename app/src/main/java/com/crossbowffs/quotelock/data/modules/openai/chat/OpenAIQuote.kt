@file:OptIn(ExperimentalSerializationApi::class)

package com.crossbowffs.quotelock.data.modules.openai.chat

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenAIQuote(
    @SerialName("quote")
    val quote: String,
    @SerialName("source")
    @EncodeDefault
    val source: String = "",
    @SerialName("category")
    @EncodeDefault
    val category: String = "",
)