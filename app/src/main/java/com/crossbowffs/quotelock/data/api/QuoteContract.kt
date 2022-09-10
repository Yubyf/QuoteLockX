package com.crossbowffs.quotelock.data.api

import android.provider.BaseColumns
import com.crossbowffs.quotelock.consts.PREF_QUOTE_SOURCE_PREFIX
import com.crossbowffs.quotelock.utils.md5

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

val QuoteEntity.readableSource: String
    get() = buildReadableSource(source, author)

fun buildReadableSource(source: String?, author: String?, withPrefix: Boolean = false): String =
    source?.let {
        (if (!author.isNullOrBlank()) "${author}${if (it.isBlank()) "" else " $it"}" else it)
            .takeIf { formatted ->
                formatted.isNotBlank()
            }?.let { formatted ->
                if (withPrefix) PREF_QUOTE_SOURCE_PREFIX + formatted
                else formatted
            }
    }.orEmpty()

fun QuoteEntity.toQuoteData(): QuoteData = QuoteData(text, source, author.orEmpty())

val QuoteData.md5: String
    get() = ("$quoteText$quoteSource$quoteAuthor").md5()

val QuoteDataWithCollectState.md5: String
    get() = ("$quoteText$quoteSource$quoteAuthor").md5()