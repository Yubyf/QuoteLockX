package com.crossbowffs.quotelock.provider

import android.content.*
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.provider.BaseColumns
import android.text.TextUtils

abstract class AutoContentProvider(authority: String?, private val mTables: Array<ProviderTable>) :
    ContentProvider() {

    class ProviderTable(val tableName: String, val itemType: String, val dirType: String)

    private val mUriMatcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH)
    private lateinit var mDatabaseHelper: SQLiteOpenHelper

    override fun onCreate(): Boolean {
        mDatabaseHelper = createDatabaseHelper(context!!)
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?,
    ): Cursor? {
        val matchCode = mUriMatcher.match(uri)
        require(matchCode >= 0) { "Invalid query URI: $uri" }
        val queryBuilder = SQLiteQueryBuilder()
        if (isItemUri(matchCode)) {
            queryBuilder.appendWhere("${BaseColumns._ID}=${uri.lastPathSegment}")
        } else if (isOtherUri(matchCode)) {
            val segments = uri.pathSegments
            queryBuilder.appendWhere("${segments[segments.size - 2]}='${uri.lastPathSegment}'")
        }
        queryBuilder.tables = getTableName(matchCode)
        val db = getDatabase(false)
        val cursor =
            queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder)
        cursor.setNotificationUri(context!!.contentResolver, uri)
        return cursor
    }

    override fun getType(uri: Uri): String? {
        val matchCode = mUriMatcher.match(uri)
        require(matchCode >= 0) { "Invalid URI: $uri" }
        return getType(matchCode)
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val matchCode = mUriMatcher.match(uri)
        require(!(matchCode < 0 || isItemUri(matchCode) || isOtherUri(matchCode))) { "Invalid insert URI: $uri" }
        val db = getDatabase(true)
        val row = db.insert(getTableName(matchCode), null, values)
        val newUri = ContentUris.withAppendedId(uri, row)
        if (row >= 0) {
            context!!.contentResolver.notifyChange(newUri, null)
        }
        return newUri
    }

    override fun bulkInsert(uri: Uri, bulkValues: Array<ContentValues>): Int {
        val matchCode = mUriMatcher.match(uri)
        require(!(matchCode < 0 || isItemUri(matchCode) || isOtherUri(matchCode))) { "Invalid insert URI: $uri" }
        val tableName = getTableName(matchCode)
        var successCount = 0
        val contentResolver = context!!.contentResolver
        val db = getDatabase(true)
        for (values in bulkValues) {
            val row = db.insert(tableName, null, values)
            if (row >= 0) {
                val newUri = ContentUris.withAppendedId(uri, row)
                contentResolver.notifyChange(newUri, null)
                successCount++
            }
        }
        return successCount
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        var combinedSelection = selection
        val matchCode = mUriMatcher.match(uri)
        require(matchCode >= 0) { "Invalid delete URI: $uri" }
        if (isItemUri(matchCode)) {
            combinedSelection = getCombinedSelectionString(BaseColumns._ID, uri, selection)
        }
        val db = getDatabase(true)
        val deletedRows = db.delete(getTableName(matchCode), combinedSelection, selectionArgs)
        if (combinedSelection == null || deletedRows > 0) {
            context!!.contentResolver.notifyChange(uri, null)
        }
        return deletedRows
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?,
    ): Int {
        var combinedSelection = selection
        val matchCode = mUriMatcher.match(uri)
        require(matchCode >= 0) { "Invalid update URI: $uri" }
        if (isItemUri(matchCode)) {
            combinedSelection = getCombinedSelectionString(BaseColumns._ID, uri, selection)
        }
        val db = getDatabase(true)
        val updatedRows =
            db.update(getTableName(matchCode), values, combinedSelection, selectionArgs)
        if (updatedRows > 0) {
            context!!.contentResolver.notifyChange(uri, null)
        }
        return updatedRows
    }

    @Throws(OperationApplicationException::class)
    override fun applyBatch(operations: ArrayList<ContentProviderOperation>): Array<ContentProviderResult?> {
        // Since we opened the database as writable, each call to the
        // content provider operations will reuse the same database instance.
        // This is technically taking advantage of an implementation detail -
        // it may be cleaner to create an overloaded version of the operation
        // methods that take the database as a parameter.
        val db = getDatabase(true)
        val results = arrayOfNulls<ContentProviderResult>(operations.size)
        db.beginTransaction()
        try {
            for (i in results.indices) {
                results[i] = operations[i].apply(this, results, i)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        return results
    }

    private fun isItemUri(matchCode: Int): Boolean {
        return matchCode % 3 == 1
    }

    private fun isOtherUri(matchCode: Int): Boolean {
        return matchCode % 3 == 2
    }

    private fun getTableName(matchCode: Int): String {
        return mTables[matchCode / 3].tableName
    }

    private fun getType(matchCode: Int): String {
        val table = mTables[matchCode / 3]
        return if (isItemUri(matchCode)) {
            table.itemType
        } else {
            table.dirType
        }
    }

    protected abstract fun createDatabaseHelper(context: Context): SQLiteOpenHelper

    private fun getDatabase(write: Boolean): SQLiteDatabase {
        return if (write) {
            mDatabaseHelper.writableDatabase
        } else {
            mDatabaseHelper.readableDatabase
        }
    }

    companion object {
        private fun getCombinedSelectionString(
            idColumnName: String,
            uri: Uri,
            selection: String?,
        ): String {
            val profileWhere = idColumnName + "=" + uri.lastPathSegment
            return if (TextUtils.isEmpty(selection)) {
                profileWhere
            } else {
                "$profileWhere AND $selection"
            }
        }
    }

    init {
        for (i in mTables.indices) {
            val table = mTables[i].tableName
            mUriMatcher.addURI(authority, table, i * 3)
            mUriMatcher.addURI(authority, "$table/#", i * 3 + 1)
            mUriMatcher.addURI(authority, "$table/*/*", i * 3 + 2)
        }
    }
}