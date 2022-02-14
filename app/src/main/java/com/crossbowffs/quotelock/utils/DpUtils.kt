@file:JvmName("DpUtils")

package com.crossbowffs.quotelock.utils

import android.content.res.Resources
import android.util.TypedValue

private val displayMetrics = Resources.getSystem().displayMetrics

fun Float.dp2px(): Float = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this,
    displayMetrics
)

fun Int.dp2px(): Float = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this.toFloat(),
    displayMetrics
)