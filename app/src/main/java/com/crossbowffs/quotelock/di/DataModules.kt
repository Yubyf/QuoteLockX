package com.crossbowffs.quotelock.di

import android.accounts.AccountManager
import android.content.Context
import com.crossbowffs.quotelock.data.history.QuoteHistoryDao
import com.crossbowffs.quotelock.data.history.QuoteHistoryDatabase
import com.crossbowffs.quotelock.data.modules.collections.database.QuoteCollectionDao
import com.crossbowffs.quotelock.data.modules.collections.database.QuoteCollectionDatabase
import com.crossbowffs.quotelock.data.modules.custom.database.CustomQuoteDao
import com.crossbowffs.quotelock.data.modules.custom.database.CustomQuoteDatabase
import com.crossbowffs.quotelock.data.modules.fortune.database.FortuneQuoteDao
import com.crossbowffs.quotelock.data.modules.fortune.database.FortuneQuoteDatabase
import com.crossbowffs.quotelock.data.modules.openai.OpenAIUsageDao
import com.crossbowffs.quotelock.data.modules.openai.OpenAIUsageDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

const val PACKAGE_SCOPE = "com.crossbowffs.quotelock"

const val DISPATCHER_IO = "DISPATCHER_IO"

@Module(
    [
        CoroutineModule::class, DatabaseModules::class, DataStoreModules::class, NetModules::class,
    ]
)
@ComponentScan(PACKAGE_SCOPE)
class DataModule {

    @Single
    fun provideAccountManager(context: Context): AccountManager = AccountManager.get(context)
}

@Module
class CoroutineModule {

    @Single
    @Named(DISPATCHER_IO)
    fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO
}

@Module
class DatabaseModules {
    @Single
    fun provideQuoteHistoryDao(context: Context): QuoteHistoryDao =
        QuoteHistoryDatabase.getDatabase(context).dao()

    @Single
    fun provideQuoteCollectionDao(context: Context): QuoteCollectionDao =
        QuoteCollectionDatabase.getDatabase(context).dao()

    @Single
    fun provideCustomQuoteDao(context: Context): CustomQuoteDao =
        CustomQuoteDatabase.getDatabase(context).dao()

    @Single
    fun provideFortuneQuoteDao(context: Context): FortuneQuoteDao =
        FortuneQuoteDatabase.getDatabase(context).dao()

    @Single
    fun provideOpenAIUsageDao(context: Context): OpenAIUsageDao =
        OpenAIUsageDatabase.getDatabase(context).dao()
}
