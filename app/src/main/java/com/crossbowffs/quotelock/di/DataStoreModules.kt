package com.crossbowffs.quotelock.di

import android.content.Context
import com.crossbowffs.quotelock.app.configs.brainyquote.BrainyQuotePrefKeys
import com.crossbowffs.quotelock.app.configs.fortune.FortunePrefKeys
import com.crossbowffs.quotelock.app.configs.hitokoto.HitokotoPrefKeys
import com.crossbowffs.quotelock.consts.PREF_COMMON
import com.crossbowffs.quotelock.consts.PREF_QUOTES
import com.crossbowffs.quotelock.data.datastore.PreferenceDataStoreAdapter
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
annotation class CommonDataStoreAdapter

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class QuotesDataStore

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class QuotesDataStoreAdapter

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class BrainyDataStore

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class FortuneDataStore

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class HitokotoDataStore

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class JinrishiciDataStore

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModules {

    @Singleton
    @CommonDataStore
    @Provides
    fun provideCommonDataStore(@ApplicationContext context: Context): DataStoreDelegate =
        context.getDataStoreDelegate(PREF_COMMON, migrate = true)

    @Singleton
    @CommonDataStoreAdapter
    @Provides
    fun provideCommonDataStoreAdapter(@ApplicationContext context: Context): PreferenceDataStoreAdapter =
        PreferenceDataStoreAdapter(context, PREF_COMMON, true)

    @Singleton
    @QuotesDataStore
    @Provides
    fun provideQuotesDataStore(@ApplicationContext context: Context): DataStoreDelegate =
        context.getDataStoreDelegate(PREF_COMMON, migrate = true)

    @Singleton
    @QuotesDataStoreAdapter
    @Provides
    fun provideQuotesDataStoreAdapter(@ApplicationContext context: Context): PreferenceDataStoreAdapter =
        PreferenceDataStoreAdapter(context, PREF_QUOTES, true)

    @Singleton
    @BrainyDataStore
    @Provides
    fun provideBrainyDataStore(@ApplicationContext context: Context): DataStoreDelegate =
        context.getDataStoreDelegate(BrainyQuotePrefKeys.PREF_BRAINY, migrate = true)

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
}