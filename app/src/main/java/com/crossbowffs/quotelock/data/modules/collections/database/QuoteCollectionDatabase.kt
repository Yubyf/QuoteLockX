package com.crossbowffs.quotelock.data.modules.collections.database

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
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
import com.crossbowffs.quotelock.data.modules.collections.database.QuoteCollectionContract.DATABASE_NAME
import com.crossbowffs.quotelock.utils.decodeHex
import com.crossbowffs.quotelock.utils.hexString
import com.crossbowffs.quotelock.utils.md5
import com.opencsv.bean.AbstractBeanField
import com.opencsv.bean.CsvBindByName
import com.opencsv.bean.CsvBindByNames
import com.opencsv.bean.CsvCustomBindByName
import com.opencsv.bean.HeaderColumnNameMappingStrategy
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

    const val LEGACY_UID = QuoteEntityContract.LEGACY_UID
    const val TEXT = QuoteEntityContract.TEXT
    const val SOURCE = QuoteEntityContract.SOURCE
    const val AUTHOR = QuoteEntityContract.AUTHOR
    const val ID = QuoteEntityContract.ID
    const val PROVIDER = QuoteEntityContract.PROVIDER
    const val UID = QuoteEntityContract.UID
    const val EXTRA = QuoteEntityContract.EXTRA
}

internal class QuoteCollectionHeaderColumnNameMappingStrategy :
    HeaderColumnNameMappingStrategy<QuoteCollectionEntity>() {
    override fun getColumnName(col: Int): String? = headerIndex.getByPosition(col)?.let {
        if (it == QuoteCollectionContract.LEGACY_UID.uppercase()) {
            QuoteCollectionContract.UID.uppercase()
        } else it
    }
}

internal val collectionCsvStrategy: QuoteCollectionHeaderColumnNameMappingStrategy =
    QuoteCollectionHeaderColumnNameMappingStrategy().apply {
        type = QuoteCollectionEntity::class.java
    }

