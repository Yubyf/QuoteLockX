package com.crossbowffs.quotelock.di

import android.accounts.AccountManager
import android.content.Context
import com.crossbowffs.quotelock.account.SyncAccountManager
import com.crossbowffs.quotelock.data.ConfigurationRepository
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
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
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
    fun provideSyncAccountManager(
        @ApplicationContext context: Context,
        collectionRepository: QuoteCollectionRepository,
        @IoDispatcher dispatcher: CoroutineDispatcher,
    ): SyncAccountManager =
        SyncAccountManager(AccountManager.get(context), collectionRepository, dispatcher)
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