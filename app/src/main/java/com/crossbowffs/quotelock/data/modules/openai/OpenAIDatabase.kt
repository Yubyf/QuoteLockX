package com.crossbowffs.quotelock.data.modules.openai

import android.content.Context
import android.provider.BaseColumns
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.crossbowffs.quotelock.data.modules.openai.OpenAIUsageContract.DATABASE_NAME
import kotlinx.coroutines.flow.Flow

const val OPENAI_USAGE_DB_VERSION = 1

object OpenAIUsageContract {
    const val DATABASE_NAME = "openai_usage.db"

    const val TABLE = "usage"

    const val ID = BaseColumns._ID
    const val API_KEY = "api_key"
    const val MODEL = "model"
    const val TOKENS = "tokens"
    const val TIMESTAMP = "timestamp"
}

@Entity(tableName = OpenAIUsageContract.TABLE)
data class OpenAIUsageEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = OpenAIUsageContract.ID)
    val id: Int? = null,
    @ColumnInfo(name = OpenAIUsageContract.API_KEY)
    val apiKey: String,
    @ColumnInfo(name = OpenAIUsageContract.MODEL)
    val model: String,
    @ColumnInfo(name = OpenAIUsageContract.TOKENS)
    val tokens: Int,
    @ColumnInfo(name = OpenAIUsageContract.TIMESTAMP)
    val timestamp: Long = System.currentTimeMillis(),
)

@Dao
interface OpenAIUsageDao {

    @Query(
        "SELECT * FROM ${OpenAIUsageContract.TABLE} WHERE ${OpenAIUsageContract.API_KEY} = :apiKey " +
                "AND ${OpenAIUsageContract.TIMESTAMP} >= (:startDate / 1000)"
    )
    fun getUsageByApiKeyStream(apiKey: String, startDate: Long): Flow<List<OpenAIUsageEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(quote: OpenAIUsageEntity): Long?

    @Delete
    suspend fun delete(quote: OpenAIUsageEntity): Int

    @Query("DELETE FROM ${OpenAIUsageContract.TABLE} WHERE ${OpenAIUsageContract.ID} = :id")
    suspend fun delete(id: Long): Int

    @Query("DELETE FROM ${OpenAIUsageContract.TABLE}")
    suspend fun deleteAll()
}

@Database(entities = [OpenAIUsageEntity::class], version = OPENAI_USAGE_DB_VERSION)
abstract class OpenAIUsageDatabase : RoomDatabase() {
    abstract fun dao(): OpenAIUsageDao

    companion object {
        @Volatile
        private var INSTANCE: OpenAIUsageDatabase? = null

        fun getDatabase(context: Context): OpenAIUsageDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OpenAIUsageDatabase::class.java,
                    DATABASE_NAME
                ).setJournalMode(JournalMode.TRUNCATE).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}