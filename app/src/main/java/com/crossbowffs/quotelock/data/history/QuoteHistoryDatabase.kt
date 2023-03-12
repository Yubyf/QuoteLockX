package com.crossbowffs.quotelock.data.history

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.crossbowffs.quotelock.data.api.QuoteEntity
import com.crossbowffs.quotelock.data.api.QuoteEntityContract
import com.crossbowffs.quotelock.data.history.QuoteHistoryContract.DATABASE_NAME
import com.yubyf.quotelockx.BuildConfig
import kotlinx.coroutines.flow.Flow

/**
 * @author Yubyf
 */

object QuoteHistoryContract {
    const val DATABASE_NAME = "quote_histories.db"

    const val TABLE = "histories"

    @Deprecated("Use [UID] instead")
    const val MD5 = QuoteEntityContract.MD5
    const val TEXT = QuoteEntityContract.TEXT
    const val SOURCE = QuoteEntityContract.SOURCE
    const val AUTHOR = QuoteEntityContract.AUTHOR
    const val ID = QuoteEntityContract.ID
    const val UID = QuoteEntityContract.UID
    const val PROVIDER = QuoteEntityContract.PROVIDER
    const val EXTRA = QuoteEntityContract.EXTRA
}


@Entity(
    tableName = QuoteHistoryContract.TABLE,
    indices = [Index(value = [QuoteHistoryContract.UID], unique = true)]
)
data class QuoteHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = QuoteHistoryContract.ID)
    override var id: Int? = null,
    @ColumnInfo(name = QuoteHistoryContract.TEXT)
    override val text: String,
    @ColumnInfo(name = QuoteHistoryContract.SOURCE)
    override val source: String,
    @ColumnInfo(name = QuoteHistoryContract.AUTHOR)
    override val author: String = "",
    @ColumnInfo(name = QuoteHistoryContract.PROVIDER)
    override var provider: String,
    @ColumnInfo(name = QuoteHistoryContract.UID)
    override var uid: String,
    @ColumnInfo(name = QuoteHistoryContract.EXTRA, typeAffinity = ColumnInfo.BLOB)
    override val extra: ByteArray? = null,
) : QuoteEntity {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QuoteHistoryEntity

        if (id != other.id) return false
        if (text != other.text) return false
        if (source != other.source) return false
        if (author != other.author) return false
        if (provider != other.provider) return false
        if (uid != other.uid) return false
        if (extra != null) {
            if (other.extra == null) return false
            if (!extra.contentEquals(other.extra)) return false
        } else if (other.extra != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id ?: 0
        result = 31 * result + text.hashCode()
        result = 31 * result + source.hashCode()
        result = 31 * result + author.hashCode()
        result = 31 * result + provider.hashCode()
        result = 31 * result + uid.hashCode()
        result = 31 * result + (extra?.contentHashCode() ?: 0)
        return result
    }
}

@Dao
interface QuoteHistoryDao {
    @Query("SELECT * FROM ${QuoteHistoryContract.TABLE}")
    fun getAllStream(): Flow<List<QuoteHistoryEntity>>

    @Query(
        "SELECT * FROM ${QuoteHistoryContract.TABLE} WHERE (text LIKE '%' || :keyword || '%'" +
                " OR source LIKE '%' || :keyword || '%'" +
                " OR author LIKE '%' || :keyword || '%')"
    )
    fun searchStream(keyword: String): Flow<List<QuoteHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(quote: QuoteHistoryEntity): Long?

