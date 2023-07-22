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
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

const val COMMON_DATA_STORE = "COMMON_DATA_STORE"
const val VERSION_DATA_STORE = "VERSION_DATA_STORE"
const val QUOTES_DATA_STORE = "QUOTES_DATA_STORE"
const val CARD_STYLE_DATA_STORE = "CARD_STYLE_DATA_STORE"
const val BRAINY_DATA_STORE = "BRAINY_DATA_STORE"
const val WIKIQUOTE_DATA_STORE = "WIKIQUOTE_DATA_STORE"
const val FORTUNE_DATA_STORE = "FORTUNE_DATA_STORE"
const val HITOKOTO_DATA_STORE = "HITOKOTO_DATA_STORE"
const val JINRISHICI_DATA_STORE = "JINRISHICI_DATA_STORE"
const val OPENAI_DATA_STORE = "OPENAI_DATA_STORE"

@Module
class DataStoreModules {
    @Single
    @Named(COMMON_DATA_STORE)
    fun provideCommonDataStore(context: Context): DataStoreDelegate =
        context.getDataStoreDelegate(PREF_COMMON, migrate = true)

    @Single
    @Named(VERSION_DATA_STORE)
    fun provideVersionDataStore(context: Context): DataStoreDelegate =
        context.getDataStoreDelegate(PREF_VERSION_UPDATE, migrate = true)

    @Single
    @Named(QUOTES_DATA_STORE)
    fun provideQuotesDataStore(context: Context): DataStoreDelegate =
        context.getDataStoreDelegate(PREF_QUOTES, migrate = true)

    @Single
    @Named(CARD_STYLE_DATA_STORE)
    fun provideCardStyleDataStore(context: Context): DataStoreDelegate =
        context.getDataStoreDelegate(PREF_CARD_STYLE, migrate = true)

    @Single
    @Named(BRAINY_DATA_STORE)
    fun provideBrainyDataStore(context: Context): DataStoreDelegate =
        context.getDataStoreDelegate(BrainyQuotePrefKeys.PREF_BRAINY, migrate = true)

    @Single
    @Named(WIKIQUOTE_DATA_STORE)
    fun provideWikiquoteDataStore(context: Context): DataStoreDelegate =
        context.getDataStoreDelegate(WikiquotePrefKeys.PREF_WIKIQUOTE, migrate = true)

    @Single
    @Named(FORTUNE_DATA_STORE)
    fun provideFortuneDataStore(context: Context): DataStoreDelegate =
        context.getDataStoreDelegate(FortunePrefKeys.PREF_FORTUNE, migrate = true)

    @Single
    @Named(HITOKOTO_DATA_STORE)
    fun provideHitokotoDataStore(context: Context): DataStoreDelegate =
        context.getDataStoreDelegate(HitokotoPrefKeys.PREF_HITOKOTO, migrate = true)

    @Single
    @Named(JINRISHICI_DATA_STORE)
    fun provideJinrishiciDataStore(context: Context): DataStoreDelegate =
        context.getDataStoreDelegate(JinrishiciQuoteModule.PREF_JINRISHICI, migrate = true)

    @Single
    @Named(OPENAI_DATA_STORE)
    fun provideOpenAIDataStore(context: Context): DataStoreDelegate =
        context.getDataStoreDelegate(OpenAIPrefKeys.PREF_OPENAI, migrate = true)
}