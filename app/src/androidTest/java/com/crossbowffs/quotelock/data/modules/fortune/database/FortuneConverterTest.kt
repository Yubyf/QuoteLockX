package com.crossbowffs.quotelock.data.modules.fortune.database

import android.os.Environment
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.md5
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.IOException
import java.io.InputStream

/**
 * Converts fortune cookies from txt files to a database.
 * For custom import.
 *
 * @author Yubyf
 */
@RunWith(AndroidJUnit4::class)
class FortuneConverterTest {

    private val appContext = InstrumentationRegistry.getInstrumentation().context

    @Test
    fun convert() {
        val dao = Room.databaseBuilder(
            appContext,
            FortuneQuoteDatabase::class.java,
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                FortuneQuoteContract.DATABASE_NAME
            ).absolutePath
        ).setJournalMode(RoomDatabase.JournalMode.TRUNCATE).build().dao()
        runBlocking {
            appContext.assets.apply {
                list("fortune")?.onEach { fileName ->
                    open("fortune/$fileName").use { stream ->
                        try {
                            stream.importFortune(dao, fileName)
                        } catch (e: IOException) {
                            Xlog.e(TAG, "Error importing fortune file $fileName", e)
                            Assert.fail("Error importing fortune file $fileName")
                        }
                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    private suspend fun InputStream.importFortune(
        fortuneQuoteDao: FortuneQuoteDao,
        category: String,
    ) = bufferedReader().useLines { lines ->
        val footerRegex = FORTUNE_FOOTER_TEMPLATE.toRegex()
        val delimiterRegex = FORTUNE_DELIMITER_TEMPLATE.toRegex()
        val sourceRegex = FORTUNE_SOURCE_TEMPLATE.toRegex()
        val text = StringBuilder()
        val source = StringBuilder()
        var fortuneCount = 0
        lines.forEach { line ->
            when {
                footerRegex.matches(line) -> Xlog.d(TAG, "Found fortune footer")

                delimiterRegex.matches(line) -> {
                    fortuneQuoteDao.insert(
                        FortuneQuoteEntity(
                            text = text.toString(),
                            source = source.toString(),
                            uid = ("$text$source${FortuneQuoteContract.TABLE}").md5(),
                            author = "",
                            category = category,
                        )
                    )
                    // Reset
                    text.clear()
                    source.clear()
                    fortuneCount++
                }

                else -> {
                    sourceRegex.find(line)?.let { match ->
                        match.groupValues[1].let { group ->
                            if (group.isNotBlank()) {
                                source.clear().append(group.trim())
                            }
                        }
                    } ?: line.let {
                        if (it.isNotBlank()) {
                            text.apply { if (isNotBlank()) append(" ") }.append(it.trim())
                        }
                    }
                }
            }
        }
        if (text.isNotBlank()) {
            fortuneQuoteDao.insert(
                FortuneQuoteEntity(
                    text = text.toString(),
                    source = source.toString(),
                    uid = ("$text$source${FortuneQuoteContract.TABLE}").md5(),
                    author = "",
                    category = category,
                )
            )
            // Reset
            text.clear()
            source.clear()
            fortuneCount++
        }
        Xlog.d(TAG, "Imported $fortuneCount fortunes from $category")
    }

    companion object {
        private const val TAG = "FortuneConverter"
        private const val FORTUNE_DELIMITER_TEMPLATE = "^\\s*%\\s*\$"
        private const val FORTUNE_FOOTER_TEMPLATE = "^\\s*%%\\s*\$"
        private const val FORTUNE_SOURCE_TEMPLATE = "^\\t{2,}-{2,}\\s*(.+)$"
    }
}