package com.crossbowffs.quotelock.history.database

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.crossbowffs.quotelock.app.App
import com.crossbowffs.quotelock.database.QuoteEntity
import com.crossbowffs.quotelock.database.QuoteEntityContract
import com.crossbowffs.quotelock.history.database.QuoteHistoryContract.DATABASE_NAME
import com.yubyf.quotelockx.BuildConfig
import kotlinx.coroutines.flow.Flow

/**
 * @author Yubyf
 */

object QuoteHistoryContract {
    const val DATABASE_NAME = "quote_histories.db"

    const val TABLE = "histories"
    const val MD5 = QuoteEntityContract.MD5
    const val TEXT = QuoteEntityContract.TEXT
    const val SOURCE = QuoteEntityContract.SOURCE
    const val AUTHOR = QuoteEntityContract.AUTHOR
    const val ID = QuoteEntityContract.ID
}


@Entity(tableName = QuoteHistoryContract.TABLE,
    indices = [Index(value = [QuoteHistoryContract.MD5], unique = true)])
data class QuoteHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = QuoteHistoryContract.ID)
    override var id: Int? = null,
    @ColumnInfo(name = QuoteHistoryContract.MD5)
    override val md5: String,
    @ColumnInfo(name = QuoteHistoryContract.TEXT)
    override val text: String,
    @ColumnInfo(name = QuoteHistoryContract.SOURCE)
    override val source: String,
    @ColumnInfo(name = QuoteHistoryContract.AUTHOR)
    override val author: String = "",
) : QuoteEntity

@Dao
interface QuoteHistoryDao {
    @Query("SELECT * FROM ${QuoteHistoryContract.TABLE}")
    fun getAll(): Flow<List<QuoteHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(quote: QuoteHistoryEntity): Long?

    @Query("SELECT COUNT(*) FROM ${QuoteHistoryContract.TABLE}")
    fun count(): Flow<Int>

    @Delete
    suspend fun delete(quote: QuoteHistoryEntity): Int

    @Query("DELETE FROM ${QuoteHistoryContract.TABLE} WHERE ${QuoteHistoryContract.ID} = :id")
    suspend fun delete(id: Long): Int

    @Query("DELETE FROM ${QuoteHistoryContract.TABLE}")
    suspend fun deleteAll()
}

@Database(entities = [QuoteHistoryEntity::class], version = BuildConfig.QUOTE_HISTORIES_DB_VERSION)
abstract class QuoteHistoryDatabase : RoomDatabase() {
    abstract fun dao(): QuoteHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: QuoteHistoryDatabase? = null
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Empty implementation, because the schema isn't changing.
            }
        }
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE ${QuoteHistoryContract.TABLE}" +
                        " ADD COLUMN ${QuoteHistoryContract.AUTHOR} TEXT NOT NULL DEFAULT ''")
            }
        }
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE ${QuoteHistoryContract.TABLE} RENAME TO tmp_table")
                database.execSQL("CREATE TABLE ${QuoteHistoryContract.TABLE}(" +
                        "${QuoteHistoryContract.ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "${QuoteHistoryContract.TEXT} TEXT NOT NULL, " +
                        "${QuoteHistoryContract.SOURCE} TEXT NOT NULL, " +
                        "${QuoteHistoryContract.MD5} TEXT UNIQUE NOT NULL, " +
                        "${QuoteHistoryContract.AUTHOR} TEXT NOT NULL ON CONFLICT REPLACE DEFAULT '')")
                database.execSQL("CREATE UNIQUE INDEX index_" +
                        "${"${QuoteHistoryContract.TABLE}_${QuoteHistoryContract.MD5}"} " +
                        "on ${QuoteHistoryContract.TABLE}(${QuoteHistoryContract.MD5})")
                database.execSQL("INSERT OR REPLACE INTO ${QuoteHistoryContract.TABLE}(" +
                        "${QuoteHistoryContract.ID}, ${QuoteHistoryContract.TEXT}, " +
                        "${QuoteHistoryContract.SOURCE}, ${QuoteHistoryContract.MD5}, " +
                        "${QuoteHistoryContract.AUTHOR}) " +
                        "SELECT ${QuoteHistoryContract.ID}, ${QuoteHistoryContract.TEXT}, " +
                        "${QuoteHistoryContract.SOURCE}, ${QuoteHistoryContract.MD5}, " +
                        "${QuoteEntityContract.AUTHOR_OLD} " +
                        "FROM tmp_table")
                database.execSQL("DROP TABLE tmp_table")
            }
        }

        fun getDatabase(context: Context): QuoteHistoryDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuoteHistoryDatabase::class.java,
                    DATABASE_NAME
                ).setJournalMode(JournalMode.TRUNCATE)
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}

// Using by lazy so the databases are only created when they're needed
// rather than when the application starts
val quoteHistoryDatabase by lazy { QuoteHistoryDatabase.getDatabase(App.INSTANCE) }