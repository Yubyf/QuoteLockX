@file:JvmName("DpUtils")

package com.crossbowffs.quotelock.utils

import android.content.res.Resources
import android.util.TypedValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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

fun Int.px2dp(): Dp = (this.toFloat() / displayMetrics.density).dp