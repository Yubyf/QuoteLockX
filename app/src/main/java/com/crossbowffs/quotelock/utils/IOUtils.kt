@file:JvmName("IOUtils")

package com.crossbowffs.quotelock.utils

import com.crossbowffs.quotelock.BuildConfig
import com.crossbowffs.quotelock.consts.Urls
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
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
fun String.downloadUrl(headers: Map<String, String?>? = null): String {
    val url = URL(this)
    val ua = "QuoteLock/${BuildConfig.VERSION_NAME} (+${Urls.GITHUB_QUOTELOCK})"
    val connection = url.openConnection() as HttpURLConnection
    return try {
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