package com.crossbowffs.quotelock.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import com.crossbowffs.quotelock.BuildConfig;
import com.crossbowffs.quotelock.app.QuoteDownloaderTask;
import com.crossbowffs.quotelock.collection.provider.QuoteCollectionContract;
import com.crossbowffs.quotelock.consts.PrefKeys;

import java.util.Objects;

public class ActionProvider extends ContentProvider {

    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".action";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    private final UriMatcher mUriMatcher;

    public ActionProvider() {
        this(AUTHORITY);
    }

    public ActionProvider(String authority) {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(authority, "refresh", 1);
        mUriMatcher.addURI(authority, "collect", 2);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int matchCode = mUriMatcher.match(uri);
        if (matchCode != 1) {
            throw new IllegalArgumentException("Invalid query URI: " + uri);
        }
        new QuoteDownloaderTask(getContext()).execute();
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int matchCode = mUriMatcher.match(uri);
        if (matchCode < 2) {
            throw new IllegalArgumentException("Invalid insert URI: " + uri);
        }
        Uri newUri = collectQuote(values);
        if (!Objects.equals(newUri.getLastPathSegment(), "-1")) {
            getContext().getSharedPreferences(PrefKeys.PREF_QUOTES, Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean(PrefKeys.PREF_QUOTES_COLLECTION_STATE, true)
                    .apply();
        }
        return newUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int matchCode = mUriMatcher.match(uri);
        if (matchCode < 2) {
            throw new IllegalArgumentException("Invalid delete URI: " + uri);
        }
        int result = deleteCollectedQuote(selection);
        if (result >= 0) {
            getContext().getSharedPreferences(PrefKeys.PREF_QUOTES, Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean(PrefKeys.PREF_QUOTES_COLLECTION_STATE, false)
                    .apply();
        }
        return result;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    private Uri collectQuote(ContentValues values) {
        ContentResolver resolver = getContext().getContentResolver();
        return resolver.insert(QuoteCollectionContract.Collection.CONTENT_URI, values);
    }

    private int deleteCollectedQuote(String selection) {
        return getContext().getContentResolver().delete(QuoteCollectionContract.Collection.CONTENT_URI,
                selection, null);
    }
}
