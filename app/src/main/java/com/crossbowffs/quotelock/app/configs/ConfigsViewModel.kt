package com.crossbowffs.quotelock.app.configs

import androidx.lifecycle.ViewModel
import com.crossbowffs.quotelock.app.configs.brainyquote.BrainyQuotePrefKeys.PREF_BRAINY_TYPE_INT
import com.crossbowffs.quotelock.app.configs.brainyquote.BrainyQuotePrefKeys.PREF_BRAINY_TYPE_STRING
import com.crossbowffs.quotelock.app.configs.fortune.FortunePrefKeys.PREF_FORTUNE_CATEGORY_INT
import com.crossbowffs.quotelock.app.configs.fortune.FortunePrefKeys.PREF_FORTUNE_CATEGORY_STRING
import com.crossbowffs.quotelock.app.configs.hitokoto.HitokotoPrefKeys.PREF_HITOKOTO_LEGACY_TYPE_INT
import com.crossbowffs.quotelock.app.configs.hitokoto.HitokotoPrefKeys.PREF_HITOKOTO_TYPES_STRING
import com.crossbowffs.quotelock.di.BrainyDataStore
import com.crossbowffs.quotelock.di.HitokotoDataStore
import com.yubyf.datastore.DataStoreDelegate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class ConfigsViewModel @Inject constructor(
    @HitokotoDataStore private val hitokotoDataStore: DataStoreDelegate,
    @BrainyDataStore private val brainyDataStore: DataStoreDelegate,
    @BrainyDataStore private val fortuneDataStore: DataStoreDelegate,
) : ViewModel() {

    fun loadHitokotoTypeIndex() = runBlocking {
        hitokotoDataStore.getIntSuspend(PREF_HITOKOTO_LEGACY_TYPE_INT, -1)
    }

    fun loadHitokotoTypesString() = runBlocking {
        hitokotoDataStore.getStringSetSuspend(PREF_HITOKOTO_TYPES_STRING)
    }

    fun selectHitokotoTypes(types: Set<String>) {
        hitokotoDataStore.put(PREF_HITOKOTO_TYPES_STRING, types)
    }

    fun loadBrainyQuoteTypeIndex() = runBlocking {
        brainyDataStore.getIntSuspend(PREF_BRAINY_TYPE_INT, 0)
    }

    fun selectBrainyQuoteType(index: Int, type: String) {
        brainyDataStore.put(PREF_BRAINY_TYPE_INT, index)
        brainyDataStore.put(PREF_BRAINY_TYPE_STRING, type)
    }

    fun loadFortuneCategoryIndex() = runBlocking {
        fortuneDataStore.getIntSuspend(PREF_FORTUNE_CATEGORY_INT, 0)
    }

    fun selectFortuneCategory(index: Int, type: String) {
        fortuneDataStore.put(PREF_FORTUNE_CATEGORY_INT, index)
        fortuneDataStore.put(PREF_FORTUNE_CATEGORY_STRING, type)
    }
}