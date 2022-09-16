package com.crossbowffs.quotelock.data.modules.collections

import android.net.Uri
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.data.api.md5
import com.crossbowffs.quotelock.data.modules.collections.backup.CollectionLocalBackupSource
import com.crossbowffs.quotelock.data.modules.collections.backup.CollectionRemoteSyncSource
import com.crossbowffs.quotelock.data.modules.collections.database.QuoteCollectionContract
import com.crossbowffs.quotelock.data.modules.collections.database.QuoteCollectionDao
import com.crossbowffs.quotelock.data.modules.collections.database.QuoteCollectionEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class QuoteCollectionRepository internal constructor(
    private val collectionDao: QuoteCollectionDao,
    private val localBackupSource: CollectionLocalBackupSource,
    private val remoteSyncSource: CollectionRemoteSyncSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    //region Data list

    fun getAllStream(): Flow<List<QuoteCollectionEntity>> = collectionDao.getAllStream()

    suspend fun getByQuote(text: String, source: String, author: String?): QuoteCollectionEntity? =
        withContext(dispatcher) {
            collectionDao.getByQuote(text, source, author)
        }

    suspend fun getRandomItem(): QuoteCollectionEntity? = withContext(dispatcher) {
        collectionDao.getRandomItem()
    }

    suspend fun insert(quoteData: QuoteData): Long? =
        insert(QuoteCollectionEntity(
            text = quoteData.quoteText,
            source = quoteData.quoteSource,
            author = quoteData.quoteAuthor,
            md5 = quoteData.md5)
        )

    suspend fun insert(quote: QuoteCollectionEntity): Long? = withContext(dispatcher) {
        collectionDao.insert(quote)
    }

    suspend fun insert(quotes: List<QuoteCollectionEntity>): Array<Long> = withContext(dispatcher) {
        collectionDao.insert(quotes)
    }

    fun count(): Flow<Int> = collectionDao.countStream()

    suspend fun delete(id: Long): Int = withContext(dispatcher) {
        collectionDao.delete(id)
    }

    suspend fun delete(md5: String): Int = withContext(dispatcher) {
        collectionDao.delete(md5)
    }

    suspend fun clear() = withContext(dispatcher) {
        collectionDao.clear()
    }
    //endregion

    //region Backup
    suspend fun exportDatabase() = withContext(dispatcher) {
        localBackupSource.exportDb(QuoteCollectionContract.DATABASE_NAME)
    }

    suspend fun importDatabase(fileUri: Uri) = withContext(dispatcher) {
        localBackupSource.importDb(QuoteCollectionContract.DATABASE_NAME, fileUri)
    }

    suspend fun exportCsv() = withContext(dispatcher) {
        localBackupSource.exportCsv(QuoteCollectionContract.DATABASE_NAME)
    }

    suspend fun importCsv(fileUri: Uri) = withContext(dispatcher) {
        localBackupSource.importCsv(fileUri)
    }
    //endregion

    //region Sync
    fun updateDriveService() = remoteSyncSource.updateDriveService()

    fun ensureDriveService(): Boolean = remoteSyncSource.ensureDriveService()

    suspend fun getGDriveFileTimestamp(): Flow<Long> {
        remoteSyncSource.queryDriveFileTimestamp(QuoteCollectionContract.DATABASE_NAME)
        return remoteSyncSource.cloudFileTimestampFlow
    }

    suspend fun gDriveBackup() =
        remoteSyncSource.performDriveBackup(QuoteCollectionContract.DATABASE_NAME)

    suspend fun gDriveRestore() =
        remoteSyncSource.performDriveRestore(QuoteCollectionContract.DATABASE_NAME)

    fun gDriveBackupSync() =
        remoteSyncSource.performSafeDriveBackupSync(QuoteCollectionContract.DATABASE_NAME)

    fun gDriveRestoreSync() =
        remoteSyncSource.performSafeDriveRestoreSync(QuoteCollectionContract.DATABASE_NAME)
    //endregion
}