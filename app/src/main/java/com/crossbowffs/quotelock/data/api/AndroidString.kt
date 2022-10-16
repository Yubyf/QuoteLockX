package com.crossbowffs.quotelock.data.api

import android.content.Context
import com.crossbowffs.quotelock.app.font.FontInfo

sealed class AndroidString {
    data class StringText(val string: String) : AndroidString()
    data class StringRes(
        val stringRes: Int,
        val args: Array<Any> = emptyArray(),
    ) : AndroidString() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as StringRes

            if (stringRes != other.stringRes) return false
            if (!args.contentEquals(other.args)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = stringRes
            result = 31 * result + args.contentHashCode()
            return result
        }
    }
}

fun AndroidString?.contextString(context: Context): String {
    return when (this) {
        is AndroidString.StringText -> string
        is AndroidString.StringRes -> {
            val firstArg = args.firstOrNull()
            if (args.size == 1 && firstArg is FontInfo) {
                val arg = with(firstArg) { context.resources.configuration.localeName }
                context.getString(stringRes, arg)
            } else {
                context.getString(stringRes, *args)
            }
        }

        null -> ""
    }
}
