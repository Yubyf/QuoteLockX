package com.crossbowffs.quotelock.di

import android.accounts.AccountManager
import android.content.Context
import com.crossbowffs.quotelock.data.ConfigurationRepository
import com.crossbowffs.quotelock.data.WidgetRepository
import com.crossbowffs.quotelock.data.history.QuoteHistoryDatabase
import com.crossbowffs.quotelock.data.history.QuoteHistoryRepository
import com.crossbowffs.quotelock.data.modules.QuoteRepository
import com.crossbowffs.quotelock.data.modules.collections.QuoteCollectionRepository
import com.crossbowffs.quotelock.data.modules.collections.backup.CollectionLocalBackupSource
import com.crossbowffs.quotelock.data.modules.collections.backup.CollectionRemoteSyncSource
import com.crossbowffs.quotelock.data.modules.collections.database.QuoteCollectionDatabase
import com.crossbowffs.quotelock.data.modules.custom.CustomQuoteRepository
import com.crossbowffs.quotelock.data.modules.custom.database.CustomQuoteDatabase
import com.crossbowffs.quotelock.data.modules.fortune.database.FortuneQuoteDatabase
import com.crossbowffs.quotelock.data.modules.openai.OpenAIRepository
import com.crossbowffs.quotelock.data.modules.openai.OpenAIUsageDatabase
import com.crossbowffs.quotelock.data.modules.wikiquote.WikiquoteRepository
import com.crossbowffs.quotelock.data.version.VersionRepository
import com.yubyf.datastore.DataStoreDelegate
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class IoDispatcher

@Module
@InstallIn(SingletonComponent::class)
object CoroutineModule {

    @Provides
    @IoDispatcher
    fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO
}

@Module
@InstallIn(SingletonComponent::class)
object SyncModule {

    @Singleton
    @Provides
    fun provideAccountManager(
        @ApplicationContext context: Context,
    ): AccountManager = AccountManager.get(context)
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModules {

    @Singleton
    @Provides
    fun provideQuoteHistoryRepository(
        database: QuoteHistoryDatabase,
        @IoDispatcher dispatcher: CoroutineDispatcher,
    ): QuoteHistoryRepository {
        return QuoteHistoryRepository(database.dao(), dispatcher)
    }

    @Singleton
    @Provides
    fun provideQuoteCollectionRepository(
        database: QuoteCollectionDatabase,
        localBackupSource: CollectionLocalBackupSource,
        remoteSyncSource: CollectionRemoteSyncSource,
        @IoDispatcher dispatcher: CoroutineDispatcher,
    ): QuoteCollectionRepository {
        return QuoteCollectionRepository(database.dao(),
            localBackupSource,
            remoteSyncSource,
            dispatcher)
    }

    @Singleton
    @Provides
    fun provideCustomQuoteRepository(
        database: CustomQuoteDatabase,
        @IoDispatcher dispatcher: CoroutineDispatcher,
    ): CustomQuoteRepository {
        return CustomQuoteRepository(database.dao(), dispatcher)
    }

    @Singleton
    @Provides
    fun provideOpenAIUsageRepository(
        @OpenAIDataStore openaiDataStore: DataStoreDelegate,
        openAIUsageDatabase: OpenAIUsageDatabase,
        @IoDispatcher dispatcher: CoroutineDispatcher,
        httpClient: HttpClient,
        json: Json,
    ): OpenAIRepository {
        return OpenAIRepository(
            openaiDataStore,
            openAIUsageDatabase.dao(),
            dispatcher,
            httpClient,
            json
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModules {

    @Singleton
    @Provides
    fun provideCollectionLocalBackupSource(
        @ApplicationContext context: Context,
        database: QuoteCollectionDatabase,
    ): CollectionLocalBackupSource {
        return CollectionLocalBackupSource(context, database.dao())
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModules {

    @Singleton
    @Provides
    fun provideQuoteHistoryDatabase(@ApplicationContext context: Context): QuoteHistoryDatabase {
        return QuoteHistoryDatabase.getDatabase(context)
    }

    @Singleton
    @Provides
    fun provideQuoteCollectionDatabase(@ApplicationContext context: Context): QuoteCollectionDatabase {
        return QuoteCollectionDatabase.getDatabase(context)
    }

    @Singleton
    @Provides
    fun provideCustomQuoteDatabase(@ApplicationContext context: Context): CustomQuoteDatabase {
        return CustomQuoteDatabase.getDatabase(context)
    }

    @Singleton
    @Provides
    fun provideFortuneQuoteDatabase(@ApplicationContext context: Context): FortuneQuoteDatabase {
        return FortuneQuoteDatabase.getDatabase(context)
    }

    @Singleton
    @Provides
    fun provideOpenAIUsageDatabase(@ApplicationContext context: Context): OpenAIUsageDatabase {
        return OpenAIUsageDatabase.getDatabase(context)
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface QuoteProviderEntryPoint {
    fun quoteRepository(): QuoteRepository
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ConfigurationEntryPoint {
    fun configurationRepository(): ConfigurationRepository
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WikiquoteEntryPoint {
    fun wikiquoteRepository(): WikiquoteRepository
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface OpenAIEntryPoint {
    fun openAIRepository(): OpenAIRepository
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun widgetRepository(): WidgetRepository
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface VersionEntryPoint {
    fun versionRepository(): VersionRepository
}