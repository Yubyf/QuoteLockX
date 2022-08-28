package com.crossbowffs.quotelock.data.api

/**
 * @author Yubyf
 * @date 2022/8/8.
 */
data class QuoteModuleData(
    val displayName: String,
    val configRoute: String?,
    val minimumRefreshInterval: Int,
    val requiresInternetConnectivity: Boolean,
)
