package com.crossbowffs.quotelock.di

import com.crossbowffs.quotelock.data.modules.collections.QuoteCollectionRepository
import com.crossbowffs.quotelock.data.modules.custom.CustomQuoteRepository
import com.crossbowffs.quotelock.data.modules.fortune.database.FortuneQuoteDatabase
import com.yubyf.datastore.DataStoreDelegate
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface QuoteModuleEntryPoint {
    fun collectionRepository(): QuoteCollectionRepository

    @BrainyDataStore
    fun brainyDataStore(): DataStoreDelegate

    fun customQuoteRepository(): CustomQuoteRepository

    @FortuneDataStore
    fun fortuneDataStore(): DataStoreDelegate

    fun fortuneDatabase(): FortuneQuoteDatabase

    @HitokotoDataStore
    fun hitokotoDataStore(): DataStoreDelegate

    @JinrishiciDataStore
    fun jinrishiciDataStore(): DataStoreDelegate
}