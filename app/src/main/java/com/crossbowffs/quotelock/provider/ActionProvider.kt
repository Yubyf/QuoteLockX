package com.crossbowffs.quotelock.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.crossbowffs.quotelock.BuildConfig
import com.crossbowffs.quotelock.app.QuoteDownloaderTask
import com.crossbowffs.quotelock.collections.provider.QuoteCollectionContract
import com.crossbowffs.quotelock.consts.PREF_QUOTES_COLLECTION_STATE
import com.crossbowffs.quotelock.data.quotesDataStore

class ActionProvider @JvmOverloads constructor(authority: String? = AUTHORITY) : ContentProvider() {

    private val mUriMatcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH)

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?,
    ): Cursor? {
        val matchCode = mUriMatcher.match(uri)
        require(matchCode == 1) { "Invalid query URI: $uri" }
        QuoteDownloaderTask(context!!).execute()
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val matchCode = mUriMatcher.match(uri)
        require(matchCode >= 2) { "Invalid insert URI: $uri" }
        val newUri = collectQuote(values)
        if (newUri?.lastPathSegment != "-1") {
            quotesDataStore.putBoolean(PREF_QUOTES_COLLECTION_STATE, true)
        }
        return newUri
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val matchCode = mUriMatcher.match(uri)
        require(matchCode >= 2) { "Invalid delete URI: $uri" }
        val result = deleteCollectedQuote(selection)
        if (result >= 0) {
            quotesDataStore.putBoolean(PREF_QUOTES_COLLECTION_STATE, false)
        }
        return result
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?,
    ): Int {
        return 0
    }

    private fun collectQuote(values: ContentValues?): Uri? {
        val resolver = context!!.contentResolver
        return resolver.insert(QuoteCollectionContract.Collections.CONTENT_URI, values)
    }

    private fun deleteCollectedQuote(selection: String?): Int {
        return context!!.contentResolver.delete(QuoteCollectionContract.Collections.CONTENT_URI,
            selection, null)
    }

    companion object {
        const val AUTHORITY = BuildConfig.APPLICATION_ID + ".action"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY")
    }

    init {
        mUriMatcher.addURI(authority, "refresh", 1)
        mUriMatcher.addURI(authority, "collect", 2)
    }
}