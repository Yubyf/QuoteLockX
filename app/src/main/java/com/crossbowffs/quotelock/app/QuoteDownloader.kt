package com.crossbowffs.quotelock.app

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.text.TextUtils
import com.crossbowffs.quotelock.api.QuoteData
import com.crossbowffs.quotelock.api.QuoteModule
import com.crossbowffs.quotelock.collections.provider.QuoteCollectionContract
import com.crossbowffs.quotelock.consts.*
import com.crossbowffs.quotelock.data.commonDataStore
import com.crossbowffs.quotelock.data.quotesDataStore
import com.crossbowffs.quotelock.history.provider.QuoteHistoryContract
import com.crossbowffs.quotelock.modules.ModuleManager
import com.crossbowffs.quotelock.modules.ModuleNotFoundException
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.md5
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible

private const val TAG = "QuoteDownloader"

@Throws(CancellationException::class)
suspend fun Context.downloadQuote(): QuoteData? {
    return fetchQuote().also {
        Xlog.d(TAG, "QuoteDownloader success: ${it != null}")
        handleQuote(it)
    }
}

private suspend fun Context.fetchQuote(): QuoteData? {
    val mModuleName: String = commonDataStore.getString(PREF_COMMON_QUOTE_MODULE,
        PREF_COMMON_QUOTE_MODULE_DEFAULT)!!

    Xlog.d(TAG, "Attempting to download new quote...")
    val module: QuoteModule = try {
        ModuleManager.getModule(this, mModuleName)
    } catch (e: ModuleNotFoundException) {
        Xlog.e(TAG, "Selected module not found", e)
        return null
    }
    Xlog.d(TAG, "Provider: ${module.getDisplayName(this)}")
    return runInterruptible(Dispatchers.IO) {
        try {
            module.getQuote(this)
        } catch (e: Exception) {
            Xlog.e(TAG, "Quote download failed", e)
            null
        }
    }
}

private fun Context.queryQuoteCollectionState(text: String, source: String): Boolean {
    val uri =
        Uri.withAppendedPath(Uri.withAppendedPath(QuoteCollectionContract.Collections.CONTENT_URI,
            QuoteCollectionContract.Collections.MD5), (text + source).md5())
    val columns = arrayOf(QuoteCollectionContract.Collections.ID)
    contentResolver.query(uri, columns, null, null, null)
        .use { cursor ->
            return if (cursor != null && cursor.moveToFirst()) {
                cursor.getInt(0) > 0
            } else {
                false
            }
        }
}

private fun Context.insertQuoteHistory(text: String, source: String) {
    val values = ContentValues(3).apply {
        put(QuoteHistoryContract.Histories.TEXT, text)
        put(QuoteHistoryContract.Histories.SOURCE, source)
        put(QuoteHistoryContract.Histories.MD5, (text + source).md5())
    }
    contentResolver.insert(QuoteHistoryContract.Histories.CONTENT_URI, values)
}

private fun Context.handleQuote(quote: QuoteData?) {
    quote?.run {
        Xlog.d(TAG, "Text: $quoteText")
        Xlog.d(TAG, "Source: $quoteSource")
        insertQuoteHistory(quoteText, quoteSource)
        val collectionState = queryQuoteCollectionState(quoteText,
            if (TextUtils.isEmpty(quoteSource)) "" else quoteSource.replace("―", "")
                .trim())
        quotesDataStore.bulkPut(mapOf(PREF_QUOTES_TEXT to quoteText,
            PREF_QUOTES_SOURCE to quoteSource,
            PREF_QUOTES_COLLECTION_STATE to collectionState,
            PREF_QUOTES_LAST_UPDATED to System.currentTimeMillis()))
    }
}