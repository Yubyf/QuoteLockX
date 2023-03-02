package com.crossbowffs.quotelock.data.modules.fortune.database

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.crossbowffs.quotelock.app.configs.fortune.FortunePrefKeys.FORTUNE_QUOTE_MAX_LENGTH
import com.crossbowffs.quotelock.data.api.QuoteEntity
import com.crossbowffs.quotelock.data.api.QuoteEntityContract
import com.crossbowffs.quotelock.data.modules.fortune.database.FortuneQuoteContract.DATABASE_NAME
import com.crossbowffs.quotelock.utils.md5String
import com.yubyf.quotelockx.BuildConfig
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * @author Yubyf
 */

object FortuneQuoteContract {
    const val DATABASE_NAME = "fortune_quotes.db"

    const val TABLE = "fortune"

    @Deprecated("Use [UID] instead")
    const val MD5 = QuoteEntityContract.MD5
    const val TEXT = QuoteEntityContract.TEXT
    const val SOURCE = QuoteEntityContract.SOURCE
    const val AUTHOR = QuoteEntityContract.AUTHOR
    const val CATEGORY = "category"
    const val ID = QuoteEntityContract.ID
    const val UID = QuoteEntityContract.UID
}

@Entity(
    tableName = FortuneQuoteContract.TABLE,
    indices = [Index(value = [FortuneQuoteContract.UID], unique = true)]
)
data class FortuneQuoteEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = FortuneQuoteContract.ID)
    override var id: Int? = null,
    @ColumnInfo(name = FortuneQuoteContract.TEXT)
    override val text: String,
    @ColumnInfo(name = FortuneQuoteContract.SOURCE)
    override val source: String,
    @ColumnInfo(name = FortuneQuoteContract.AUTHOR)
    override val author: String = "",
    @ColumnInfo(name = FortuneQuoteContract.CATEGORY)
    val category: String = "",
    @ColumnInfo(name = FortuneQuoteContract.UID)
    override var uid: String,
) : QuoteEntity {
    @Ignore
    override var provider: String = FortuneQuoteContract.TABLE

    @Ignore
    override val extra: ByteArray? = null
}

@Dao
interface FortuneQuoteDao {
    @Query(
        "SELECT * FROM ${FortuneQuoteContract.TABLE} " +
                "WHERE ${FortuneQuoteContract.CATEGORY} = :category " +
                "AND LENGTH(${FortuneQuoteContract.TEXT}) <= $FORTUNE_QUOTE_MAX_LENGTH " +
                "ORDER BY RANDOM() LIMIT 1"
    )
    fun getRandomItemByCategory(category: String): Flow<FortuneQuoteEntity>

    @Query(
        "SELECT * FROM ${FortuneQuoteContract.TABLE} " +
                "WHERE LENGTH(${FortuneQuoteContract.TEXT}) <= $FORTUNE_QUOTE_MAX_LENGTH " +
                "ORDER BY RANDOM() LIMIT 1"
    )
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
        private const val PRESET_FORTUNE_DB_MD5 = "5f8be72a34468b4171a19dc37f1cb21d"

        @Volatile
        private var INSTANCE: FortuneQuoteDatabase? = null

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE ${FortuneQuoteContract.TABLE} RENAME TO tmp_table"
                )
                database.execSQL(
                    "CREATE TABLE ${FortuneQuoteContract.TABLE}(" +
                            "${FortuneQuoteContract.ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "${FortuneQuoteContract.TEXT} TEXT NOT NULL, " +
                            "${FortuneQuoteContract.SOURCE} TEXT NOT NULL, " +
                            "${FortuneQuoteContract.AUTHOR} TEXT NOT NULL ON CONFLICT REPLACE DEFAULT '', " +
                            "${FortuneQuoteContract.CATEGORY} TEXT NOT NULL, " +
                            "${FortuneQuoteContract.UID} TEXT UNIQUE NOT NULL)"
                )
                database.execSQL(
                    "CREATE UNIQUE INDEX index_" +
                            "${"${FortuneQuoteContract.TABLE}_${FortuneQuoteContract.UID}"} " +
                            "on ${FortuneQuoteContract.TABLE}(${FortuneQuoteContract.UID})"
                )
                database.execSQL(
                    "INSERT OR REPLACE INTO ${FortuneQuoteContract.TABLE}(" +
                            "${FortuneQuoteContract.ID}, ${FortuneQuoteContract.TEXT}, " +
                            "${FortuneQuoteContract.SOURCE}, ${FortuneQuoteContract.AUTHOR}, " +
                            "${FortuneQuoteContract.CATEGORY}, ${FortuneQuoteContract.UID}) " +
                            "SELECT ${FortuneQuoteContract.ID}, ${FortuneQuoteContract.TEXT}, " +
                            "${FortuneQuoteContract.SOURCE}, ${FortuneQuoteContract.AUTHOR}, " +
                            "${FortuneQuoteContract.CATEGORY}, ${FortuneQuoteContract.MD5} " +
                            "FROM tmp_table"
                )
                database.execSQL("DROP TABLE tmp_table")
            }
        }

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
                        if (!it.exists() || it.md5String() != PRESET_FORTUNE_DB_MD5) {
                            createFromAsset("database${File.separatorChar}$DATABASE_NAME")
                        }
                    }
                }.setJournalMode(JournalMode.TRUNCATE)
                    .addMigrations(MIGRATION_4_5)
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