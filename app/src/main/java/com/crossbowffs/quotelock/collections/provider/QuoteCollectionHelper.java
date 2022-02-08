package com.crossbowffs.quotelock.collections.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.crossbowffs.quotelock.BuildConfig;

/**
 * @author Yubyf
 */
public class QuoteCollectionHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "quote_collections.db";
    private static final int DATABASE_VERSION = BuildConfig.QUOTE_COLLECTIONS_DB_VERSION;

    private static final String CREATE_QUOTE_COLLECTIONS_TABLE =
            "CREATE TABLE " + QuoteCollectionContract.Collections.TABLE + "(" +
                    QuoteCollectionContract.Collections._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    QuoteCollectionContract.Collections.MD5 + " TEXT UNIQUE NOT NULL, " +
                    QuoteCollectionContract.Collections.TEXT + " TEXT NOT NULL, " +
                    QuoteCollectionContract.Collections.SOURCE + " TEXT NOT NULL" +
                    ");";

    public QuoteCollectionHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_QUOTE_COLLECTIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
