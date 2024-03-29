package com.crossbowffs.quotelock.di

import android.content.Context
import com.crossbowffs.quotelock.app.configs.brainyquote.BrainyQuotePrefKeys
import com.crossbowffs.quotelock.app.configs.fortune.FortunePrefKeys
import com.crossbowffs.quotelock.app.configs.hitokoto.HitokotoPrefKeys
import com.crossbowffs.quotelock.app.configs.openai.OpenAIPrefKeys
import com.crossbowffs.quotelock.app.configs.wikiquote.WikiquotePrefKeys
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE
import com.crossbowffs.quotelock.consts.PREF_COMMON
import com.crossbowffs.quotelock.consts.PREF_QUOTES
import com.crossbowffs.quotelock.consts.PREF_VERSION_UPDATE
import com.crossbowffs.quotelock.data.modules.jinrishici.JinrishiciQuoteModule
import com.yubyf.datastore.DataStoreDelegate
import com.yubyf.datastore.DataStoreDelegate.Companion.getDataStoreDelegate
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class CommonDataStore

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class VersionDataStore

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class QuotesDataStore

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class CardStyleDataStore

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class BrainyDataStore

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class WikiquoteDataStore

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class FortuneDataStore

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class HitokotoDataStore

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class JinrishiciDataStore

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class OpenAIDataStore

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModules {

    @Singleton
    @CommonDataStore
    @Provides
    fun provideCommonDataStore(@ApplicationContext context: Context): DataStoreDelegate =
        context.getDataStoreDelegate(PREF_COMMON, migrate = true)

    @Singleton
    @VersionDataStore
    @Provides
    fun provideVersionDataStore(@ApplicationContext context: Context): DataStoreDelegate =
        context.getDataStoreDelegate(PREF_VERSION_UPDATE, migrate = true)

    @Singleton
    @QuotesDataStore
    @Provides
    fun provideQuotesDataStore(@ApplicationContext context: Context): DataStoreDelegate =
        context.getDataStoreDelegate(PREF_QUOTES, migrate = true)

    @Singleton
    @CardStyleDataStore
    @Provides
    fun provideCardStyleDataStore(@ApplicationContext context: Context): DataStoreDelegate =
        context.getDataStoreDelegate(PREF_CARD_STYLE, migrate = true)

    @Singleton
    @BrainyDataStore
    @Provides
    fun provideBrainyDataStore(@ApplicationContext context: Context): DataStoreDelegate =
        context.getDataStoreDelegate(BrainyQuotePrefKeys.PREF_BRAINY, migrate = true)

    @Singleton
    @WikiquoteDataStore
    @Provides
    fun provideWikiquoteDataStore(@ApplicationContext context: Context): DataStoreDelegate =
        context.getDataStoreDelegate(WikiquotePrefKeys.PREF_WIKIQUOTE, migrate = true)

    @Singleton
    @FortuneDataStore
    @Provides
    fun provideFortuneDataStore(@ApplicationContext context: Context): DataStoreDelegate =
        context.getDataStoreDelegate(FortunePrefKeys.PREF_FORTUNE, migrate = true)

    @Singleton
    @HitokotoDataStore
    @Provides
    fun provideHitokotoDataStore(@ApplicationContext context: Context): DataStoreDelegate =
        context.getDataStoreDelegate(HitokotoPrefKeys.PREF_HITOKOTO, migrate = true)

    @Singleton
    @JinrishiciDataStore
    @Provides
    fun provideJinrishiciDataStore(@ApplicationContext context: Context): DataStoreDelegate =
        context.getDataStoreDelegate(JinrishiciQuoteModule.PREF_JINRISHICI, migrate = true)

    @Singleton
    @OpenAIDataStore
    @Provides
    fun provideOpenAIDataStore(@ApplicationContext context: Context): DataStoreDelegate =
        context.getDataStoreDelegate(OpenAIPrefKeys.PREF_OPENAI, migrate = true)
}