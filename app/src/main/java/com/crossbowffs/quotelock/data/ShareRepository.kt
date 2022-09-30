package com.crossbowffs.quotelock.data

import android.graphics.Bitmap
import com.crossbowffs.quotelock.app.App
import com.crossbowffs.quotelock.consts.PREF_SHARE_IMAGE_CHILD_PATH
import com.crossbowffs.quotelock.consts.PREF_SHARE_IMAGE_EXTENSION
import com.crossbowffs.quotelock.di.IoDispatcher
import com.crossbowffs.quotelock.utils.toFile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareRepository @Inject internal constructor(
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) {
    private val shareDir = File(App.instance.getExternalFilesDir(null), PREF_SHARE_IMAGE_CHILD_PATH)

    private val _cacheSizeFlow = MutableStateFlow(-1L)
    val cacheSizeFlow = _cacheSizeFlow.asStateFlow()

    suspend fun saveBitmap(bitmap: Bitmap): File = File(shareDir,
        UUID.randomUUID().toString() + PREF_SHARE_IMAGE_EXTENSION).also {
        bitmap.toFile(it, dispatcher)
    }

    suspend fun clearCache() = withContext(dispatcher) {
        shareDir.deleteRecursively()
        shareDir.mkdirs()
        _cacheSizeFlow.value = 0
    }

    suspend fun calcCacheSizeBytes() = withContext(dispatcher) {
        _cacheSizeFlow.value = shareDir.listFiles()?.sumOf { it.length() } ?: 0
    }
}