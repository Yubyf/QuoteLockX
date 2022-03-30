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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
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

private suspend fun queryQuoteCollectionState(text: String, source: String): Boolean =
    quoteCollectionDatabase.dao().getByQuote(text, source).firstOrNull() != null

private fun insertQuoteHistory(text: String, source: String) {
    ioScope.launch {
        quoteHistoryDatabase.dao().insert(
            QuoteHistoryEntity(
                md5 = (text + source).md5(),
                text = text,
                source = source,
            )
        )
    }
}

private suspend fun handleQuote(quote: QuoteData?) {
    quote?.run {
        Xlog.d(TAG, "Text: $quoteText")
        Xlog.d(TAG, "Source: $quoteSource")
        val quoteSourceInDb =
            if (quoteSource.isBlank()) "" else quoteSource.replace(PREF_QUOTE_SOURCE_PREFIX, "")
                .trim()
        insertQuoteHistory(quoteText, quoteSourceInDb)
        val collectionState = queryQuoteCollectionState(quoteText, quoteSourceInDb)
        quotesDataStore.bulkPut(mapOf(PREF_QUOTES_TEXT to quoteText,
            PREF_QUOTES_SOURCE to quoteSource,
            PREF_QUOTES_COLLECTION_STATE to collectionState,
            PREF_QUOTES_LAST_UPDATED to System.currentTimeMillis()))
    }
}