    @Query("SELECT COUNT(*) FROM ${QuoteHistoryContract.TABLE}")
    fun countStream(): Flow<Int>

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
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE ${QuoteHistoryContract.TABLE}" +
                            " ADD COLUMN ${QuoteEntityContract.AUTHOR_OLD} TEXT NOT NULL DEFAULT ''"
                )
            }
        }
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE ${QuoteHistoryContract.TABLE} RENAME TO tmp_table"
                )
                database.execSQL(
                    "CREATE TABLE ${QuoteHistoryContract.TABLE}(" +
                            "${QuoteHistoryContract.ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "${QuoteHistoryContract.TEXT} TEXT NOT NULL, " +
                            "${QuoteHistoryContract.SOURCE} TEXT NOT NULL, " +
                            "${QuoteHistoryContract.MD5} TEXT UNIQUE NOT NULL, " +
                            "${QuoteHistoryContract.AUTHOR} TEXT NOT NULL ON CONFLICT REPLACE DEFAULT '')"
                )
                database.execSQL(
                    "CREATE UNIQUE INDEX index_" +
                            "${"${QuoteHistoryContract.TABLE}_${QuoteHistoryContract.MD5}"} " +
                            "on ${QuoteHistoryContract.TABLE}(${QuoteHistoryContract.MD5})"
                )
                database.execSQL(
                    "INSERT OR REPLACE INTO ${QuoteHistoryContract.TABLE}(" +
                            "${QuoteHistoryContract.ID}, ${QuoteHistoryContract.TEXT}, " +
                            "${QuoteHistoryContract.SOURCE}, ${QuoteHistoryContract.MD5}, " +
                            "${QuoteHistoryContract.AUTHOR}) " +
                            "SELECT ${QuoteHistoryContract.ID}, ${QuoteHistoryContract.TEXT}, " +
                            "${QuoteHistoryContract.SOURCE}, ${QuoteHistoryContract.MD5}, " +
                            "${QuoteEntityContract.AUTHOR_OLD} " +
                            "FROM tmp_table"
                )
                database.execSQL("DROP TABLE tmp_table")
            }
        }
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE ${QuoteHistoryContract.TABLE}" +
                            " ADD COLUMN ${QuoteHistoryContract.PROVIDER} TEXT DEFAULT '' NOT NULL"
                )
                database.execSQL(
                    "ALTER TABLE ${QuoteHistoryContract.TABLE}" +
                            " ADD COLUMN ${QuoteHistoryContract.EXTRA} BLOB DEFAULT NULL"
                )
                database.execSQL(
                    "ALTER TABLE ${QuoteHistoryContract.TABLE} RENAME TO tmp_table"
                )
                database.execSQL(
                    "CREATE TABLE ${QuoteHistoryContract.TABLE}(" +
                            "${QuoteHistoryContract.ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "${QuoteHistoryContract.TEXT} TEXT NOT NULL, " +
                            "${QuoteHistoryContract.SOURCE} TEXT NOT NULL, " +
                            "${QuoteHistoryContract.AUTHOR} TEXT NOT NULL ON CONFLICT REPLACE DEFAULT '', " +
                            "${QuoteHistoryContract.PROVIDER} TEXT NOT NULL, " +
                            "${QuoteHistoryContract.UID} TEXT UNIQUE NOT NULL, " +
                            "${QuoteHistoryContract.EXTRA} BLOB DEFAULT NULL)"
                )
                database.execSQL(
                    "CREATE UNIQUE INDEX index_" +
                            "${"${QuoteHistoryContract.TABLE}_${QuoteHistoryContract.UID}"} " +
                            "on ${QuoteHistoryContract.TABLE}(${QuoteHistoryContract.UID})"
                )
                database.execSQL(
                    "INSERT OR REPLACE INTO ${QuoteHistoryContract.TABLE}(" +
                            "${QuoteHistoryContract.ID}, ${QuoteHistoryContract.TEXT}, " +
                            "${QuoteHistoryContract.SOURCE}, ${QuoteHistoryContract.UID}, " +
                            "${QuoteHistoryContract.AUTHOR}, ${QuoteHistoryContract.PROVIDER}) " +
                            "SELECT ${QuoteHistoryContract.ID}, ${QuoteHistoryContract.TEXT}, " +
                            "${QuoteHistoryContract.SOURCE}, ${QuoteHistoryContract.MD5}, " +
                            "${QuoteHistoryContract.AUTHOR}, ${QuoteHistoryContract.PROVIDER} " +
                            "FROM tmp_table"
                )
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}