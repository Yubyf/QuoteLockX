package com.crossbowffs.quotelock.data.api

import android.content.res.Resources
import com.crossbowffs.quotelock.consts.PREF_QUOTE_SOURCE_PREFIX
import com.yubyf.quotelockx.R

data class QuoteViewData(val text: String = "", val source: String = "")

fun Resources.buildQuoteViewData(
    text: String,
    source: String?,
    author: String?,
): QuoteViewData {
    return QuoteViewData(
        text,
        author.let {
            if (!it.isNullOrBlank()) {
                "$PREF_QUOTE_SOURCE_PREFIX$it${if (source.isNullOrBlank()) "" else " $source"}"
            } else {
                if (source == getString(R.string.module_custom_setup_line2)
                    || source == getString(R.string.module_collections_setup_line2)
                ) {
                    source
                } else {
                    if (source.isNullOrBlank()) {
                        ""
                    } else {
                        "$PREF_QUOTE_SOURCE_PREFIX$source"
                    }
                }
            }
        }
    )
}
