package com.crossbowffs.quotelock.data.api

import android.provider.BaseColumns

/**
 * @author Yubyf
 */
object QuoteEntityContract {
    const val MD5 = "md5"
    const val TEXT = "text"
    const val SOURCE = "source"
    const val AUTHOR_OLD = "AUTHOR"
    const val AUTHOR = "author"
    const val ID = BaseColumns._ID
}

interface QuoteEntity {
    val id: Int?
    val md5: String
    val text: String
    val source: String
    val author: String?

    override fun equals(other: Any?): Boolean
}

fun QuoteEntity.toReadableQuote(): ReadableQuote = ReadableQuote(
    text,
    source.run {
        if (!author.isNullOrBlank()) {
            "${author}${if (this.isBlank()) "" else " $this"}"
        } else {
            this
        }
    }
)

data class ReadableQuote(
    val text: String,
    val source: String?,
)
