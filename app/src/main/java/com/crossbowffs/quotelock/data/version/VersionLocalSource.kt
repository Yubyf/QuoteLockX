package com.crossbowffs.quotelock.data.version

import com.crossbowffs.quotelock.consts.PREF_VERSION_DOWNLOAD_APK_PATH
import com.crossbowffs.quotelock.consts.PREF_VERSION_DOWNLOAD_APK_RANGE_IDENTIFIER
import com.crossbowffs.quotelock.consts.PREF_VERSION_DOWNLOAD_APK_TOTAL_SIZE
import com.crossbowffs.quotelock.consts.PREF_VERSION_DOWNLOAD_APK_VERSION_CODE
import com.crossbowffs.quotelock.consts.PREF_VERSION_DOWNLOAD_APK_VERSION_NAME
import com.crossbowffs.quotelock.di.VersionDataStore
import com.crossbowffs.quotelock.utils.ResumableFile
import com.yubyf.datastore.DataStoreDelegate
import kotlinx.coroutines.runBlocking
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VersionLocalSource @Inject constructor(
    @VersionDataStore private val versionDataStore: DataStoreDelegate,
) {
    fun getDownloadedApk(): ResumableApkFile? = runBlocking {
        val versionCode = versionDataStore.getIntSuspend(PREF_VERSION_DOWNLOAD_APK_VERSION_CODE, -1)
        val versionName =
            versionDataStore.getStringSuspend(PREF_VERSION_DOWNLOAD_APK_VERSION_NAME, "")!!
        val path = versionDataStore.getStringSuspend(PREF_VERSION_DOWNLOAD_APK_PATH)
        val totalSize =
            versionDataStore.getLongSuspend(PREF_VERSION_DOWNLOAD_APK_TOTAL_SIZE, 0L)
        val rangeIdentity =
            versionDataStore.getStringSuspend(PREF_VERSION_DOWNLOAD_APK_RANGE_IDENTIFIER)
        if (path.isNullOrBlank()) null else ResumableApkFile(
            versionCode,
            versionName,
            path,
            totalSize,
            rangeIdentity
        )
    }

    fun setDownloadedApk(
        versionCode: Int,
        versionName: String,
        file: File,
        totalSize: Long,
        rangeIdentifier: String,
    ) {
        versionDataStore.bulkPut(
            mapOf(
                PREF_VERSION_DOWNLOAD_APK_VERSION_CODE to versionCode,
                PREF_VERSION_DOWNLOAD_APK_VERSION_NAME to versionName,
                PREF_VERSION_DOWNLOAD_APK_PATH to file.absolutePath,
                PREF_VERSION_DOWNLOAD_APK_TOTAL_SIZE to totalSize,
                PREF_VERSION_DOWNLOAD_APK_RANGE_IDENTIFIER to rangeIdentifier
            )
        )
    }

    companion object {
        private const val TAG = "VersionLocalSource"
    }
}

data class ResumableApkFile(
    val versionCode: Int,
    val versionName: String,
    override val downloadedPath: String,
    override val totalSize: Long = 0,
    override val rangeIdentity: String? = null,
) : ResumableFile(
    downloadedPath = downloadedPath,
    totalSize = totalSize,
    rangeIdentity = rangeIdentity,
)