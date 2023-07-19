package com.crossbowffs.quotelock.data.api

import android.content.Context
import android.provider.BaseColumns
import com.crossbowffs.quotelock.app.App
import com.crossbowffs.quotelock.consts.PREF_QUOTE_SOURCE_PREFIX
import com.crossbowffs.quotelock.utils.isStringMatchesResource
import com.yubyf.quotelockx.R

/**
 * @author Yubyf
 */
object QuoteEntityContract {
    const val LEGACY_UID = "md5"
    const val TEXT = "text"
    const val SOURCE = "source"
    const val AUTHOR_OLD = "AUTHOR"
    const val AUTHOR = "author"
    const val ID = BaseColumns._ID
    const val UID = "uid"
    const val PROVIDER = "provider"
    const val EXTRA = "extra"
}

interface QuoteEntity {
    val id: Int?
    val text: String
    val source: String
    val author: String?
    val provider: String
    val uid: String
    val extra: ByteArray?

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

fun Context.isQuoteGeneratedByConfiguration(
    text: String,
    source: String?,
    author: String?,
): Boolean {
    if (source.isNullOrBlank() && author.isNullOrBlank()) return false
    return sequenceOf(
        R.string.module_custom_setup_line1,
        R.string.module_collections_setup_line1,
        R.string.module_openai_setup_line1
    ).any { isStringMatchesResource(text, it) }
            && sequenceOf(
        R.string.module_custom_setup_line2,
        R.string.module_collections_setup_line2,
        R.string.module_openai_setup_line2
    ).any { isStringMatchesResource(source, it) }
}

fun isQuoteJustForDisplay(text: String, source: String?, author: String?) =
    App.instance.isQuoteGeneratedByConfiguration(text, source, author)

fun QuoteEntity.toQuoteData(): QuoteData =
    QuoteData(text, source, author.orEmpty(), provider, uid, extra)