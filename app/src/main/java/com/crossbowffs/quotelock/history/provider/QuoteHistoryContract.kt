package com.crossbowffs.quotelock.history.provider

import android.content.ContentResolver
import android.net.Uri
import android.provider.BaseColumns
import com.crossbowffs.quotelock.BuildConfig

/**
 * @author Yubyf
 */
object QuoteHistoryContract {
    const val AUTHORITY = BuildConfig.APPLICATION_ID + ".history.provider"
    val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY")

    object Histories : BaseColumns {
        const val TABLE = "histories"

        val CONTENT_URI: Uri = Uri.withAppendedPath(QuoteHistoryContract.CONTENT_URI, TABLE)
        const val CONTENT_TYPE = "${ContentResolver.CURSOR_DIR_BASE_TYPE}/vnd.crossbowffs.history"
        const val CONTENT_ITEM_TYPE =
            "${ContentResolver.CURSOR_ITEM_BASE_TYPE}/vnd.crossbowffs.history"
        const val MD5 = "md5"
        const val TEXT = "text"
        const val SOURCE = "source"
        const val ID = BaseColumns._ID
        val ALL = arrayOf(
            ID,
            MD5,
            TEXT,
            SOURCE
        )
    }
}