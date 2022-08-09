package com.crossbowffs.quotelock.data.modules.fortune

import android.content.ComponentName
import android.content.Context
import com.crossbowffs.quotelock.app.configs.fortune.FortuneConfigActivity
import com.crossbowffs.quotelock.app.configs.fortune.FortunePrefKeys
import com.crossbowffs.quotelock.app.configs.fortune.FortunePrefKeys.PREF_FORTUNE_DEFAULT_CATEGORY
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.data.api.QuoteModule
import com.crossbowffs.quotelock.data.api.QuoteModule.Companion.CHARACTER_TYPE_DEFAULT
import com.crossbowffs.quotelock.di.QuoteModuleEntryPoint
import com.yubyf.quotelockx.R
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.firstOrNull
import java.io.IOException

class FortuneQuoteModule : QuoteModule {
    override fun getDisplayName(context: Context): String {
        return context.getString(R.string.module_fortune_name)
    }

    override fun getConfigActivity(context: Context): ComponentName {
        return ComponentName(context, FortuneConfigActivity::class.java)
    }

    override fun getMinimumRefreshInterval(context: Context): Int {
        return 0
    }

    override fun requiresInternetConnectivity(context: Context): Boolean {
        return false
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(IOException::class)
    override suspend fun getQuote(context: Context): QuoteData {
        val dataStore =
            EntryPointAccessors.fromApplication<QuoteModuleEntryPoint>(context.applicationContext)
                .fortuneDataStore()
        val database = EntryPointAccessors.fromApplication<QuoteModuleEntryPoint>(
            context.applicationContext).fortuneDatabase()
        val type =
            dataStore.getStringSuspend(FortunePrefKeys.PREF_FORTUNE_CATEGORY_STRING,
                PREF_FORTUNE_DEFAULT_CATEGORY)!!
        return if (type == PREF_FORTUNE_DEFAULT_CATEGORY) {
            database.dao().getRandomItem().firstOrNull()
        } else {
            database.dao().getRandomItemByCategory(type).firstOrNull()
        }?.let { QuoteData(it.text, it.source) } ?: QuoteData()
    }

    override val characterType: Int
        get() = CHARACTER_TYPE_DEFAULT
}