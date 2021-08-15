package com.crossbowffs.quotelock.collections.provider;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import com.crossbowffs.quotelock.account.SyncAccountManager;
import com.crossbowffs.quotelock.utils.Xlog;

/**
 * @author Yubyf
 * @date 2021/6/20.
 */
public class QuoteCollectionObserver extends ContentObserver {
    private static final String TAG = QuoteCollectionObserver.class.getSimpleName();

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public QuoteCollectionObserver(Handler handler) {
        super(handler);
    }

    /**
     * Define a method that's called when data in the
     * observed content provider changes.
     * This method signature is provided for compatibility with
     * older platforms.
     */
    @Override
    public void onChange(boolean selfChange) {
        /*
         * Invoke the method signature available as of
         * Android platform version 4.1, with a null URI.
         */
        onChange(selfChange, null);
    }

    /**
     * Define a method that's called when data in the
     * observed content provider changes.
     */
    @Override
    public void onChange(boolean selfChange, Uri changeUri) {
        /*
         * Ask the framework to run sync adapter.
         * To maintain backward compatibility, assume that
         * changeUri is null.
         */
        Xlog.d(TAG, "Data on change, requesting sync...");
        ContentResolver.requestSync(SyncAccountManager.getInstance().getCurrentSyncAccount(),
                QuoteCollectionContract.AUTHORITY, new Bundle());
    }
}
