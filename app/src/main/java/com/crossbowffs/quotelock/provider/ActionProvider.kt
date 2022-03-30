package com.crossbowffs.quotelock.provider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.crossbowffs.quotelock.BuildConfig
import com.crossbowffs.quotelock.app.downloadQuote
import com.crossbowffs.quotelock.collections.database.QuoteCollectionContract
import com.crossbowffs.quotelock.collections.database.QuoteCollectionEntity
import com.crossbowffs.quotelock.collections.database.quoteCollectionDatabase
import com.crossbowffs.quotelock.consts.PREF_QUOTES_COLLECTION_STATE
import com.crossbowffs.quotelock.data.quotesDataStore
import com.crossbowffs.quotelock.utils.ioScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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
        context?.run { ioScope.launch { downloadQuote() } }
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val matchCode = mUriMatcher.match(uri)
        require(matchCode >= 2) { "Invalid insert URI: $uri" }
        val result = collectQuote(values)
        if (result != -1L) {
            quotesDataStore.putBoolean(PREF_QUOTES_COLLECTION_STATE, true)
        }
        return ContentUris.withAppendedId(uri, result ?: -1)
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val matchCode = mUriMatcher.match(uri)
        require(matchCode >= 2) { "Invalid delete URI: $uri" }
        val key = selection?.split("=")?.get(0)
        val value = selectionArgs?.get(0)
        if (key.isNullOrBlank() || key != QuoteCollectionContract.MD5 || value.isNullOrBlank()) {
            return -1
        }
        val result = deleteCollectedQuote(value)
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

    private fun collectQuote(values: ContentValues?): Long? {
        return values?.let {
            runBlocking {
                quoteCollectionDatabase.dao()
                    .insert(QuoteCollectionEntity(
                        text = it[QuoteCollectionContract.TEXT].toString(),
                        source = it[QuoteCollectionContract.SOURCE].toString(),
                        md5 = it[QuoteCollectionContract.MD5].toString(),
                    ))
            }
        }
    }

    private fun deleteCollectedQuote(md5: String): Int {
        return runBlocking {
            quoteCollectionDatabase.dao().delete(md5)
        }
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