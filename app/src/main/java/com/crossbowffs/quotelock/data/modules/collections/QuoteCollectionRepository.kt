package com.crossbowffs.quotelock.data.modules.collections

import android.net.Uri
import com.crossbowffs.quotelock.data.api.QuoteData
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

    fun search(keyword: String): Flow<List<QuoteCollectionEntity>> =
        collectionDao.searchStream(keyword)

    suspend fun getByUid(uid: String): QuoteCollectionEntity? = withContext(dispatcher) {
        collectionDao.getByUid(uid)
    }

    suspend fun getRandomItem(): QuoteCollectionEntity? = withContext(dispatcher) {
        collectionDao.getRandomItem()
    }

    suspend fun insert(quoteData: QuoteData): Long? =
        insert(
            QuoteCollectionEntity(
                text = quoteData.quoteText,
                source = quoteData.quoteSource,
                author = quoteData.quoteAuthor,
                provider = quoteData.provider,
                uid = quoteData.uid,
                extra = quoteData.extra
            )
        )

    suspend fun insert(quote: QuoteCollectionEntity): Long? = withContext(dispatcher) {
        collectionDao.insert(quote)
    }

    suspend fun insert(quotes: List<QuoteCollectionEntity>): Array<Long> = withContext(dispatcher) {
        collectionDao.insert(quotes)
    }

    suspend fun count(): Int = collectionDao.count()

    suspend fun delete(id: Long): Int = withContext(dispatcher) {
        collectionDao.delete(id)
    }

    suspend fun delete(uid: String): Int = withContext(dispatcher) {
        collectionDao.delete(uid)
    }

    suspend fun clear() = withContext(dispatcher) {
        collectionDao.clear()
    }
    //endregion

    //region Backup
    suspend fun exportDatabase() = withContext(dispatcher) {
        localBackupSource.exportDb(QuoteCollectionContract.DATABASE_NAME)
    }

    suspend fun importDatabase(fileUri: Uri, merge: Boolean = false) = withContext(dispatcher) {
        localBackupSource.importDb(QuoteCollectionContract.DATABASE_NAME, fileUri, merge)
    }

    suspend fun exportCsv() = withContext(dispatcher) {
        localBackupSource.exportCsv(QuoteCollectionContract.DATABASE_NAME)
    }

    suspend fun importCsv(fileUri: Uri, merge: Boolean = false) = withContext(dispatcher) {
        localBackupSource.importCsv(fileUri, merge)
    }
    //endregion

    //region Sync
    fun updateDriveService() = remoteSyncSource.updateDriveService()

    fun ensureDriveService(): Boolean = remoteSyncSource.ensureDriveService()

    fun getGDriveFileTimestamp(): Flow<Long> = remoteSyncSource.cloudFileTimestampFlow

    suspend fun queryDriveFileTimestamp() =
        remoteSyncSource.queryDriveFileTimestamp(QuoteCollectionContract.DATABASE_NAME)

    suspend fun gDriveBackup() =
        remoteSyncSource.performDriveBackup(QuoteCollectionContract.DATABASE_NAME)

    suspend fun gDriveRestore(merge: Boolean = false) =
        remoteSyncSource.performDriveRestore(QuoteCollectionContract.DATABASE_NAME, merge)

    suspend fun gDriveBackupSync() =
        remoteSyncSource.performSafeDriveBackupSync(QuoteCollectionContract.DATABASE_NAME)

    suspend fun gDriveRestoreSync(merge: Boolean = false) =
        remoteSyncSource.performSafeDriveRestoreSync(QuoteCollectionContract.DATABASE_NAME, merge)
    //endregion
}