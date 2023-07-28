package com.crossbowffs.quotelock.data.modules.openai.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenAIModelsResponse(
    @SerialName("object")
    val objectX: String,
    @SerialName("data")
    val dataX: List<OpenAIModelData>,
)

@Serializable
data class OpenAIModelData(
    @SerialName("id")
    val id: String,
    @SerialName("object")
    val objectX: String,
    @SerialName("created")
    val created: Long,
    @SerialName("owned_by")
    val ownedBy: String,
    @SerialName("root")
    val root: String,
)
