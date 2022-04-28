package com.crossbowffs.quotelock.app

import android.content.Context
import com.crossbowffs.quotelock.api.QuoteData
import com.crossbowffs.quotelock.api.QuoteModule
import com.crossbowffs.quotelock.collections.database.quoteCollectionDatabase
import com.crossbowffs.quotelock.consts.*
import com.crossbowffs.quotelock.data.commonDataStore
import com.crossbowffs.quotelock.data.quotesDataStore
import com.crossbowffs.quotelock.history.database.QuoteHistoryEntity
import com.crossbowffs.quotelock.history.database.quoteHistoryDatabase
import com.crossbowffs.quotelock.modules.ModuleManager
import com.crossbowffs.quotelock.modules.ModuleNotFoundException
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.ioScope
import com.crossbowffs.quotelock.utils.md5
import com.yubyf.quotelockx.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        ModuleManager.getModule(mModuleName)
    } catch (e: ModuleNotFoundException) {
        Xlog.e(TAG, "Selected module not found", e)
        return null
    }
    Xlog.d(TAG, "Provider: ${module.getDisplayName(this)}")
    return withContext(Dispatchers.IO) {
        runCatching { module.getQuote(this@fetchQuote) }.onFailure {
            Xlog.e(TAG, "Quote download failed", it)
        }.getOrNull()
    }
}

private suspend fun queryQuoteCollectionState(
    text: String,
    source: String,
    author: String,
): Boolean =
    quoteCollectionDatabase.dao().getByQuote(text, source, author).firstOrNull() != null

private fun insertQuoteHistory(text: String, source: String, author: String) {
    ioScope.launch {
        quoteHistoryDatabase.dao().insert(
            QuoteHistoryEntity(
                md5 = ("$text$source$author").md5(),
                text = text,
                source = source,
                author = author,
            )
        )
    }
}

private suspend fun handleQuote(quote: QuoteData?) {
    quote?.run {
        Xlog.d(TAG, "Text: $quoteText")
        Xlog.d(TAG, "Source: $quoteSource")
        Xlog.d(TAG, "Author: $quoteAuthor")
        if (quoteSource != App.INSTANCE.getString(R.string.module_custom_setup_line2)
            && quoteSource != App.INSTANCE.getString(R.string.module_collections_setup_line2)
        ) {
            insertQuoteHistory(quoteText, quoteSource, quoteAuthor)
        }
        val collectionState = queryQuoteCollectionState(quoteText, quoteSource, quoteAuthor)
        quotesDataStore.bulkPut(
            mapOf(PREF_QUOTES_TEXT to quoteText,
                PREF_QUOTES_SOURCE to quoteSource,
                PREF_QUOTES_AUTHOR to quoteAuthor,
                PREF_QUOTES_COLLECTION_STATE to collectionState,
                PREF_QUOTES_LAST_UPDATED to System.currentTimeMillis()))
    }
}