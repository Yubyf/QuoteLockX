package com.crossbowffs.quotelock.modules.custom.provider

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.crossbowffs.quotelock.BuildConfig

class CustomQuoteHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_QUOTES_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    companion object {
        private const val DATABASE_NAME = "custom_quotes.db"
        private const val DATABASE_VERSION = BuildConfig.CUSTOM_QUOTES_DB_VERSION
        private const val CREATE_QUOTES_TABLE =
            "CREATE TABLE " + CustomQuoteContract.Quotes.TABLE + "(" +
                    CustomQuoteContract.Quotes.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    CustomQuoteContract.Quotes.TEXT + " TEXT NOT NULL, " +
                    CustomQuoteContract.Quotes.SOURCE + " TEXT NOT NULL" +
                    ");"
    }
}