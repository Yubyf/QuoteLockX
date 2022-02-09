package com.crossbowffs.quotelock.collections.provider

import android.content.ContentResolver
import android.net.Uri
import android.provider.BaseColumns
import com.crossbowffs.quotelock.BuildConfig

/**
 * @author Yubyf
 */
object QuoteCollectionContract {
    const val AUTHORITY = BuildConfig.APPLICATION_ID + ".collection.provider"
    val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY")

    object Collections : BaseColumns {
        const val TABLE = "collections"

        @JvmField
        val CONTENT_URI: Uri = Uri.withAppendedPath(QuoteCollectionContract.CONTENT_URI, TABLE)
        const val CONTENT_TYPE =
            "${ContentResolver.CURSOR_DIR_BASE_TYPE}/vnd.crossbowffs.collection"
        const val CONTENT_ITEM_TYPE =
            "${ContentResolver.CURSOR_DIR_BASE_TYPE}/vnd.crossbowffs.collection"
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