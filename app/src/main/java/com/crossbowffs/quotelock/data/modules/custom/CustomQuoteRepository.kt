package com.crossbowffs.quotelock.data.modules.custom

import com.crossbowffs.quotelock.data.modules.custom.database.CustomQuoteDao
import com.crossbowffs.quotelock.data.modules.custom.database.CustomQuoteEntity
import com.crossbowffs.quotelock.di.DISPATCHER_IO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class CustomQuoteRepository internal constructor(
    private val customQuoteDao: CustomQuoteDao,
    @Named(DISPATCHER_IO) private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
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