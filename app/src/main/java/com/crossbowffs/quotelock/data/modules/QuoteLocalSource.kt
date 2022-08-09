package com.crossbowffs.quotelock.data.modules

import androidx.datastore.preferences.core.Preferences
import com.crossbowffs.quotelock.consts.*
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.data.datastore.PreferenceDataStoreAdapter
import com.crossbowffs.quotelock.data.history.QuoteHistoryEntity
import com.crossbowffs.quotelock.data.history.QuoteHistoryRepository
import com.crossbowffs.quotelock.data.modules.collections.QuoteCollectionRepository
import com.crossbowffs.quotelock.di.QuotesDataStore
import com.crossbowffs.quotelock.di.ResourceProvider
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.md5
import com.yubyf.quotelockx.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuoteLocalSource @Inject constructor(
    @QuotesDataStore private val quotesDataStore: PreferenceDataStoreAdapter,
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
        if (quoteSource != resourceProvider.getString(R.string.module_custom_setup_line2)
            && quoteSource != resourceProvider.getString(R.string.module_collections_setup_line2)
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

    fun setQuoteCollectionState(state: Boolean) {
        quotesDataStore.putBoolean(PREF_QUOTES_COLLECTION_STATE, state)
    }

    fun getLastUpdateTime() = quotesDataStore.getLong(PREF_QUOTES_LAST_UPDATED, -1)

    fun getCurrentQuote() = QuoteData(
        quotesDataStore.getString(PREF_QUOTES_TEXT, "")!!,
        quotesDataStore.getString(PREF_QUOTES_SOURCE, "")!!,
        quotesDataStore.getString(PREF_QUOTES_AUTHOR, "")!!,
    )

    fun notifyBooted() {
        val hasBootNotifyFlag = quotesDataStore.contains(PREF_BOOT_NOTIFY_FLAG)
        if (!hasBootNotifyFlag) {
            quotesDataStore.putInt(PREF_BOOT_NOTIFY_FLAG, 0)
        } else {
            quotesDataStore.remove(PREF_BOOT_NOTIFY_FLAG)
        }
    }

    suspend fun observeQuoteDataStore(collector: suspend (Preferences, Preferences.Key<*>?) -> Unit) =
        quotesDataStore.collectSuspend(collector)

    companion object {
        private const val TAG = "QuoteDownloader"
    }
}