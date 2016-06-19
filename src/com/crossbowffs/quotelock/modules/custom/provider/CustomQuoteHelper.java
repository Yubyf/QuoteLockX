package com.crossbowffs.quotelock.modules.custom.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CustomQuoteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "custom_quotes.db";
    private static final int DATABASE_VERSION = 1;

    private static final String CREATE_QUOTES_TABLE =
        "CREATE TABLE " + CustomQuoteContract.Quotes.TABLE + "(" +
            CustomQuoteContract.Quotes._ID        + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            CustomQuoteContract.Quotes.TEXT       + " TEXT NOT NULL, " +
            CustomQuoteContract.Quotes.SOURCE     + " TEXT NOT NULL" +
        ");";

    public CustomQuoteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_QUOTES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
