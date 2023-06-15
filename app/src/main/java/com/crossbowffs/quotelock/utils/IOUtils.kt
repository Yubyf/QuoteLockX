@file:JvmName("IOUtils")

package com.crossbowffs.quotelock.utils

import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.FloatRange
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentLength
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import org.jsoup.nodes.Document
import java.io.*

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
            sb.appendRange(buffer, 0, count)
        }
    }
    return sb.toString()
}

@Throws(IOException::class)
suspend inline fun <reified T> HttpClient.fetchJson(
    url: String,
    headers: Map<String, String?>? = null,
): T = fetchAny(url, headers) { }

@Throws(IOException::class)
suspend fun HttpClient.fetchXml(
    url: String,
    headers: Map<String, String?>? = null,
): Document = fetchAny<Document>(url, headers) { }

@Throws(IOException::class)
suspend inline fun <reified T> HttpClient.fetchAny(
    url: String,
    headers: Map<String, String?>? = null,
    crossinline block: HttpClientConfig<*>.() -> Unit,
): T = run {
    val response = config {
        block()
    }.get(url) {
        headers {
            headers?.forEach { (key, value) -> append(key, value!!) }
        }
    }
    if (response.status == HttpStatusCode.OK) {
        response.body<T>()
    } else {
        throw IOException("Server returned non-200 status code: ${response.status.description}")
    }
}

@Throws(IOException::class)
suspend fun HttpClient.fetchFile(
    url: String,
    headers: Map<String, String>? = null,
    file: File,
): Flow<DownloadState> = callbackFlow {
    send(DownloadState.Start)
    val response = get(url) {
        headers {
            headers?.forEach { (key, value) -> append(key, value) }
        }
        onDownload { bytesSentTotal, contentLength ->
            send(DownloadState.Downloading(bytesSentTotal.toFloat() / contentLength.toFloat()))
        }
    }
    response.contentLength()
    if (response.status == HttpStatusCode.OK) {
        response.bodyAsChannel().copyAndClose(file.writeChannel())
    } else {
        throw IOException("Server returned non-200 status code: ${response.status.description}")
    }
    send(DownloadState.End)
    close()
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
            compress(
                when (extension.lowercase()) {
                    "jpg",
                    "jpeg",
                    -> Bitmap.CompressFormat.JPEG

                    "webp" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        Bitmap.CompressFormat.WEBP_LOSSY
                    } else {
                        @Suppress("DEPRECATION")
                        Bitmap.CompressFormat.WEBP
                    }

                    "png" -> Bitmap.CompressFormat.PNG
                    else -> Bitmap.CompressFormat.PNG
                },
                90, fos
            )
            fos.flush()
            fos.close()
            true
        }.getOrDefault(false)
    }

sealed class DownloadState {
    object Start : DownloadState()
    data class Downloading(@FloatRange(0.0, 1.0) val progress: Float) : DownloadState()
    object End : DownloadState()
}