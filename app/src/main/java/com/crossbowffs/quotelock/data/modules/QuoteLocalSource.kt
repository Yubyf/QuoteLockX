package com.crossbowffs.quotelock.data.modules

import androidx.datastore.preferences.core.Preferences
import com.crossbowffs.quotelock.consts.PREF_BOOT_NOTIFY_FLAG
import com.crossbowffs.quotelock.consts.PREF_QUOTES_COLLECTION_STATE
import com.crossbowffs.quotelock.consts.PREF_QUOTES_CONTENTS
import com.crossbowffs.quotelock.consts.PREF_QUOTES_LAST_UPDATED
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.data.api.QuoteDataWithCollectState
import com.crossbowffs.quotelock.data.history.QuoteHistoryEntity
import com.crossbowffs.quotelock.data.history.QuoteHistoryRepository
import com.crossbowffs.quotelock.data.modules.collections.QuoteCollectionRepository
import com.crossbowffs.quotelock.di.QuotesDataStore
import com.crossbowffs.quotelock.di.ResourceProvider
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.md5
import com.yubyf.datastore.DataStoreDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuoteLocalSource @Inject constructor(
    @QuotesDataStore private val quotesDataStore: DataStoreDelegate,
    private val collectionRepository: QuoteCollectionRepository,
    private val historyRepository: QuoteHistoryRepository,
    private val resourceProvider: ResourceProvider,
) {

    private suspend fun queryQuoteCollectionState(
        text: String,
        source: String,
        author: String,
    ): Boolean = collectionRepository.getByQuote(text, source, author) != null

    private suspend fun insertQuoteHistory(text: String, source: String, author: String) =
        historyRepository.insert(
            QuoteHistoryEntity(
                md5 = ("$text$source$author").md5(),
                text = text,
                source = source,
                author = author,
            )
        )

    suspend fun handleDownloadedQuote(quote: QuoteData?) = quote?.run {
        Xlog.d(TAG, "Text: $quoteText")
        Xlog.d(TAG, "Source: $quoteSource")
        Xlog.d(TAG, "Author: $quoteAuthor")
        if (!resourceProvider.isQuoteJustForDisplay(quoteText, quoteSource, quoteAuthor)) {
            insertQuoteHistory(quoteText, quoteSource, quoteAuthor)
        }
        val collectionState = queryQuoteCollectionState(quoteText, quoteSource, quoteAuthor)
        quotesDataStore.bulkPut(
            mapOf(
                PREF_QUOTES_CONTENTS to byteString,
                PREF_QUOTES_COLLECTION_STATE to collectionState,
                PREF_QUOTES_LAST_UPDATED to System.currentTimeMillis()
            )
        )
    }

    fun setQuoteCollectionState(state: Boolean) {
        quotesDataStore.put(PREF_QUOTES_COLLECTION_STATE, state)
    }

    fun getLastUpdateTime() =
        runBlocking { quotesDataStore.getLongSuspend(PREF_QUOTES_LAST_UPDATED, -1) }

    fun getCurrentQuote() = runBlocking {
        val quoteDataByteString = quotesDataStore.getStringSuspend(PREF_QUOTES_CONTENTS, "")!!
        val quoteData = QuoteData.fromByteString(quoteDataByteString)
        QuoteDataWithCollectState(
            quoteData.quoteText,
            quoteData.quoteSource,
            quoteData.quoteAuthor,
            quotesDataStore.getBooleanSuspend(PREF_QUOTES_COLLECTION_STATE, false),
        )
    }

    fun notifyBooted() = runBlocking {
        val hasBootNotifyFlag = quotesDataStore.containsSuspend(PREF_BOOT_NOTIFY_FLAG)
        if (!hasBootNotifyFlag) {
            quotesDataStore.put(PREF_BOOT_NOTIFY_FLAG, 0)
        } else {
            quotesDataStore.remove(PREF_BOOT_NOTIFY_FLAG)
        }
    }

    fun observeQuoteDataStore(
        scope: CoroutineScope,
        collector: suspend (Preferences, Preferences.Key<*>?) -> Unit,
    ) = quotesDataStore.collectIn(scope, collector)

    companion object {
        private const val TAG = "QuoteLocalSource"
    }
}