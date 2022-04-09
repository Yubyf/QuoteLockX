package com.crossbowffs.quotelock.modules.fortune

import android.content.ComponentName
import android.content.Context
import com.crossbowffs.quotelock.api.QuoteData
import com.crossbowffs.quotelock.api.QuoteModule
import com.crossbowffs.quotelock.api.QuoteModule.Companion.CHARACTER_TYPE_DEFAULT
import com.crossbowffs.quotelock.app.App
import com.crossbowffs.quotelock.modules.fortune.app.FortuneConfigActivity
import com.crossbowffs.quotelock.modules.fortune.consts.FortunePrefKeys
import com.crossbowffs.quotelock.modules.fortune.consts.FortunePrefKeys.PREF_FORTUNE_DEFAULT_CATEGORY
import com.crossbowffs.quotelock.modules.fortune.database.fortuneQuoteDatabase
import com.yubyf.datastore.DataStoreDelegate.Companion.getDataStoreDelegate
import com.yubyf.quotelockx.R
import kotlinx.coroutines.flow.firstOrNull
import java.io.IOException

internal val fortuneDataStore =
    App.INSTANCE.getDataStoreDelegate(FortunePrefKeys.PREF_FORTUNE, migrate = true)

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
        val type =
            fortuneDataStore.getStringSuspend(FortunePrefKeys.PREF_FORTUNE_CATEGORY_STRING,
                PREF_FORTUNE_DEFAULT_CATEGORY)!!
        return if (type == PREF_FORTUNE_DEFAULT_CATEGORY) {
            fortuneQuoteDatabase.dao().getRandomItem().firstOrNull()
        } else {
            fortuneQuoteDatabase.dao().getRandomItemByCategory(type).firstOrNull()
        }?.let { QuoteData(it.text, it.source) } ?: QuoteData()
    }

    override val characterType: Int
        get() = CHARACTER_TYPE_DEFAULT
}