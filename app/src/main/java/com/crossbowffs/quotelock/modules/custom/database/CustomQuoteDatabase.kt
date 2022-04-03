package com.crossbowffs.quotelock.modules.custom.database

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.crossbowffs.quotelock.app.App
import com.crossbowffs.quotelock.database.QuoteEntity
import com.crossbowffs.quotelock.database.QuoteEntityContract
import com.crossbowffs.quotelock.modules.custom.database.CustomQuoteContract.DATABASE_NAME
import com.yubyf.quotelockx.BuildConfig
import kotlinx.coroutines.flow.Flow

/**
 * @author Yubyf
 */

object CustomQuoteContract {
    const val DATABASE_NAME = "custom_quotes.db"

    const val TABLE = "quotes"
    const val TEXT = QuoteEntityContract.TEXT
    const val SOURCE = QuoteEntityContract.SOURCE
    const val AUTHOR = QuoteEntityContract.AUTHOR
    const val ID = QuoteEntityContract.ID
}

@Entity(tableName = CustomQuoteContract.TABLE)
data class CustomQuoteEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = CustomQuoteContract.ID)
    override var id: Int? = null,
    @ColumnInfo(name = CustomQuoteContract.TEXT)
    override val text: String,
    @ColumnInfo(name = CustomQuoteContract.SOURCE)
    override val source: String,
    @ColumnInfo(name = CustomQuoteContract.AUTHOR)
    override val author: String = "",
) : QuoteEntity {
    @Ignore
    override val md5: String = ""
}

@Dao
interface CustomQuoteDao {
    @Query("SELECT * FROM ${CustomQuoteContract.TABLE}")
    fun getAll(): Flow<List<CustomQuoteEntity>>

    @Query("SELECT * FROM ${CustomQuoteContract.TABLE} WHERE ${CustomQuoteContract.ID} = :id")
    fun getById(id: Long): Flow<CustomQuoteEntity?>

    @Query("SELECT * FROM ${CustomQuoteContract.TABLE} ORDER BY RANDOM() LIMIT 1")
    fun getRandomItem(): Flow<CustomQuoteEntity?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(quote: CustomQuoteEntity): Long?

    @Update
    suspend fun update(quote: CustomQuoteEntity)

    @Query("DELETE FROM ${CustomQuoteContract.TABLE} WHERE ${CustomQuoteContract.ID} = :id")
    suspend fun delete(id: Long): Int
}

@Database(entities = [CustomQuoteEntity::class], version = BuildConfig.CUSTOM_QUOTES_DB_VERSION)
abstract class CustomQuoteDatabase : RoomDatabase() {
    abstract fun dao(): CustomQuoteDao

    companion object {
        @Volatile
        private var INSTANCE: CustomQuoteDatabase? = null
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Empty implementation, because the schema isn't changing.
            }
        }
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE ${CustomQuoteContract.TABLE} ADD COLUMN ${CustomQuoteContract.AUTHOR} TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): CustomQuoteDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CustomQuoteDatabase::class.java,
                    DATABASE_NAME
                ).setJournalMode(JournalMode.TRUNCATE)
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
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
val customQuoteDatabase by lazy { CustomQuoteDatabase.getDatabase(App.INSTANCE) }