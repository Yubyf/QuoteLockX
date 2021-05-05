package com.crossbowffs.quotelock.collection.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.crossbowffs.quotelock.BuildConfig;

/**
 * @author Yubyf
 */
public class QuoteCollectionHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "quote_collections.db";
    private static final int DATABASE_VERSION = BuildConfig.QUOTE_COLLECTIONS_DB_VERSION;

    private static final String CREATE_QUOTE_COLLECTIONS_TABLE =
            "CREATE TABLE " + QuoteCollectionContract.Collection.TABLE + "(" +
                    QuoteCollectionContract.Collection._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    QuoteCollectionContract.Collection.MD5 + " TEXT UNIQUE NOT NULL, " +
                    QuoteCollectionContract.Collection.TEXT + " TEXT NOT NULL, " +
                    QuoteCollectionContract.Collection.SOURCE + " TEXT NOT NULL" +
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
