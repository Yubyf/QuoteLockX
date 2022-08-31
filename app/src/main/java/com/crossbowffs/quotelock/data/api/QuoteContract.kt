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
    source.let {
        if (!author.isNullOrBlank()) "${author}${if (it.isBlank()) "" else " $it"}" else it
    }.takeIf(String::isNotBlank)
)

data class ReadableQuote(
    val text: String,
    val source: String?,
)
