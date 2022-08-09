@file:JvmName("TypefaceUtils")

package com.crossbowffs.quotelock.utils

import android.content.Context
import android.content.res.Resources.NotFoundException
import android.graphics.Typeface
import androidx.annotation.FontRes
import androidx.core.content.res.ResourcesCompat

private fun getFont(context: Context, @FontRes id: Int): Typeface? {
    return ResourcesCompat.getFont(context, id)
}

fun Context.getFontFromName(fontResName: String): Typeface? {
    val res = resources
    val resId = res.getIdentifier(fontResName, "font", packageName)
    if (resId == 0) {
        throw NotFoundException("Could not find font: $fontResName")
    }
    return getFont(this, resId)
}

fun getTypefaceStyle(styles: Set<String>?): Int {
    var style = Typeface.NORMAL
    if (styles != null) {
        if (styles.contains("bold") && styles.contains("italic")) {
            style = Typeface.BOLD_ITALIC
        } else if (styles.contains("bold")) {
            style = Typeface.BOLD
        } else if (styles.contains("italic")) {
            style = Typeface.ITALIC
        }
    }
    return style
}