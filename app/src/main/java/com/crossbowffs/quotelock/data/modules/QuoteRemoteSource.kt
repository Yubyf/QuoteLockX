package com.crossbowffs.quotelock.data.modules

import android.content.Context
import com.crossbowffs.quotelock.consts.PREF_COMMON_QUOTE_MODULE
import com.crossbowffs.quotelock.consts.PREF_COMMON_QUOTE_MODULE_DEFAULT
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.data.api.QuoteModule
import com.crossbowffs.quotelock.data.datastore.PreferenceDataStoreAdapter
import com.crossbowffs.quotelock.di.CommonDataStore
import com.crossbowffs.quotelock.di.IoDispatcher
import com.crossbowffs.quotelock.utils.Xlog
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuoteRemoteSource @Inject constructor(
    @ApplicationContext private val context: Context,
    @CommonDataStore private val commonDataStore: PreferenceDataStoreAdapter,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) {

    @Throws(CancellationException::class)
    suspend fun downloadQuote(): QuoteData? = withContext(dispatcher) {
        context.fetchQuote().also {
            Xlog.d(TAG, "QuoteDownloader success: ${it != null}")
        }
    }

    private suspend fun Context.fetchQuote(): QuoteData? {
        val moduleName: String = commonDataStore.getString(PREF_COMMON_QUOTE_MODULE,
            PREF_COMMON_QUOTE_MODULE_DEFAULT)!!

        Xlog.d(TAG, "Attempting to download new quote...")
        val module: QuoteModule = try {
            Modules[moduleName]
        } catch (e: ModuleNotFoundException) {
            Xlog.e(TAG, "Selected module not found", e)
            return null
        }
        Xlog.d(TAG, "Provider: ${module.getDisplayName(this)}")
        return runCatching { module.getQuote(this@fetchQuote) }.onFailure {
            Xlog.e(TAG, "Quote download failed", it)
        }.getOrNull()
    }

    companion object {
        private const val TAG = "QuoteRemoteSource"
    }
}