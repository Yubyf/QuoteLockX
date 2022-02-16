package com.crossbowffs.quotelock.collections.provider

import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import com.crossbowffs.quotelock.account.SyncAccountManager
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.className

/**
 * @author Yubyf
 * @date 2021/6/20.
 */
class QuoteCollectionObserver
/**
 * Creates a content observer.
 *
 * @param handler The handler to run [.onChange] on, or null if none.
 */
    (handler: Handler?) : ContentObserver(handler) {
    /**
     * Define a method that's called when data in the
     * observed content provider changes.
     * This method signature is provided for compatibility with
     * older platforms.
     */
    override fun onChange(selfChange: Boolean) {
        /*
         * Invoke the method signature available as of
         * Android platform version 4.1, with a null URI.
         */
        onChange(selfChange, null)
    }

    /**
     * Define a method that's called when data in the
     * observed content provider changes.
     */
    override fun onChange(selfChange: Boolean, changeUri: Uri?) {
        /*
         * Ask the framework to run sync adapter.
         * To maintain backward compatibility, assume that
         * changeUri is null.
         */
        Xlog.d(TAG, "Data on change, requesting sync...")
        ContentResolver.requestSync(
            SyncAccountManager.instance.currentSyncAccount,
            QuoteCollectionContract.AUTHORITY, Bundle()
        )
    }

    companion object {
        private val TAG = className<QuoteCollectionObserver>()
    }
}