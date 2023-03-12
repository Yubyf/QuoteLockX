@file:Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER")

package com.crossbowffs.quotelock.data.modules.fortune.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.crossbowffs.quotelock.data.SAMPLE_AUTHOR
import com.crossbowffs.quotelock.data.SAMPLE_ID
import com.crossbowffs.quotelock.data.SAMPLE_SOURCE
import com.crossbowffs.quotelock.data.SAMPLE_TEXT
import com.crossbowffs.quotelock.data.SAMPLE_UID
import com.crossbowffs.quotelock.data.TEST_DB
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
class FortuneQuoteDatabaseTest {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        FortuneQuoteDatabase::class.java,
        emptyList()
    )


    @Test
    @Throws(IOException::class)
    fun testMigrate4to5() {
        var db = helper.createDatabase(TEST_DB, 4).apply {
            execSQL(
                "INSERT INTO ${FortuneQuoteContract.TABLE} " +
                        "(${FortuneQuoteContract.ID}, ${FortuneQuoteContract.MD5}, " +
                        "${FortuneQuoteContract.TEXT}, ${FortuneQuoteContract.SOURCE}, " +
                        "${FortuneQuoteContract.AUTHOR}, ${FortuneQuoteContract.CATEGORY}) " +
                        "VALUES ($SAMPLE_ID, '$SAMPLE_UID', '$SAMPLE_TEXT', '$SAMPLE_SOURCE', " +
                        "'$SAMPLE_AUTHOR', 'art')"
            )
            close()
        }

        db = helper.runMigrationsAndValidate(
            TEST_DB,
            5,
            true,
            FortuneQuoteDatabase.MIGRATION_4_5
        )

        db.query("SELECT * FROM ${FortuneQuoteContract.TABLE}").use { cursor ->
            Assert.assertEquals(cursor.count, 1)
            cursor.moveToFirst()
            Assert.assertArrayEquals(
                cursor.columnNames,
                arrayOf(
                    FortuneQuoteContract.ID,
                    FortuneQuoteContract.TEXT,
                    FortuneQuoteContract.SOURCE,
                    FortuneQuoteContract.AUTHOR,
                    FortuneQuoteContract.CATEGORY,
                    FortuneQuoteContract.UID
                )
            )
            Assert.assertEquals(cursor.getInt(0), SAMPLE_ID)
            Assert.assertEquals(cursor.getString(1), SAMPLE_TEXT)
            Assert.assertEquals(cursor.getString(2), SAMPLE_SOURCE)
            Assert.assertEquals(cursor.getString(3), SAMPLE_AUTHOR)
            Assert.assertEquals(cursor.getString(4), "art")
            Assert.assertEquals(cursor.getString(5), SAMPLE_UID)
        }
    }
}