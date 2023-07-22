package com.crossbowffs.quotelock.data.modules

import android.content.Context
import com.crossbowffs.quotelock.consts.PREF_COMMON_QUOTE_MODULE
import com.crossbowffs.quotelock.consts.PREF_COMMON_QUOTE_MODULE_DEFAULT
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.data.api.QuoteModule
import com.crossbowffs.quotelock.di.COMMON_DATA_STORE
import com.crossbowffs.quotelock.di.DISPATCHER_IO
import com.crossbowffs.quotelock.utils.Xlog
import com.yubyf.datastore.DataStoreDelegate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class QuoteRemoteSource(
    private val context: Context,
    @Named(COMMON_DATA_STORE) private val commonDataStore: DataStoreDelegate,
    @Named(DISPATCHER_IO) private val dispatcher: CoroutineDispatcher,
) {

    @Throws(CancellationException::class)
    suspend fun downloadQuote(): QuoteData? = withContext(dispatcher) {
        context.fetchQuote().also {
            Xlog.d(TAG, "QuoteDownloader success: ${it != null}")
        }
    }

    private suspend fun Context.fetchQuote(): QuoteData? {
        val moduleName: String = commonDataStore.getStringSuspend(
            PREF_COMMON_QUOTE_MODULE,
            PREF_COMMON_QUOTE_MODULE_DEFAULT
        )!!

        Xlog.d(TAG, "Attempting to download new quote...")
        val module: QuoteModule = try {
            Modules[moduleName]
        } catch (e: ModuleNotFoundException) {
            Xlog.e(TAG, "Selected module not found", e)
            return null
        }
        Xlog.d(TAG, "Provider: ${module.getDisplayName(this)}")
        return runCatching {
            module.run {
                this@fetchQuote.getQuote()
            }
        }.onFailure {
            Xlog.e(TAG, "Quote download failed", it)
        }.getOrNull()
    }

    companion object {
        private const val TAG = "QuoteRemoteSource"
    }
}