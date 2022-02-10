package com.crossbowffs.quotelock.modules.custom.provider

import android.content.ContentResolver
import android.net.Uri
import android.provider.BaseColumns
import com.crossbowffs.quotelock.BuildConfig

object CustomQuoteContract {
    const val AUTHORITY = BuildConfig.APPLICATION_ID + ".modules.custom.provider"
    val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY")

    object Quotes : BaseColumns {
        const val TABLE = "quotes"
        val CONTENT_URI: Uri = Uri.withAppendedPath(CustomQuoteContract.CONTENT_URI, TABLE)
        const val CONTENT_TYPE = "${ContentResolver.CURSOR_DIR_BASE_TYPE}/vnd.crossbowffs.quote"
        const val CONTENT_ITEM_TYPE =
            "${ContentResolver.CURSOR_ITEM_BASE_TYPE}/vnd.crossbowffs.quote"
        const val TEXT = "text"
        const val SOURCE = "source"
        const val ID = BaseColumns._ID
        val ALL = arrayOf(
            ID,
            TEXT,
            SOURCE
        )
    }
}