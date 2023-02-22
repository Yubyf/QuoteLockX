@file:JvmName("IOUtils")

package com.crossbowffs.quotelock.utils

import android.graphics.Bitmap
import com.crossbowffs.quotelock.consts.Urls
import com.yubyf.quotelockx.BuildConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

@Throws(IOException::class)
fun InputStream.readString(
    encoding: String? = "UTF-8",
    bufferSize: Int = 2048,
): String {
    val buffer = CharArray(bufferSize)
    val sb = StringBuilder()
    InputStreamReader(this, encoding).use { reader ->
        while (true) {
            val count = reader.read(buffer, 0, bufferSize)
            if (count < 0) break
            sb.append(buffer, 0, count)
        }
    }
    return sb.toString()
}

@Throws(IOException::class)
suspend fun String.downloadUrl(
    headers: Map<String, String?>? = null,
): String = runInterruptible(Dispatchers.IO) {
    val ua = "QuoteLockX/${BuildConfig.VERSION_NAME} (+${Urls.GITHUB_QUOTELOCK})"
    val connection = URL(this@downloadUrl).openConnection() as HttpURLConnection
    try {
        connection.connectTimeout = 3000
        connection.readTimeout = 3000
        connection.setRequestProperty("User-Agent", ua)
        headers?.run {
            forEach { (key, value) -> connection.addRequestProperty(key, value) }
        }
        val responseCode = connection.responseCode
        if (connection.responseCode == 200) {
            connection.inputStream.readString()
        } else {
            throw IOException("Server returned non-200 status code: $responseCode")
        }
    } finally {
        connection.disconnect()
    }
}

@Throws(IOException::class)
fun InputStream.toFile(file: File) = use { inputStream ->
    val fos: OutputStream = FileOutputStream(file)
    val buffer = ByteArray(1024)
    var length: Int
    while (inputStream.read(buffer).also { length = it } > 0) {
        fos.write(buffer, 0, length)
    }
    fos.flush()
    fos.close()
}

@Throws(IOException::class)
fun OutputStream.fromFile(file: File) = use { outputStream ->
    val fis = FileInputStream(file)
    val buffer = ByteArray(1024)
    var length: Int
    fis.use {
        while ((fis.read(buffer).also { length = it }) > 0) {
            outputStream.write(buffer, 0, length)
        }
    }
    outputStream.flush()
}

@Throws(IOException::class)
suspend fun Bitmap.toFile(file: File, dispatcher: CoroutineDispatcher = Dispatchers.IO): Boolean =
    withContext(dispatcher) {
        file.runCatching {
            if (parentFile?.exists() != true && parentFile?.mkdirs() != true) {
                return@runCatching false
            }
            if (!exists()) {
                createNewFile()
            }
            val fos: OutputStream = FileOutputStream(this)
            compress(Bitmap.CompressFormat.PNG, 90, fos)
            fos.flush()
            fos.close()
            true
        }.getOrDefault(false)
    }