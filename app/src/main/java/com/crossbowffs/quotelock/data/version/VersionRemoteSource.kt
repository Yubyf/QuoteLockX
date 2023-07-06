package com.crossbowffs.quotelock.data.version

import com.crossbowffs.quotelock.consts.Urls
import com.crossbowffs.quotelock.data.api.AndroidUpdateInfo
import com.crossbowffs.quotelock.utils.fetchFile
import com.crossbowffs.quotelock.utils.fetchFileInfo
import com.crossbowffs.quotelock.utils.fetchJson
import io.ktor.client.HttpClient
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VersionRemoteSource @Inject constructor(private val httpClient: HttpClient) {

    suspend fun fetchUpdate(): VersionResponse =
        httpClient.fetchJson<VersionResponse>(Urls.VERSION_JSON_URL)

    suspend fun fetchUpdateFileInfo(url: String) = httpClient.fetchFileInfo(url = url)

    suspend fun fetchUpdateFile(url: String, file: File) =
        httpClient.fetchFile(url = url, file = file)

    companion object {
        private const val TAG = "VersionRemoteSource"
    }
}

@Serializable
data class VersionResponse(
    @SerialName("android")
    val androidUpdateInfo: AndroidUpdateInfo,
)