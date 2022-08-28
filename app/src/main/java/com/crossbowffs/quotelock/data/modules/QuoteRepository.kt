package com.crossbowffs.quotelock.data.modules

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import com.crossbowffs.quotelock.data.api.QuoteModule
import com.crossbowffs.quotelock.data.api.QuoteModuleData
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuoteRepository @Inject internal constructor(
    @ApplicationContext private val context: Context,
    private val quoteRemoteSource: QuoteRemoteSource,
    private val quoteLocalSource: QuoteLocalSource,
) {

    fun getQuoteModule(className: String): QuoteModule = Modules[className]

    fun getQuoteModuleData(module: QuoteModule): QuoteModuleData = QuoteModuleData(
        displayName = module.getDisplayName(context),
        configRoute = module.getConfigRoute(),
        minimumRefreshInterval = module.getMinimumRefreshInterval(context),
        requiresInternetConnectivity = module.requiresInternetConnectivity(context),
    )

    fun getAllModules(): List<QuoteModule> = Modules.values()

    suspend fun downloadQuote() = quoteRemoteSource.downloadQuote().also {
        quoteLocalSource.handleDownloadedQuote(it)
    }

    fun setQuoteCollectionState(state: Boolean) = quoteLocalSource.setQuoteCollectionState(state)

    fun getLastUpdateTime() = quoteLocalSource.getLastUpdateTime()

    fun getCurrentQuote() = quoteLocalSource.getCurrentQuote()

    fun notifyBooted() = quoteLocalSource.notifyBooted()

    suspend fun observeQuoteData(collector: suspend (Preferences, Preferences.Key<*>?) -> Unit) =
        quoteLocalSource.observeQuoteDataStore(collector)
}