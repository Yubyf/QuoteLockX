package com.crossbowffs.quotelock.provider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.crossbowffs.quotelock.data.modules.QuoteRepository
import com.crossbowffs.quotelock.data.modules.collections.QuoteCollectionRepository
import com.crossbowffs.quotelock.data.modules.collections.database.QuoteCollectionContract
import com.crossbowffs.quotelock.data.modules.collections.database.QuoteCollectionEntity
import com.crossbowffs.quotelock.di.QuoteModuleEntryPoint
import com.crossbowffs.quotelock.di.QuoteProviderEntryPoint
import com.crossbowffs.quotelock.utils.Xlog
import com.yubyf.quotelockx.BuildConfig
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ActionProvider @JvmOverloads constructor(authority: String? = AUTHORITY) : ContentProvider() {

    private val mUriMatcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH)

    private val quoteRepository: QuoteRepository by lazy {
        EntryPointAccessors.fromApplication(context!!.applicationContext,
            QuoteProviderEntryPoint::class.java).quoteRepository()
    }

    private val collectionRepository: QuoteCollectionRepository by lazy {
        EntryPointAccessors.fromApplication<QuoteModuleEntryPoint>(context!!.applicationContext)
            .collectionRepository()
    }

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
        CoroutineScope(Dispatchers.Default).launch { quoteRepository.downloadQuote() }
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri {
        val matchCode = mUriMatcher.match(uri)
        require(matchCode >= 2) { "Invalid insert URI: $uri" }
        val result = collectQuote(values)
        return ContentUris.withAppendedId(uri, result ?: -1)
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val matchCode = mUriMatcher.match(uri)
        require(matchCode >= 2) { "Invalid delete URI: $uri" }
        val key = selection?.split("=")?.get(0)
        val value = selectionArgs?.get(0)
        if (key.isNullOrBlank() || key != QuoteCollectionContract.UID || value.isNullOrBlank()) {
            return -1
        }
        return deleteCollectedQuote(value)
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?,
    ): Int {
        return 0
    }

    private fun collectQuote(values: ContentValues?): Long? = values?.let {
        runBlocking {
            Xlog.d("LockscreenHook", "Quote extra: ${it[QuoteCollectionContract.EXTRA]}")
            collectionRepository.insert(
                QuoteCollectionEntity(
                    text = it[QuoteCollectionContract.TEXT].toString(),
                    source = it[QuoteCollectionContract.SOURCE].toString(),
                    author = it[QuoteCollectionContract.AUTHOR].toString(),
                    provider = it[QuoteCollectionContract.PROVIDER].toString(),
                    uid = it[QuoteCollectionContract.UID].toString(),
                    extra = it[QuoteCollectionContract.EXTRA] as? ByteArray
                )
            )
        }
    }

    private fun deleteCollectedQuote(md5: String): Int = runBlocking {
        collectionRepository.delete(md5)
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