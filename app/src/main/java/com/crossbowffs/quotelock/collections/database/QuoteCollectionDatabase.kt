package com.crossbowffs.quotelock.collections.database

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.crossbowffs.quotelock.app.App
import com.crossbowffs.quotelock.collections.database.QuoteCollectionContract.DATABASE_NAME
import com.crossbowffs.quotelock.database.QuoteEntity
import com.crossbowffs.quotelock.database.QuoteEntityContract
import com.crossbowffs.quotelock.utils.md5
import com.opencsv.bean.CsvBindByName
import com.yubyf.quotelockx.BuildConfig
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * @author Yubyf
 */

object QuoteCollectionContract {
    const val AUTHORITY = BuildConfig.APPLICATION_ID + ".collection.provider"
    const val DATABASE_NAME = "quote_collections.db"

    const val TABLE = "collections"
    const val MD5 = QuoteEntityContract.MD5
    const val TEXT = QuoteEntityContract.TEXT
    const val SOURCE = QuoteEntityContract.SOURCE
    const val AUTHOR = QuoteEntityContract.AUTHOR
    const val ID = QuoteEntityContract.ID
}

@Entity(tableName = QuoteCollectionContract.TABLE,
    indices = [Index(value = [QuoteCollectionContract.MD5], unique = true)])
data class QuoteCollectionEntity @JvmOverloads constructor(
    @CsvBindByName(column = QuoteCollectionContract.ID, required = true)
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = QuoteCollectionContract.ID)
    override var id: Int? = null,
    @CsvBindByName(column = QuoteCollectionContract.MD5, required = true)
    @ColumnInfo(name = QuoteCollectionContract.MD5)
    override var md5: String,
    @CsvBindByName(column = QuoteCollectionContract.TEXT, required = true)
    @ColumnInfo(name = QuoteCollectionContract.TEXT)
    override var text: String,
    @CsvBindByName(column = QuoteCollectionContract.SOURCE, required = true)
    @ColumnInfo(name = QuoteCollectionContract.SOURCE)
    override var source: String,
    @CsvBindByName(column = QuoteCollectionContract.AUTHOR, required = false)
    @ColumnInfo(name = QuoteCollectionContract.AUTHOR)
    override var author: String = "",
) : QuoteEntity {
    // Empty constructor for OpenCSV
    constructor() : this(
        id = null,
        text = "",
        source = "",
        author = "",
        md5 = "".md5()
    )
}

@Dao
interface QuoteCollectionDao {
    @Query("SELECT * FROM ${QuoteCollectionContract.TABLE}")
    fun getAll(): Flow<List<QuoteCollectionEntity>>

    @Query("""SELECT * FROM ${QuoteCollectionContract.TABLE}"""
            + """ WHERE ${QuoteCollectionContract.TEXT} = :text"""
            + """ AND ${QuoteCollectionContract.SOURCE} = :source"""
            + """ AND ${QuoteCollectionContract.AUTHOR} = :author""")
    fun getByQuote(text: String, source: String, author: String?): Flow<QuoteCollectionEntity?>

    @Query("SELECT * FROM ${QuoteCollectionContract.TABLE} ORDER BY RANDOM() LIMIT 1")
    fun getRandomItem(): Flow<QuoteCollectionEntity?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(quote: QuoteCollectionEntity): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(quotes: List<QuoteCollectionEntity>): Array<Long>

    @Query("SELECT COUNT(*) FROM ${QuoteCollectionContract.TABLE}")
    fun count(): Flow<Int>

    @Query("DELETE FROM ${QuoteCollectionContract.TABLE} WHERE ${QuoteCollectionContract.ID} = :id")
    suspend fun delete(id: Long): Int

    @Query("DELETE FROM ${QuoteCollectionContract.TABLE} WHERE ${QuoteCollectionContract.MD5} = :md5")
    suspend fun delete(md5: String): Int

    @Query("DELETE FROM ${QuoteCollectionContract.TABLE}")
    suspend fun clear()
}

@Database(entities = [QuoteCollectionEntity::class],
    version = BuildConfig.QUOTE_COLLECTIONS_DB_VERSION)
abstract class QuoteCollectionDatabase : RoomDatabase() {
    abstract fun dao(): QuoteCollectionDao

    companion object {
        @Volatile
        private var INSTANCE: QuoteCollectionDatabase? = null
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Empty implementation, because the schema isn't changing.
            }
        }
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE ${QuoteCollectionContract.TABLE}" +
                        " ADD COLUMN ${QuoteCollectionContract.AUTHOR} TEXT")
            }
        }
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE ${QuoteCollectionContract.TABLE} RENAME TO tmp_table")
                database.execSQL("CREATE TABLE ${QuoteCollectionContract.TABLE}(" +
                        "${QuoteCollectionContract.ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "${QuoteCollectionContract.TEXT} TEXT NOT NULL, " +
                        "${QuoteCollectionContract.SOURCE} TEXT NOT NULL, " +
                        "${QuoteCollectionContract.MD5} TEXT UNIQUE NOT NULL, " +
                        "${QuoteCollectionContract.AUTHOR} TEXT NOT NULL)")
                database.execSQL("CREATE UNIQUE INDEX index_" +
                        "${"${QuoteCollectionContract.TABLE}_${QuoteCollectionContract.MD5}"} " +
                        "on ${QuoteCollectionContract.TABLE}(${QuoteCollectionContract.MD5})")
                database.execSQL("INSERT OR IGNORE INTO ${QuoteCollectionContract.TABLE}(" +
                        "${QuoteCollectionContract.ID}, ${QuoteCollectionContract.TEXT}, " +
                        "${QuoteCollectionContract.SOURCE}, ${QuoteCollectionContract.MD5}, " +
                        "${QuoteCollectionContract.AUTHOR}) " +
                        "SELECT ${QuoteCollectionContract.ID}, ${QuoteCollectionContract.TEXT}, " +
                        "${QuoteCollectionContract.SOURCE}, ${QuoteCollectionContract.MD5}, " +
                        "${QuoteEntityContract.AUTHOR_OLD} " +
                        "FROM tmp_table")
                database.execSQL("DROP TABLE tmp_table")
            }
        }

        fun getDatabase(context: Context): QuoteCollectionDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuoteCollectionDatabase::class.java,
                    DATABASE_NAME
                ).setJournalMode(JournalMode.TRUNCATE)
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }

        fun openTemporaryDatabaseFrom(
            context: Context,
            name: String,
            file: File,
        ): QuoteCollectionDatabase {
            return synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    QuoteCollectionDatabase::class.java,
                    name
                ).setJournalMode(JournalMode.TRUNCATE)
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .createFromFile(file)
                    .build()
            }
        }
    }
}

// Using by lazy so the databases are only created when they're needed
// rather than when the application starts
val quoteCollectionDatabase by lazy { QuoteCollectionDatabase.getDatabase(App.INSTANCE) }