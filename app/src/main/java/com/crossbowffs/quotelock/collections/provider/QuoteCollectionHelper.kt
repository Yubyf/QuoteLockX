package com.crossbowffs.quotelock.collections.provider

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.crossbowffs.quotelock.BuildConfig

/**
 * @author Yubyf
 */
class QuoteCollectionHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_QUOTE_COLLECTIONS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    companion object {
        const val DATABASE_NAME = "quote_collections.db"
        private const val DATABASE_VERSION = BuildConfig.QUOTE_COLLECTIONS_DB_VERSION
        private const val CREATE_QUOTE_COLLECTIONS_TABLE =
            "CREATE TABLE " + QuoteCollectionContract.Collections.TABLE + "(" +
                    QuoteCollectionContract.Collections.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    QuoteCollectionContract.Collections.MD5 + " TEXT UNIQUE NOT NULL, " +
                    QuoteCollectionContract.Collections.TEXT + " TEXT NOT NULL, " +
                    QuoteCollectionContract.Collections.SOURCE + " TEXT NOT NULL" +
                    ");"
    }
}