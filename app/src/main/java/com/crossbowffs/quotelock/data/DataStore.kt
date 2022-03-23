package com.crossbowffs.quotelock.data

import androidx.datastore.preferences.core.Preferences
import androidx.preference.PreferenceDataStore
import com.crossbowffs.quotelock.app.App
import com.crossbowffs.quotelock.consts.PREF_COMMON
import com.crossbowffs.quotelock.consts.PREF_QUOTES
import com.yubyf.datastore.DataStoreDelegate.Companion.getDataStoreDelegate
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking

/**
 * @author Yubyf
 */
class PreferenceDataStoreAdapter(name: String, migrate: Boolean = false) :
    PreferenceDataStore() {

    private val dataStoreDelegate = App.INSTANCE.getDataStoreDelegate(name, migrate = migrate)

    override fun putString(key: String, value: String?) = dataStoreDelegate.put(key, value)

    override fun putStringSet(key: String, values: MutableSet<String>?) =
        dataStoreDelegate.put(key, values)

    override fun putInt(key: String, value: Int) = dataStoreDelegate.put(key, value)

    override fun putLong(key: String, value: Long) = dataStoreDelegate.put(key, value)

    override fun putFloat(key: String, value: Float) = dataStoreDelegate.put(key, value)

    override fun putBoolean(key: String, value: Boolean) = dataStoreDelegate.put(key, value)

    override fun getString(key: String, defValue: String?): String? =
        runBlocking { dataStoreDelegate.getStringSuspend(key, defValue) }

    override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? =
        runBlocking { dataStoreDelegate.getStringSetSuspend(key, defValues) ?: emptySet() }

    override fun getInt(key: String, defValue: Int): Int =
        runBlocking { dataStoreDelegate.getIntSuspend(key, defValue) }

    override fun getLong(key: String, defValue: Long): Long =
        runBlocking { dataStoreDelegate.getLongSuspend(key, defValue) }

    override fun getFloat(key: String, defValue: Float): Float =
        runBlocking { dataStoreDelegate.getFloatSuspend(key, defValue) }

    override fun getBoolean(key: String, defValue: Boolean): Boolean =
        runBlocking { dataStoreDelegate.getBooleanSuspend(key, defValue) }

    fun bulkPut(map: Map<String, *>) {
        dataStoreDelegate.bulkPut(map)
    }

    fun remove(key: String) = dataStoreDelegate.remove(key)

    fun collect(collector: suspend (Preferences, Preferences.Key<*>?) -> Unit): Job =
        dataStoreDelegate.collect(collector)

    suspend fun collectSuspend(collector: suspend (Preferences, Preferences.Key<*>?) -> Unit) =
        dataStoreDelegate.collectSuspend(collector)

    fun contains(key: String): Boolean = runBlocking { dataStoreDelegate.containsSuspend(key) }
}

val commonDataStore = PreferenceDataStoreAdapter(PREF_COMMON, true)
val quotesDataStore = PreferenceDataStoreAdapter(PREF_QUOTES, true)