@Entity(
    tableName = QuoteCollectionContract.TABLE,
    indices = [Index(value = [QuoteCollectionContract.UID], unique = true)]
)
data class QuoteCollectionEntity @JvmOverloads constructor(
    @CsvBindByName(column = QuoteCollectionContract.ID, required = true)
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = QuoteCollectionContract.ID)
    override var id: Int? = null,
    @CsvBindByName(column = QuoteCollectionContract.TEXT, required = true)
    @ColumnInfo(name = QuoteCollectionContract.TEXT)
    override var text: String,
    @CsvBindByName(column = QuoteCollectionContract.SOURCE)
    @ColumnInfo(name = QuoteCollectionContract.SOURCE)
    override var source: String,
    @CsvBindByName(column = QuoteCollectionContract.AUTHOR)
    @ColumnInfo(name = QuoteCollectionContract.AUTHOR)
    override var author: String = "",
    @CsvBindByName(column = QuoteCollectionContract.PROVIDER)
    @ColumnInfo(name = QuoteCollectionContract.PROVIDER)
    override var provider: String = "",
    @CsvBindByNames(
        CsvBindByName(column = QuoteCollectionContract.UID),
        CsvBindByName(column = QuoteCollectionContract.LEGACY_UID)
    )
    @ColumnInfo(name = QuoteCollectionContract.UID)
    override var uid: String,
    @CsvCustomBindByName(
        column = QuoteCollectionContract.EXTRA,
        converter = CsvByteArrayConverter::class
    )
    @ColumnInfo(name = QuoteCollectionContract.EXTRA, typeAffinity = ColumnInfo.BLOB)
    override val extra: ByteArray? = null,
) : QuoteEntity {
    // Empty constructor for OpenCSV
    @Suppress("unused")
    constructor() : this(
        id = null,
        text = "",
        source = "",
        author = "",
        provider = "",
        uid = "".md5(),
        extra = null
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QuoteCollectionEntity

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

class CsvByteArrayConverter : AbstractBeanField<ByteArray?, String>() {
    override fun convert(value: String): ByteArray? = value.takeIf { it.isNotBlank() }?.decodeHex()

    override fun convertToWrite(value: Any?): String? = (value as? ByteArray)?.hexString()
}

@Dao
interface QuoteCollectionDao {
    @Query("SELECT * FROM ${QuoteCollectionContract.TABLE}")
    fun getAllStream(): Flow<List<QuoteCollectionEntity>>

    @Query(
        "SELECT * FROM ${QuoteCollectionContract.TABLE} WHERE (text LIKE '%' || :keyword || '%'" +
                " OR source LIKE '%' || :keyword || '%'" +
                " OR author LIKE '%' || :keyword || '%')"
    )
    fun searchStream(keyword: String): Flow<List<QuoteCollectionEntity>>

    @Query("SELECT * FROM ${QuoteCollectionContract.TABLE}")
    suspend fun getAll(): List<QuoteCollectionEntity>

    @Query(
        """SELECT * FROM ${QuoteCollectionContract.TABLE}"""
                + """ WHERE ${QuoteCollectionContract.TEXT} = :text"""
                + """ AND ${QuoteCollectionContract.SOURCE} = :source"""
                + """ AND ${QuoteCollectionContract.AUTHOR} = :author"""
    )
    suspend fun getByQuote(text: String, source: String, author: String?): QuoteCollectionEntity?

    @Query(
        "SELECT * FROM ${QuoteCollectionContract.TABLE} WHERE ${QuoteCollectionContract.UID} = :uid"
    )
    suspend fun getByUid(uid: String): QuoteCollectionEntity?

    @Query("SELECT * FROM ${QuoteCollectionContract.TABLE} ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomItem(): QuoteCollectionEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(quote: QuoteCollectionEntity): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(quotes: List<QuoteCollectionEntity>): Array<Long>

    @Query("SELECT COUNT(*) FROM ${QuoteCollectionContract.TABLE}")
    fun countStream(): Flow<Int>

    @Query("SELECT COUNT(*) FROM ${QuoteCollectionContract.TABLE}")
    suspend fun count(): Int

    @Query("DELETE FROM ${QuoteCollectionContract.TABLE} WHERE ${QuoteCollectionContract.ID} = :id")
    suspend fun delete(id: Long): Int

    @Query("DELETE FROM ${QuoteCollectionContract.TABLE} WHERE ${QuoteCollectionContract.UID} = :uid")
    suspend fun delete(uid: String): Int

    @Query("DELETE FROM ${QuoteCollectionContract.TABLE}")
    suspend fun clear()
}

@Database(
    entities = [QuoteCollectionEntity::class],
    version = BuildConfig.QUOTE_COLLECTIONS_DB_VERSION
)
abstract class QuoteCollectionDatabase : RoomDatabase() {
    abstract fun dao(): QuoteCollectionDao

    companion object {
        @Volatile
        private var INSTANCE: QuoteCollectionDatabase? = null
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Empty implementation, because the schema isn't changing.
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE ${QuoteCollectionContract.TABLE}" +
                            " ADD COLUMN ${QuoteEntityContract.AUTHOR_OLD} TEXT"
                )
            }
        }
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE ${QuoteCollectionContract.TABLE} RENAME TO tmp_table"
                )
                database.execSQL(
                    "CREATE TABLE ${QuoteCollectionContract.TABLE}(" +
                            "${QuoteCollectionContract.ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "${QuoteCollectionContract.TEXT} TEXT NOT NULL, " +
                            "${QuoteCollectionContract.SOURCE} TEXT NOT NULL, " +
                            "${QuoteCollectionContract.LEGACY_UID} TEXT UNIQUE NOT NULL, " +
                            "${QuoteCollectionContract.AUTHOR} TEXT NOT NULL ON CONFLICT REPLACE DEFAULT '')"
                )
                database.execSQL(
                    "CREATE UNIQUE INDEX index_" +
                            "${"${QuoteCollectionContract.TABLE}_${QuoteCollectionContract.LEGACY_UID}"} " +
                            "on ${QuoteCollectionContract.TABLE}(${QuoteCollectionContract.LEGACY_UID})"
                )
                database.execSQL(
                    "INSERT OR REPLACE INTO ${QuoteCollectionContract.TABLE}(" +
                            "${QuoteCollectionContract.ID}, ${QuoteCollectionContract.TEXT}, " +
                            "${QuoteCollectionContract.SOURCE}, ${QuoteCollectionContract.LEGACY_UID}, " +
                            "${QuoteCollectionContract.AUTHOR}) " +
                            "SELECT ${QuoteCollectionContract.ID}, ${QuoteCollectionContract.TEXT}, " +
                            "${QuoteCollectionContract.SOURCE}, ${QuoteCollectionContract.LEGACY_UID}, " +
                            "${QuoteEntityContract.AUTHOR_OLD} " +
                            "FROM tmp_table"
                )
                database.execSQL("DROP TABLE tmp_table")
            }
        }
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE ${QuoteCollectionContract.TABLE}" +
                            " ADD COLUMN ${QuoteCollectionContract.PROVIDER} TEXT DEFAULT '' NOT NULL"
                )
                database.execSQL(
                    "ALTER TABLE ${QuoteCollectionContract.TABLE}" +
                            " ADD COLUMN ${QuoteCollectionContract.EXTRA} BLOB DEFAULT NULL"
                )
                database.execSQL(
                    "ALTER TABLE ${QuoteCollectionContract.TABLE} RENAME TO tmp_table"
                )
                database.execSQL(
                    "CREATE TABLE ${QuoteCollectionContract.TABLE}(" +
                            "${QuoteCollectionContract.ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "${QuoteCollectionContract.TEXT} TEXT NOT NULL, " +
                            "${QuoteCollectionContract.SOURCE} TEXT NOT NULL, " +
                            "${QuoteCollectionContract.AUTHOR} TEXT NOT NULL ON CONFLICT REPLACE DEFAULT '', " +
                            "${QuoteCollectionContract.PROVIDER} TEXT NOT NULL, " +
                            "${QuoteCollectionContract.UID} TEXT UNIQUE NOT NULL, " +
                            "${QuoteCollectionContract.EXTRA} BLOB DEFAULT NULL)"
                )
                database.execSQL(
                    "CREATE UNIQUE INDEX index_" +
                            "${"${QuoteCollectionContract.TABLE}_${QuoteCollectionContract.UID}"} " +
                            "on ${QuoteCollectionContract.TABLE}(${QuoteCollectionContract.UID})"
                )
                database.execSQL(
                    "INSERT OR REPLACE INTO ${QuoteCollectionContract.TABLE}(" +
                            "${QuoteCollectionContract.ID}, ${QuoteCollectionContract.TEXT}, " +
                            "${QuoteCollectionContract.SOURCE}, ${QuoteCollectionContract.AUTHOR}, " +
                            "${QuoteCollectionContract.PROVIDER}, ${QuoteCollectionContract.UID}) " +
                            "SELECT ${QuoteCollectionContract.ID}, ${QuoteCollectionContract.TEXT}, " +
                            "${QuoteCollectionContract.SOURCE}, ${QuoteCollectionContract.AUTHOR}, " +
                            "${QuoteEntityContract.PROVIDER}, ${QuoteCollectionContract.LEGACY_UID} " +
                            "FROM tmp_table"
                )
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .createFromFile(file)
                    .build()
            }
        }
    }
}