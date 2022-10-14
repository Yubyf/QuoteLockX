package com.crossbowffs.quotelock.data.history

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class QuoteHistoryRepository internal constructor(
    private val historyDao: QuoteHistoryDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    fun getAll(): Flow<List<QuoteHistoryEntity>> = historyDao.getAllStream()

    fun search(keyword: String): Flow<List<QuoteHistoryEntity>> = historyDao.searchStream(keyword)

    fun count(): Flow<Int> = historyDao.countStream()

    suspend fun insert(quote: QuoteHistoryEntity): Long? = withContext(dispatcher) {
        historyDao.insert(quote)
    }

    suspend fun delete(quote: QuoteHistoryEntity): Int = withContext(dispatcher) {
        historyDao.delete(quote)
    }

    suspend fun delete(id: Long): Int = withContext(dispatcher) {
        historyDao.delete(id)
    }

    suspend fun deleteAll() = withContext(dispatcher) {
        historyDao.deleteAll()
    }
}