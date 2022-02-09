package com.crossbowffs.quotelock.history.provider

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.crossbowffs.quotelock.BuildConfig

/**
 * @author Yubyf
 */
class QuoteHistoryHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_QUOTE_HISTORIES_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    companion object {
        const val DATABASE_NAME = "quote_histories.db"
        private const val DATABASE_VERSION = BuildConfig.QUOTE_COLLECTIONS_DB_VERSION
        private const val CREATE_QUOTE_HISTORIES_TABLE =
            "CREATE TABLE " + QuoteHistoryContract.Histories.TABLE + "(" +
                    QuoteHistoryContract.Histories.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    QuoteHistoryContract.Histories.MD5 + " TEXT UNIQUE NOT NULL, " +
                    QuoteHistoryContract.Histories.TEXT + " TEXT NOT NULL, " +
                    QuoteHistoryContract.Histories.SOURCE + " TEXT NOT NULL" +
                    ");"
    }
}