package com.crossbowffs.quotelock.data.modules

import androidx.datastore.preferences.core.Preferences
import com.crossbowffs.quotelock.consts.PREF_BOOT_NOTIFY_FLAG
import com.crossbowffs.quotelock.consts.PREF_QUOTES_COLLECTION_STATE
import com.crossbowffs.quotelock.consts.PREF_QUOTES_CONTENTS
import com.crossbowffs.quotelock.consts.PREF_QUOTES_LAST_UPDATED
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.data.api.isQuoteJustForDisplay
import com.crossbowffs.quotelock.data.api.withCollectState
import com.crossbowffs.quotelock.data.history.QuoteHistoryEntity
import com.crossbowffs.quotelock.data.history.QuoteHistoryRepository
import com.crossbowffs.quotelock.data.modules.collections.QuoteCollectionRepository
import com.crossbowffs.quotelock.di.QUOTES_DATA_STORE
import com.crossbowffs.quotelock.utils.Xlog
import com.yubyf.datastore.DataStoreDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class QuoteLocalSource(
    @Named(QUOTES_DATA_STORE) private val quotesDataStore: DataStoreDelegate,
    private val collectionRepository: QuoteCollectionRepository,
    private val historyRepository: QuoteHistoryRepository,
) {

    private suspend fun queryQuoteCollectionState(uid: String): Boolean =
        collectionRepository.getByUid(uid) != null

    private suspend fun insertQuoteHistory(quote: QuoteData) = quote.run {
        historyRepository.insert(
            QuoteHistoryEntity(
                text = quoteText,
                source = quoteSource,
                author = quoteAuthor,
                provider = provider,
                uid = uid,
                extra = extra
            )
        )
    }

    suspend fun handleDownloadedQuote(quote: QuoteData?) = quote?.run {
        Xlog.d(TAG, "Text: $quoteText")
        Xlog.d(TAG, "Source: $quoteSource")
        Xlog.d(TAG, "Author: $quoteAuthor")
        if (!isQuoteJustForDisplay(quoteText, quoteSource, quoteAuthor)) {
            insertQuoteHistory(quote)
        }
        val collectionState = queryQuoteCollectionState(quote.uid)
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
        quoteData.withCollectState(
            quotesDataStore.getBooleanSuspend(PREF_QUOTES_COLLECTION_STATE, false)
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