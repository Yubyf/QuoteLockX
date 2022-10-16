package com.crossbowffs.quotelock.data

import com.crossbowffs.quotelock.data.api.AndroidString

/**
 * @author Yubyf
 */
sealed class AsyncResult<out R> {
    data class Success<out T>(val data: T) : AsyncResult<T>()
    sealed class Error : AsyncResult<Nothing>() {
        data class ExceptionWrapper(val exception: Exception) : Error()
        data class Message(val message: AndroidString) : Error()
    }

    data class Loading(val message: AndroidString) : AsyncResult<Nothing>()
}

val AsyncResult<*>.succeeded get() = this is AsyncResult.Success && data != null
val AsyncResult<*>.failed get() = this is AsyncResult.Error
val AsyncResult<*>?.exceptionMessage
    get() = if (this is AsyncResult.Error.ExceptionWrapper) exception.message else ""