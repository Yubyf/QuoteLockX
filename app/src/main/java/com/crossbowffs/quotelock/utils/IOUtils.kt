@file:JvmName("IOUtils")

package com.crossbowffs.quotelock.utils

import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.FloatRange
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.head
import io.ktor.client.request.headers
import io.ktor.client.request.prepareGet
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.appendPathSegments
import io.ktor.http.contentLength
import io.ktor.http.contentType
import io.ktor.http.etag
import io.ktor.utils.io.jvm.javaio.copyTo
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
suspend inline fun <reified T> HttpClient.fetchCustom(
    url: String,
    path: String? = null,
    method: HttpMethod = HttpMethod.Get,
    headers: Map<String, String?>? = null,
    queries: Map<String, String>? = null,
    body: Any? = null,
    converter: (String) -> T,
): T =
    converter(
        fetchString(
            url = url,
            path = path,
            method = method,
            headers = headers,
            queries = queries,
            body = body
        )
    )

@Throws(IOException::class)
suspend fun HttpClient.fetchString(
    url: String,
    path: String? = null,
    method: HttpMethod = HttpMethod.Get,
    headers: Map<String, String?>? = null,
    queries: Map<String, String>? = null,
    body: Any? = null,
): String = fetchAny(
    url = url,
    path = path,
    method = method,
    headers = headers,
    queries = queries,
    body = body
) { }

@Throws(IOException::class)
suspend inline fun <reified T> HttpClient.fetchJson(
    url: String,
    path: String? = null,
    method: HttpMethod = HttpMethod.Get,
    headers: Map<String, String?>? = null,
    queries: Map<String, String>? = null,
    body: Any? = null,
): T = fetchAny(
    url = url,
    path = path,
    method = method,
    headers = headers,
    queries = queries,
    body = body
) { }

@Throws(IOException::class)
suspend fun HttpClient.fetchXml(
    url: String,
    path: String? = null,
    method: HttpMethod = HttpMethod.Get,
    headers: Map<String, String?>? = null,
    queries: Map<String, String>? = null,
    body: Any? = null,
): Document =
    fetchAny<Document>(
        url = url,
        path = path,
        method = method,
        headers = headers,
        queries = queries,
        body = body
    ) { }

@Throws(IOException::class)
suspend inline fun <reified T> HttpClient.fetchAny(
    url: String,
    path: String? = null,
    method: HttpMethod = HttpMethod.Get,
    headers: Map<String, String?>? = null,
    queries: Map<String, String>? = null,
    body: Any? = null,
    crossinline block: HttpClientConfig<*>.() -> Unit,
): T = run {
    val response = config {
        block()
    }.request(url) {
        path?.let {
            url {
                appendPathSegments(path.split("/"))
            }
        }
        queries?.let { query ->
            url {
                query.forEach { (key, value) -> parameters.append(key, value) }
            }
        }
        this.method = method
        headers?.let {
            headers {
                it.forEach { (key, value) -> append(key, value!!) }
            }
        }
        body?.let {
            contentType(ContentType.Application.Json)
            setBody(it)
        }
    }
    if (response.status == HttpStatusCode.OK) {
        response.body<T>()
    } else {
        throw HttpException(
            response.status,
            "Server returned non-200 status code: ${response.status.description}"
        )
    }
}

class HttpException(val status: HttpStatusCode, message: String) : IOException(message)

@Throws(IOException::class)
suspend fun HttpClient.fetchFile(
    url: String,
    headers: Map<String, String>? = null,
    file: File,
): Flow<DownloadState> = callbackFlow {
    val resumable = file is ResumableFile && file.exists() && file.length() < file.totalSize
    send(if (resumable) DownloadState.Resume((file as ResumableFile).progress) else DownloadState.Start)
    prepareGet(url) {
        val progressOffset = if (resumable) (file as ResumableFile).progress else 0F
        headers {
            headers?.forEach { (key, value) -> append(key, value) }
            if (resumable) {
                append("Range", "bytes=${file.length()}-")
                (file as ResumableFile).rangeIdentity?.let { append("If-Range", it) }
            }
        }
        onDownload { bytesSentTotal, contentLength ->
            send(
                DownloadState.Downloading(
                    (bytesSentTotal.toFloat() / contentLength.toFloat()) * (1 - progressOffset)
                            + progressOffset
                )
            )
        }
    }.execute { response ->
        if (response.status == HttpStatusCode.OK || response.status == HttpStatusCode.PartialContent) {
            response.bodyAsChannel().copyTo(FileOutputStream(file, resumable))
        } else {
            throw IOException("Server returned non-200 status code: ${response.status.description}")
        }
    }
    send(DownloadState.End)
    close()
}

@Throws(IOException::class)
suspend fun HttpClient.fetchFileInfo(
    url: String,
    headers: Map<String, String>? = null,
): RemoteFileInfo = head(url) {
    headers {
        headers?.forEach { (key, value) -> append(key, value) }
    }
}.let { response ->
    if (response.status == HttpStatusCode.OK) {
        RemoteFileInfo(
            response.contentLength() ?: 0,
            response.headers[HttpHeaders.LastModified],
            response.etag()
        )
    } else {
        throw IOException("Server returned non-200 status code: ${response.status.description}")
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
    object Idle : DownloadState()
    object Start : DownloadState()
    data class Downloading(@FloatRange(0.0, 1.0) val progress: Float) : DownloadState()
    data class Resume(@FloatRange(0.0, 1.0) val progress: Float) : DownloadState()
    data class Pause(@FloatRange(0.0, 1.0) val progress: Float) : DownloadState()
    object End : DownloadState()
    data class Error(val throwable: Throwable) : DownloadState()

    val DownloadState.progress: Float
        get() = when (this) {
            is Downloading -> progress
            is Resume -> progress
            is Pause -> progress
            is End -> 1F
            else -> 0F
        }
}

data class RemoteFileInfo(
    val size: Long,
    private val lastModified: String?,
    private val eTag: String?,
) {
    val rangeIdentifier: String?
        get() = eTag ?: lastModified
}

abstract class ResumableFile(
    open val downloadedPath: String,
    open val totalSize: Long = 0,
    open val rangeIdentity: String? = null,
) : File(downloadedPath) {
    val progress: Float
        get() = if (totalSize == 0L) 0F else (length() * 100 / totalSize) / 100F

    val completed: Boolean
        get() = isFile && totalSize == length()

    fun isMatchRemoteFile(remoteFile: RemoteFileInfo): Boolean =
        remoteFile.size == totalSize && remoteFile.rangeIdentifier == rangeIdentity
}