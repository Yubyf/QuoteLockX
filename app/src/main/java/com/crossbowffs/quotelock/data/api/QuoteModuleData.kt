package com.crossbowffs.quotelock.data.api

import android.content.ComponentName

/**
 * @author Yubyf
 * @date 2022/8/8.
 */
data class QuoteModuleData(
    val displayName: String,
    val configActivity: ComponentName?,
    val minimumRefreshInterval: Int,
    val requiresInternetConnectivity: Boolean,
)
