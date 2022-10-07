package com.crossbowffs.quotelock.data.modules

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.crossbowffs.quotelock.consts.PREF_QUOTES_COLLECTION_STATE
import com.crossbowffs.quotelock.consts.PREF_QUOTES_CONTENTS
import com.crossbowffs.quotelock.consts.PREF_QUOTES_LAST_UPDATED
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.data.api.QuoteModule
import com.crossbowffs.quotelock.data.api.QuoteModuleData
import com.crossbowffs.quotelock.data.api.md5
import com.crossbowffs.quotelock.data.modules.collections.QuoteCollectionRepository
import com.crossbowffs.quotelock.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuoteRepository @Inject internal constructor(
    @ApplicationContext private val context: Context,
    private val quoteRemoteSource: QuoteRemoteSource,
    private val quoteLocalSource: QuoteLocalSource,
    private val collectionRepository: QuoteCollectionRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) {

    private val _quoteDataFlow = MutableStateFlow(getCurrentQuote())
    val quoteDataFlow = _quoteDataFlow.asStateFlow()

    private val _lastUpdateFlow = MutableStateFlow(0L)
    val lastUpdateFlow = _lastUpdateFlow.asStateFlow()

    init {
        CoroutineScope(dispatcher).launch {
            quoteLocalSource.observeQuoteDataStore(this) { preferences, key ->
                when (key?.name) {
                    PREF_QUOTES_CONTENTS -> _quoteDataFlow.update { currentValue ->
                        QuoteData.fromByteString(
                            preferences[stringPreferencesKey(PREF_QUOTES_CONTENTS)]
                                .orEmpty()
                        ).let {
                            currentValue.copy(
                                quoteText = it.quoteText,
                                quoteSource = it.quoteSource,
                                quoteAuthor = it.quoteAuthor
                            )
                        }
                    }

                    PREF_QUOTES_COLLECTION_STATE -> _quoteDataFlow.update { currentValue ->
                        currentValue.copy(
                            collectState = preferences[booleanPreferencesKey(
                                PREF_QUOTES_COLLECTION_STATE
                            )] ?: false
                        )
                    }

                    PREF_QUOTES_LAST_UPDATED -> _lastUpdateFlow.value =
                        preferences[longPreferencesKey(PREF_QUOTES_LAST_UPDATED)] ?: 0L
                }
            }
            collectionRepository.getAllStream().onEach { collections ->
                val quoteData = quoteDataFlow.value
                if (collections.find { quoteData.md5 == it.md5 } == null) {
                    quoteLocalSource.setQuoteCollectionState(false)
                } else {
                    quoteLocalSource.setQuoteCollectionState(true)
                }
            }.launchIn(this)
        }
    }

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

    fun getLastUpdateTime() = quoteLocalSource.getLastUpdateTime()

    fun getCurrentQuote() = quoteLocalSource.getCurrentQuote()

    fun notifyBooted() = quoteLocalSource.notifyBooted()
}