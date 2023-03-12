@file:Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER")

package com.crossbowffs.quotelock.data.history

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.crossbowffs.quotelock.data.SAMPLE_AUTHOR
import com.crossbowffs.quotelock.data.SAMPLE_ID
import com.crossbowffs.quotelock.data.SAMPLE_SOURCE
import com.crossbowffs.quotelock.data.SAMPLE_TEXT
import com.crossbowffs.quotelock.data.SAMPLE_UID
import com.crossbowffs.quotelock.data.TEST_DB
import com.crossbowffs.quotelock.data.api.QuoteEntityContract
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * @author Yubyf
 * @date 2023/3/12.
 */
@RunWith(AndroidJUnit4::class)
class QuoteHistoryDatabaseMigrationTest {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        QuoteHistoryDatabase::class.java,
        emptyList()
    )

    @Test
    @Throws(IOException::class)
    fun testMigrate2to3() {
        var db = helper.createDatabase(TEST_DB, 2).apply {
            execSQL(
                "INSERT INTO ${QuoteHistoryContract.TABLE} " +
                        "(${QuoteHistoryContract.ID}, ${QuoteHistoryContract.MD5}, " +
                        "${QuoteHistoryContract.TEXT}, ${QuoteHistoryContract.SOURCE}) " +
                        "VALUES (${SAMPLE_ID}, '${SAMPLE_UID}', '${SAMPLE_TEXT}', '${SAMPLE_SOURCE}')"
            )
            close()
        }

        db = helper.runMigrationsAndValidate(
            TEST_DB,
            3,
            true,
            QuoteHistoryDatabase.MIGRATION_2_3
        )

        db.query("SELECT * FROM ${QuoteHistoryContract.TABLE}").use { cursor ->
            assertEquals(cursor.count, 1)
            cursor.moveToFirst()
            assertArrayEquals(
                cursor.columnNames,
                arrayOf(
                    QuoteHistoryContract.ID,
                    QuoteHistoryContract.MD5,
                    QuoteHistoryContract.TEXT,
                    QuoteHistoryContract.SOURCE,
                    QuoteEntityContract.AUTHOR_OLD
                )
            )
            assertEquals(cursor.getInt(0), SAMPLE_ID)
            assertEquals(cursor.getString(1), SAMPLE_UID)
            assertEquals(cursor.getString(2), SAMPLE_TEXT)
            assertEquals(cursor.getString(3), SAMPLE_SOURCE)
            assertEquals(cursor.getString(4), "")
        }
    }

    @Test
    @Throws(IOException::class)
    fun testMigrate3to4() {
        var db = helper.createDatabase(TEST_DB, 3).apply {
            execSQL(
                "INSERT INTO ${QuoteHistoryContract.TABLE} " +
                        "(${QuoteHistoryContract.ID}, ${QuoteHistoryContract.MD5}, " +
                        "${QuoteHistoryContract.TEXT}, ${QuoteHistoryContract.SOURCE}, " +
                        "${QuoteEntityContract.AUTHOR_OLD}) " +
                        "VALUES (${SAMPLE_ID}, '${SAMPLE_UID}', '${SAMPLE_TEXT}', " +
                        "'${SAMPLE_SOURCE}', '${SAMPLE_AUTHOR}')"
            )
            close()
        }

        db = helper.runMigrationsAndValidate(
            TEST_DB,
            4,
            true,
            QuoteHistoryDatabase.MIGRATION_3_4
        )

        db.query("SELECT * FROM ${QuoteHistoryContract.TABLE}").use { cursor ->
            assertEquals(cursor.count, 1)
            cursor.moveToFirst()
            assertArrayEquals(
                cursor.columnNames,
                arrayOf(
                    QuoteHistoryContract.ID,
                    QuoteHistoryContract.TEXT,
                    QuoteHistoryContract.SOURCE,
                    QuoteHistoryContract.MD5,
                    QuoteHistoryContract.AUTHOR
                )
            )
            assertEquals(cursor.getInt(0), SAMPLE_ID)
            assertEquals(cursor.getString(1), SAMPLE_TEXT)
            assertEquals(cursor.getString(2), SAMPLE_SOURCE)
            assertEquals(cursor.getString(3), SAMPLE_UID)
            assertEquals(cursor.getString(4), SAMPLE_AUTHOR)
        }
    }

    @Test
    @Throws(IOException::class)
    fun testMigrate4to5() {
        var db = helper.createDatabase(TEST_DB, 4).apply {
            execSQL(
                "INSERT INTO ${QuoteHistoryContract.TABLE} " +
                        "(${QuoteHistoryContract.ID}, ${QuoteHistoryContract.MD5}, " +
                        "${QuoteHistoryContract.TEXT}, ${QuoteHistoryContract.SOURCE}, " +
                        "${QuoteHistoryContract.AUTHOR}) " +
                        "VALUES (${SAMPLE_ID}, '${SAMPLE_UID}', '${SAMPLE_TEXT}', " +
                        "'${SAMPLE_SOURCE}', '${SAMPLE_AUTHOR}')"
            )
            close()
        }

        db = helper.runMigrationsAndValidate(
            TEST_DB,
            5,
            true,
            QuoteHistoryDatabase.MIGRATION_4_5
        )

        db.query("SELECT * FROM ${QuoteHistoryContract.TABLE}").use { cursor ->
            assertEquals(cursor.count, 1)
            cursor.moveToFirst()
            assertArrayEquals(
                cursor.columnNames,
                arrayOf(
                    QuoteHistoryContract.ID,
                    QuoteHistoryContract.TEXT,
                    QuoteHistoryContract.SOURCE,
                    QuoteHistoryContract.AUTHOR,
                    QuoteHistoryContract.PROVIDER,
                    QuoteHistoryContract.UID,
                    QuoteHistoryContract.EXTRA
                )
            )
            assertEquals(cursor.getInt(0), SAMPLE_ID)
            assertEquals(cursor.getString(1), SAMPLE_TEXT)
            assertEquals(cursor.getString(2), SAMPLE_SOURCE)
            assertEquals(cursor.getString(3), SAMPLE_AUTHOR)
            assertEquals(cursor.getString(4), "")
            assertEquals(cursor.getString(5), SAMPLE_UID)
            assertNull(cursor.getBlob(6))
        }
    }
}