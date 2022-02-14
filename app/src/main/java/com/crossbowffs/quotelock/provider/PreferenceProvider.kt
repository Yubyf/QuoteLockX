package com.crossbowffs.quotelock.provider

import android.database.Cursor
import android.net.Uri
import com.crossbowffs.quotelock.BuildConfig
import com.crossbowffs.quotelock.consts.PREF_COMMON
import com.crossbowffs.quotelock.consts.PREF_QUOTES
import com.crossbowffs.remotepreferences.RemotePreferenceProvider

class PreferenceProvider :
    RemotePreferenceProvider(AUTHORITY, arrayOf(PREF_COMMON, PREF_QUOTES)) {

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