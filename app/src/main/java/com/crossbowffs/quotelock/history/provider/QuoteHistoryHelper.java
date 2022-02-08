package com.crossbowffs.quotelock.history.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.crossbowffs.quotelock.BuildConfig;

/**
 * @author Yubyf
 */
public class QuoteHistoryHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "quote_histories.db";
    private static final int DATABASE_VERSION = BuildConfig.QUOTE_COLLECTIONS_DB_VERSION;

    private static final String CREATE_QUOTE_HISTORIES_TABLE =
            "CREATE TABLE " + QuoteHistoryContract.Histories.TABLE + "(" +
                    QuoteHistoryContract.Histories._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    QuoteHistoryContract.Histories.MD5 + " TEXT UNIQUE NOT NULL, " +
                    QuoteHistoryContract.Histories.TEXT + " TEXT NOT NULL, " +
                    QuoteHistoryContract.Histories.SOURCE + " TEXT NOT NULL" +
                    ");";

    public QuoteHistoryHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_QUOTE_HISTORIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
