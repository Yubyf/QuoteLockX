package com.crossbowffs.quotelock.app

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import android.text.TextUtils
import com.crossbowffs.quotelock.api.QuoteData
import com.crossbowffs.quotelock.api.QuoteModule
import com.crossbowffs.quotelock.app.QuoteDownloaderTask
import com.crossbowffs.quotelock.collections.provider.QuoteCollectionContract
import com.crossbowffs.quotelock.consts.*
import com.crossbowffs.quotelock.history.provider.QuoteHistoryContract
import com.crossbowffs.quotelock.modules.ModuleManager
import com.crossbowffs.quotelock.modules.ModuleNotFoundException
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.md5

open class QuoteDownloaderTask(protected val mContext: Context) :
    AsyncTask<Void?, Void?, QuoteData?>() {
    private val mModuleName: String

    override fun doInBackground(vararg params: Void?): QuoteData? {
        Xlog.d(TAG, "Attempting to download new quote...")
        val module: QuoteModule = try {
            ModuleManager.getModule(mContext, mModuleName)
        } catch (e: ModuleNotFoundException) {
            Xlog.e(TAG, "Selected module not found", e)
            return null
        }
        Xlog.d(TAG, "Provider: %s", module.getDisplayName(mContext)!!)
        return try {
            module.getQuote(mContext)
        } catch (e: Exception) {
            Xlog.e(TAG, "Quote download failed", e)
            null
        }
    }

    override fun onPostExecute(quote: QuoteData?) {
        Xlog.d(TAG, "QuoteDownloaderTask#onPostExecute called, success: %s", quote != null)
        if (quote != null) {
            Xlog.d(TAG, "Text: %s", quote.quoteText)
            Xlog.d(TAG, "Source: %s", quote.quoteSource)
            insertQuoteHistory(quote.quoteText, quote.quoteSource)
            val collectionState = queryQuoteCollectionState(quote.quoteText,
                if (TextUtils.isEmpty(quote.quoteSource)) "" else quote.quoteSource.replace("―", "")
                    .trim())
            mContext.getSharedPreferences(PREF_QUOTES, Context.MODE_PRIVATE)
                .edit()
                .putString(PREF_QUOTES_TEXT, quote.quoteText)
                .putString(PREF_QUOTES_SOURCE, quote.quoteSource)
                .putBoolean(PREF_QUOTES_COLLECTION_STATE, collectionState)
                .putLong(PREF_QUOTES_LAST_UPDATED, System.currentTimeMillis())
                .apply()
        }
    }

    private fun queryQuoteCollectionState(text: String, source: String): Boolean {
        val uri =
            Uri.withAppendedPath(Uri.withAppendedPath(QuoteCollectionContract.Collections.CONTENT_URI,
                QuoteCollectionContract.Collections.MD5), (text + source).md5())
        val columns = arrayOf(QuoteCollectionContract.Collections.ID)
        mContext.contentResolver.query(uri, columns, null, null, null)
            .use { cursor ->
                return if (cursor != null && cursor.moveToFirst()) {
                    cursor.getInt(0) > 0
                } else {
                    false
                }
            }
    }

    private fun insertQuoteHistory(text: String, source: String) {
        val values = ContentValues(3)
        values.put(QuoteHistoryContract.Histories.TEXT, text)
        values.put(QuoteHistoryContract.Histories.SOURCE, source)
        values.put(QuoteHistoryContract.Histories.MD5, (text + source).md5())
        mContext.contentResolver.insert(QuoteHistoryContract.Histories.CONTENT_URI, values)
    }

    override fun onCancelled(quoteData: QuoteData?) {
        Xlog.d(TAG, "QuoteDownloaderTask#onCancelled called")
    }

    companion object {
        private val TAG = QuoteDownloaderTask::class.simpleName
    }

    init {
        val preferences = mContext.getSharedPreferences(PREF_COMMON, Context.MODE_PRIVATE)
        mModuleName =
            preferences.getString(PREF_COMMON_QUOTE_MODULE, PREF_COMMON_QUOTE_MODULE_DEFAULT)!!
    }
}