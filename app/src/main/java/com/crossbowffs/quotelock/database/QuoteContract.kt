package com.crossbowffs.quotelock.database

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