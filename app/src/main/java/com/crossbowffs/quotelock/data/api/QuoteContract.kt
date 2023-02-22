package com.crossbowffs.quotelock.data.api

import android.content.Context
import android.content.res.Configuration
import android.provider.BaseColumns
import com.crossbowffs.quotelock.app.App
import com.crossbowffs.quotelock.consts.PREF_QUOTE_SOURCE_PREFIX
import com.yubyf.quotelockx.R
import java.util.Locale

/**
 * @author Yubyf
 */
object QuoteEntityContract {
    @Deprecated("Use [UID] instead")
    const val MD5 = "md5"
    const val TEXT = "text"
    const val SOURCE = "source"
    const val AUTHOR_OLD = "AUTHOR"
    const val AUTHOR = "author"
    const val ID = BaseColumns._ID
    const val UID = "uid"
    const val PROVIDER = "provider"
}

interface QuoteEntity {
    val id: Int?
    val text: String
    val source: String
    val author: String?
    val provider: String
    val uid: String

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

fun Context.isQuoteGeneratedByApp(text: String, source: String?, author: String?): Boolean {
    if (source.isNullOrBlank() && author.isNullOrBlank()) return false
    val configuration = Configuration(resources.configuration)
    return sequenceOf(Locale.ENGLISH, Locale.SIMPLIFIED_CHINESE, Locale.TRADITIONAL_CHINESE).map {
        configuration.setLocale(it)
        createConfigurationContext(configuration).resources.let { resources ->
            text == resources.getString(R.string.module_custom_setup_line1)
                    && source == resources.getString(R.string.module_custom_setup_line2)
                    || (text == resources.getString(R.string.module_collections_setup_line1)
                    && source == resources.getString(R.string.module_collections_setup_line2))
        }
    }.reduce { acc, b -> acc || b }
}

fun isQuoteJustForDisplay(text: String, source: String?, author: String?) =
    App.instance.isQuoteGeneratedByApp(text, source, author)

fun QuoteEntity.toQuoteData(): QuoteData = QuoteData(text, source, author.orEmpty(), provider, uid)