package com.crossbowffs.quotelock.utils

import com.yubyf.datastore.DataStoreDelegate

@Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
suspend fun <T> DataStoreDelegate.getValueByDefault(key: String, default: T): T = when (default) {
    is Int -> getIntSuspend(key, default)
    is String,
    is String?,
    -> getStringSuspend(key) ?: default

    is Boolean -> getBooleanSuspend(key, default)
    is Float -> getFloatSuspend(key, default)
    is Long -> getLongSuspend(key, default)
    is Set<*>?,
    is Set<*>,
    -> getStringSetSuspend(key)
        ?: default as Set<String>

    else -> throw IllegalArgumentException("Type not supported: ${default?.let { it::class } ?: "null"}")
} as T