package com.crossbowffs.quotelock.utils

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
import java.util.Locale

val supportedLocales =
    sequenceOf(Locale.ENGLISH, Locale.SIMPLIFIED_CHINESE, Locale.TRADITIONAL_CHINESE)

fun String.prefix(prefix: String): String {
    return prefix + this
}

fun Context.getStringForSupportLocales(@StringRes id: Int): Sequence<String> =
    Configuration().let { configuration ->
        supportedLocales.map { locale ->
            configuration.setLocale(locale)
            createConfigurationContext(configuration).resources.getString(id)
        }
    }

fun Context.isStringMatchesResource(text: String?, @StringRes id: Int): Boolean =
    !text.isNullOrBlank() && getStringForSupportLocales(id).any { it == text }