@file:JvmName("DeviceUtils")

package com.crossbowffs.quotelock.utils

import android.os.Build

/** 1900/1905:OP7 China, 1910 OP7Pro China, 1911: India, 1913: EU,
 *  1915: Tmobile, 1917: global/US unlocked, 1920: EU 5G
 */
private val OP7_DEVICE_MODELS: List<String> = listOf(
    "GM1900",
    "GM1905",
    "GM1910",
    "GM1911",
    "GM1913",
    "GM1915",
    "GM1917",
    "GM1920",
)

/**
 * @return True if current device belongs to OnePlus 7 series.
 */
val isOnePlus7Series: Boolean
    get() = OP7_DEVICE_MODELS.contains(Build.MODEL)