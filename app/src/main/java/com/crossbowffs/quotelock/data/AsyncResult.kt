package com.crossbowffs.quotelock.data

/**
 * @author Yubyf
 */
sealed class AsyncResult<out R> {
    data class Success<out T>(val data: T) : AsyncResult<T>()
    data class Error(val exception: Exception) : AsyncResult<Nothing>()
    data class Loading(val message: String) : AsyncResult<Nothing>()
}

val AsyncResult<*>.succeeded get() = this is AsyncResult.Success && data != null
val AsyncResult<*>.failed get() = this is AsyncResult.Error
val AsyncResult<*>.failedMessage get() = if (this is AsyncResult.Error) exception.message else ""