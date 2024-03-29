package com.crossbowffs.quotelock.provider

import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.net.Uri
import com.crossbowffs.quotelock.consts.PREF_COMMON
import com.crossbowffs.quotelock.consts.PREF_QUOTES
import com.crossbowffs.remotepreferences.RemotePreferenceProvider
import com.yubyf.datastore.DataStorePreferences.Companion.getDataStorePreferences
import com.yubyf.quotelockx.BuildConfig

class PreferenceProvider : RemotePreferenceProvider(AUTHORITY, arrayOf(PREF_COMMON, PREF_QUOTES)) {

    override fun getSharedPreferences(context: Context, prefFileName: String): SharedPreferences {
        return context.getDataStorePreferences(prefFileName)
    }

    override fun checkAccess(prefName: String, prefKey: String, write: Boolean): Boolean {
        return !write
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?,
    ): Cursor? {
        return super.query(uri, projection, selection, selectionArgs, sortOrder)
    }

    companion object {
        const val AUTHORITY = BuildConfig.APPLICATION_ID + ".preferences"
    }
}