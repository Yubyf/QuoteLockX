package com.crossbowffs.quotelock.provider;

import android.app.job.JobParameters;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.crossbowffs.quotelock.BuildConfig;
import com.crossbowffs.quotelock.api.QuoteData;
import com.crossbowffs.quotelock.app.QuoteDownloaderService;
import com.crossbowffs.quotelock.app.QuoteDownloaderTask;
import com.crossbowffs.quotelock.utils.JobUtils;

import java.util.ArrayList;

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
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int matchCode = mUriMatcher.match(uri);
        if (matchCode < 0) {
            throw new IllegalArgumentException("Invalid query URI: " + uri);
        }
        if (matchCode == 1) {
            new QuoteDownloaderTask(getContext()).execute();
        }
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
