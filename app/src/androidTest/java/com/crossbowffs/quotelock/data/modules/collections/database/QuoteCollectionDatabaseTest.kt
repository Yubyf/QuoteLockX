@file:Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER")

package com.crossbowffs.quotelock.data.modules.collections.database

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
class QuoteCollectionDatabaseTest {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        QuoteCollectionDatabase::class.java,
        emptyList()
    )

    @Test
    @Throws(IOException::class)
    fun testMigrate2to3() {
        var db = helper.createDatabase(TEST_DB, 2).apply {
            execSQL(
                "INSERT INTO ${QuoteCollectionContract.TABLE} " +
                        "(${QuoteCollectionContract.ID}, ${QuoteCollectionContract.LEGACY_UID}, " +
                        "${QuoteCollectionContract.TEXT}, ${QuoteCollectionContract.SOURCE}) " +
                        "VALUES ($SAMPLE_ID, '$SAMPLE_UID', '$SAMPLE_TEXT', '$SAMPLE_SOURCE')"
            )
            close()
        }

        db = helper.runMigrationsAndValidate(
            TEST_DB,
            3,
            true,
            QuoteCollectionDatabase.MIGRATION_2_3
        )

        db.query("SELECT * FROM ${QuoteCollectionContract.TABLE}").use { cursor ->
            assertEquals(cursor.count, 1)
            cursor.moveToFirst()
            assertArrayEquals(
                cursor.columnNames,
                arrayOf(
                    QuoteCollectionContract.ID,
                    QuoteCollectionContract.LEGACY_UID,
                    QuoteCollectionContract.TEXT,
                    QuoteCollectionContract.SOURCE,
                    QuoteEntityContract.AUTHOR_OLD
                )
            )
            assertEquals(cursor.getInt(0), SAMPLE_ID)
            assertEquals(cursor.getString(1), SAMPLE_UID)
            assertEquals(cursor.getString(2), SAMPLE_TEXT)
            assertEquals(cursor.getString(3), SAMPLE_SOURCE)
            assertNull(cursor.getString(4))
        }
    }

    @Test
    @Throws(IOException::class)
    fun testMigrate3to4() {
        var db = helper.createDatabase(TEST_DB, 3).apply {
            execSQL(
                "INSERT INTO ${QuoteCollectionContract.TABLE} " +
                        "(${QuoteCollectionContract.ID}, ${QuoteCollectionContract.LEGACY_UID}, " +
                        "${QuoteCollectionContract.TEXT}, ${QuoteCollectionContract.SOURCE}, " +
                        "${QuoteEntityContract.AUTHOR_OLD}) " +
                        "VALUES ($SAMPLE_ID, '$SAMPLE_UID', '$SAMPLE_TEXT', " +
                        "'$SAMPLE_SOURCE', '$SAMPLE_AUTHOR')"
            )
            close()
        }

        db = helper.runMigrationsAndValidate(
            TEST_DB,
            4,
            true,
            QuoteCollectionDatabase.MIGRATION_3_4
        )

        db.query("SELECT * FROM ${QuoteCollectionContract.TABLE}").use { cursor ->
            assertEquals(cursor.count, 1)
            cursor.moveToFirst()
            assertArrayEquals(
                cursor.columnNames,
                arrayOf(
                    QuoteCollectionContract.ID,
                    QuoteCollectionContract.TEXT,
                    QuoteCollectionContract.SOURCE,
                    QuoteCollectionContract.LEGACY_UID,
                    QuoteCollectionContract.AUTHOR
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
                "INSERT INTO ${QuoteCollectionContract.TABLE} " +
                        "(${QuoteCollectionContract.ID}, ${QuoteCollectionContract.LEGACY_UID}, " +
                        "${QuoteCollectionContract.TEXT}, ${QuoteCollectionContract.SOURCE}, " +
                        "${QuoteCollectionContract.AUTHOR}) " +
                        "VALUES ($SAMPLE_ID, '$SAMPLE_UID', '$SAMPLE_TEXT', " +
                        "'$SAMPLE_SOURCE', '$SAMPLE_AUTHOR')"
            )
            close()
        }

        db = helper.runMigrationsAndValidate(
            TEST_DB,
            5,
            true,
            QuoteCollectionDatabase.MIGRATION_4_5
        )

        db.query("SELECT * FROM ${QuoteCollectionContract.TABLE}").use { cursor ->
            assertEquals(cursor.count, 1)
            cursor.moveToFirst()
            assertArrayEquals(
                cursor.columnNames,
                arrayOf(
                    QuoteCollectionContract.ID,
                    QuoteCollectionContract.TEXT,
                    QuoteCollectionContract.SOURCE,
                    QuoteCollectionContract.AUTHOR,
                    QuoteCollectionContract.PROVIDER,
                    QuoteCollectionContract.UID,
                    QuoteCollectionContract.EXTRA
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