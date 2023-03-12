@file:Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER")

package com.crossbowffs.quotelock.data.modules.custom.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.crossbowffs.quotelock.data.SAMPLE_AUTHOR
import com.crossbowffs.quotelock.data.SAMPLE_ID
import com.crossbowffs.quotelock.data.SAMPLE_SOURCE
import com.crossbowffs.quotelock.data.SAMPLE_TEXT
import com.crossbowffs.quotelock.data.TEST_DB
import com.crossbowffs.quotelock.data.api.QuoteEntityContract
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * @author Yubyf
 * @date 2023/3/12.
 */
@RunWith(AndroidJUnit4::class)
class CustomQuoteDatabaseTest {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        CustomQuoteDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun testMigrate2to3() {
        var db = helper.createDatabase(TEST_DB, 2).apply {
            execSQL(
                "INSERT INTO ${CustomQuoteContract.TABLE} " +
                        "(${CustomQuoteContract.ID}, ${CustomQuoteContract.TEXT}, ${CustomQuoteContract.SOURCE}) " +
                        "VALUES ($SAMPLE_ID, '$SAMPLE_TEXT', '$SAMPLE_SOURCE')"
            )
            close()
        }

        db = helper.runMigrationsAndValidate(
            TEST_DB,
            3,
            true,
            CustomQuoteDatabase.MIGRATION_2_3
        )

        db.query("SELECT * FROM ${CustomQuoteContract.TABLE}").use { cursor ->
            Assert.assertEquals(cursor.count, 1)
            cursor.moveToFirst()
            Assert.assertArrayEquals(
                cursor.columnNames,
                arrayOf(
                    CustomQuoteContract.ID,
                    CustomQuoteContract.TEXT,
                    CustomQuoteContract.SOURCE,
                    QuoteEntityContract.AUTHOR_OLD
                )
            )
            Assert.assertEquals(cursor.getInt(0), SAMPLE_ID)
            Assert.assertEquals(cursor.getString(1), SAMPLE_TEXT)
            Assert.assertEquals(cursor.getString(2), SAMPLE_SOURCE)
            Assert.assertEquals(cursor.getString(3), "")
        }
    }

    @Test
    @Throws(IOException::class)
    fun testMigrate3to4() {
        var db = helper.createDatabase(TEST_DB, 3).apply {
            execSQL(
                "INSERT INTO ${CustomQuoteContract.TABLE} " +
                        "(${CustomQuoteContract.ID}, ${CustomQuoteContract.TEXT}, " +
                        "${CustomQuoteContract.SOURCE}, ${QuoteEntityContract.AUTHOR_OLD}) " +
                        "VALUES ($SAMPLE_ID, '$SAMPLE_TEXT', '$SAMPLE_SOURCE', '$SAMPLE_AUTHOR')"
            )
            close()
        }

        db = helper.runMigrationsAndValidate(
            TEST_DB,
            4,
            true,
            CustomQuoteDatabase.MIGRATION_3_4
        )

        db.query("SELECT * FROM ${CustomQuoteContract.TABLE}").use { cursor ->
            Assert.assertEquals(cursor.count, 1)
            cursor.moveToFirst()
            Assert.assertArrayEquals(
                cursor.columnNames,
                arrayOf(
                    CustomQuoteContract.ID,
                    CustomQuoteContract.TEXT,
                    CustomQuoteContract.SOURCE,
                    CustomQuoteContract.AUTHOR
                )
            )
            Assert.assertEquals(cursor.getInt(0), SAMPLE_ID)
            Assert.assertEquals(cursor.getString(1), SAMPLE_TEXT)
            Assert.assertEquals(cursor.getString(2), SAMPLE_SOURCE)
            Assert.assertEquals(cursor.getString(3), SAMPLE_AUTHOR)
        }
    }

    @Test
    @Throws(IOException::class)
    fun testMigrate4to5() {
        var db = helper.createDatabase(TEST_DB, 4).apply {
            execSQL(
                "INSERT INTO ${CustomQuoteContract.TABLE} " +
                        "(${CustomQuoteContract.ID}, ${CustomQuoteContract.TEXT}, " +
                        "${CustomQuoteContract.SOURCE}, ${CustomQuoteContract.AUTHOR}) " +
                        "VALUES ($SAMPLE_ID, '$SAMPLE_TEXT', '$SAMPLE_SOURCE', '$SAMPLE_AUTHOR')"
            )
            close()
        }

        db = helper.runMigrationsAndValidate(
            TEST_DB,
            5,
            true,
            CustomQuoteDatabase.MIGRATION_4_5
        )

        db.query("SELECT * FROM ${CustomQuoteContract.TABLE}").use { cursor ->
            Assert.assertEquals(cursor.count, 1)
            cursor.moveToFirst()
            Assert.assertArrayEquals(
                cursor.columnNames,
                arrayOf(
                    CustomQuoteContract.ID,
                    CustomQuoteContract.TEXT,
                    CustomQuoteContract.SOURCE,
                    CustomQuoteContract.AUTHOR,
                    CustomQuoteContract.PROVIDER
                )
            )
            Assert.assertEquals(cursor.getInt(0), SAMPLE_ID)
            Assert.assertEquals(cursor.getString(1), SAMPLE_TEXT)
            Assert.assertEquals(cursor.getString(2), SAMPLE_SOURCE)
            Assert.assertEquals(cursor.getString(3), SAMPLE_AUTHOR)
            Assert.assertEquals(cursor.getString(4), CustomQuoteContract.PROVIDER_VALUE)
        }
    }
}