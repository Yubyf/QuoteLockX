package com.crossbowffs.quotelock.data.modules.fortune.database

import android.content.Context
import androidx.room.*
import com.crossbowffs.quotelock.app.configs.fortune.FortunePrefKeys.FORTUNE_QUOTE_MAX_LENGTH
import com.crossbowffs.quotelock.data.api.QuoteEntity
import com.crossbowffs.quotelock.data.api.QuoteEntityContract
import com.crossbowffs.quotelock.data.modules.fortune.database.FortuneQuoteContract.DATABASE_NAME
import com.yubyf.quotelockx.BuildConfig
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * @author Yubyf
 */

object FortuneQuoteContract {
    const val DATABASE_NAME = "fortune_quotes.db"

    const val TABLE = "fortune"
    const val MD5 = QuoteEntityContract.MD5
    const val TEXT = QuoteEntityContract.TEXT
    const val SOURCE = QuoteEntityContract.SOURCE
    const val AUTHOR = QuoteEntityContract.AUTHOR
    const val CATEGORY = "category"
    const val ID = QuoteEntityContract.ID
}

@Entity(tableName = FortuneQuoteContract.TABLE,
    indices = [Index(value = [FortuneQuoteContract.MD5], unique = true)])
data class FortuneQuoteEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = FortuneQuoteContract.ID)
    override var id: Int? = null,
    @ColumnInfo(name = FortuneQuoteContract.MD5)
    override val md5: String,
    @ColumnInfo(name = FortuneQuoteContract.TEXT)
    override val text: String,
    @ColumnInfo(name = FortuneQuoteContract.SOURCE)
    override val source: String,
    @ColumnInfo(name = FortuneQuoteContract.AUTHOR)
    override val author: String = "",
    @ColumnInfo(name = FortuneQuoteContract.CATEGORY)
    val category: String = "",
) : QuoteEntity

@Dao
interface FortuneQuoteDao {
    @Query("SELECT * FROM ${FortuneQuoteContract.TABLE} " +
            "WHERE ${FortuneQuoteContract.CATEGORY} = :category " +
            "AND LENGTH(${FortuneQuoteContract.TEXT}) <= $FORTUNE_QUOTE_MAX_LENGTH " +
            "ORDER BY RANDOM() LIMIT 1")
    fun getRandomItemByCategory(category: String): Flow<FortuneQuoteEntity>

    @Query("SELECT * FROM ${FortuneQuoteContract.TABLE} " +
            "WHERE LENGTH(${FortuneQuoteContract.TEXT}) <= $FORTUNE_QUOTE_MAX_LENGTH " +
            "ORDER BY RANDOM() LIMIT 1")
    fun getRandomItem(): Flow<FortuneQuoteEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(fortuneQuote: FortuneQuoteEntity): Long?

    @Query("SELECT COUNT(*) FROM ${FortuneQuoteContract.TABLE}")
    fun count(): Flow<Int>
}

@Database(entities = [FortuneQuoteEntity::class], version = BuildConfig.FORTUNE_QUOTES_DB_VERSION)
abstract class FortuneQuoteDatabase : RoomDatabase() {
    abstract fun dao(): FortuneQuoteDao

    companion object {
        @Volatile
        private var INSTANCE: FortuneQuoteDatabase? = null

        fun getDatabase(context: Context): FortuneQuoteDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FortuneQuoteDatabase::class.java,
                    DATABASE_NAME
                ).apply {
                    context.getDatabasePath(DATABASE_NAME).let {
                        if (!it.exists()) {
                            createFromAsset("database${File.separatorChar}$DATABASE_NAME")
                        }
                    }
                }.setJournalMode(JournalMode.TRUNCATE)
                    // Enable destructive migrations to ensure that the on-device database
                    // can be overwritten when upgrading using the in-package database.
                    .fallbackToDestructiveMigration().build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}