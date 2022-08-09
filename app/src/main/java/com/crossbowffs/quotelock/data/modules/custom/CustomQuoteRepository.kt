package com.crossbowffs.quotelock.data.modules.custom

import com.crossbowffs.quotelock.data.modules.custom.database.CustomQuoteDao
import com.crossbowffs.quotelock.data.modules.custom.database.CustomQuoteEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CustomQuoteRepository internal constructor(
    private val customQuoteDao: CustomQuoteDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    fun getAll(): Flow<List<CustomQuoteEntity>> = customQuoteDao.getAllStream()

    suspend fun getById(id: Long): CustomQuoteEntity? = withContext(dispatcher) {
        customQuoteDao.getById(id)
    }

    suspend fun getRandomItem(): CustomQuoteEntity? = withContext(dispatcher) {
        customQuoteDao.getRandomItem()
    }

    suspend fun update(quote: CustomQuoteEntity) = withContext(dispatcher) {
        customQuoteDao.update(quote)
    }

    suspend fun insert(quote: CustomQuoteEntity): Long? = withContext(dispatcher) {
        customQuoteDao.insert(quote)
    }

    suspend fun delete(id: Long): Int = withContext(dispatcher) {
        customQuoteDao.delete(id)
    }
}