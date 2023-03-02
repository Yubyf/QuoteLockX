package com.crossbowffs.quotelock.data.modules.custom.database

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.crossbowffs.quotelock.data.api.QuoteEntity
import com.crossbowffs.quotelock.data.api.QuoteEntityContract
import com.crossbowffs.quotelock.data.modules.custom.database.CustomQuoteContract.DATABASE_NAME
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
    const val PROVIDER = QuoteEntityContract.PROVIDER
    const val PROVIDER_VALUE = "custom"
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
    override val uid: String = ""

    @Ignore
    override val extra: ByteArray? = null

    @ColumnInfo(name = CustomQuoteContract.PROVIDER)
    override var provider: String = CustomQuoteContract.PROVIDER_VALUE
}

@Dao
interface CustomQuoteDao {
    @Query("SELECT * FROM ${CustomQuoteContract.TABLE}")
    fun getAllStream(): Flow<List<CustomQuoteEntity>>

    @Query("SELECT * FROM ${CustomQuoteContract.TABLE} WHERE ${CustomQuoteContract.ID} = :id")
    fun getById(id: Long): CustomQuoteEntity?

    @Query("SELECT * FROM ${CustomQuoteContract.TABLE} ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomItem(): CustomQuoteEntity?

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
                database.execSQL("ALTER TABLE ${CustomQuoteContract.TABLE}" +
                        " ADD COLUMN ${CustomQuoteContract.AUTHOR} TEXT NOT NULL DEFAULT ''")
            }
        }
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE ${CustomQuoteContract.TABLE} RENAME TO tmp_table")
                database.execSQL("CREATE TABLE ${CustomQuoteContract.TABLE}(" +
                        "${CustomQuoteContract.ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "${CustomQuoteContract.TEXT} TEXT NOT NULL, " +
                        "${CustomQuoteContract.SOURCE} TEXT NOT NULL, " +
                        "${CustomQuoteContract.AUTHOR} TEXT NOT NULL ON CONFLICT REPLACE DEFAULT '')")
                database.execSQL("INSERT OR REPLACE INTO ${CustomQuoteContract.TABLE}(" +
                        "${CustomQuoteContract.ID}, ${CustomQuoteContract.TEXT}, " +
                        "${CustomQuoteContract.SOURCE}, ${CustomQuoteContract.AUTHOR}) " +
                        "SELECT ${CustomQuoteContract.ID}, ${CustomQuoteContract.TEXT}, " +
                        "${CustomQuoteContract.SOURCE}, ${QuoteEntityContract.AUTHOR_OLD} " +
                        "FROM tmp_table")
                database.execSQL("DROP TABLE tmp_table")
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}