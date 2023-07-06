package com.crossbowffs.quotelock.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AndroidUpdateInfo(
    @SerialName("versionCode")
    val versionCode: Int,
    @SerialName("versionName")
    val versionName: String,
    @SerialName("link")
    val link: String,
    @SerialName("changelog")
    val changelog: String,
)