package com.crossbowffs.quotelock.data.version

import android.content.Context
import android.os.Environment
import com.crossbowffs.quotelock.consts.PREF_DOWNLOAD_APK_EXTENSION
import com.crossbowffs.quotelock.consts.PREF_DOWNLOAD_CHANGELOG_EXTENSION
import com.crossbowffs.quotelock.data.api.AndroidUpdateInfo
import com.crossbowffs.quotelock.di.IoDispatcher
import com.crossbowffs.quotelock.utils.DownloadState
import com.crossbowffs.quotelock.utils.DownloadState.Start.progress
import com.crossbowffs.quotelock.utils.Xlog
import com.yubyf.quotelockx.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

sealed class UpdateInfo(
    open val versionCode: Int = -1,
    open val versionName: String = "",
    open val url: String = "",
    open val changelog: String = "",
) {
    object NoUpdate : UpdateInfo()
    data class LocalUpdate(
        override val versionCode: Int,
        override val versionName: String,
        override val url: String,
        override val changelog: String = "",
        val instantInstall: Boolean = false,
    ) : UpdateInfo(versionCode, versionName, changelog, url)

    data class RemoteUpdate(
        override val versionCode: Int,
        override val versionName: String,
        override val url: String,
        override val changelog: String = "",
        val downloadState: DownloadState = DownloadState.Idle,
    ) : UpdateInfo(versionCode, versionName, changelog, url)

    val hasUpdate: Boolean
        get() = this !is NoUpdate
}

@Singleton
class VersionRepository @Inject internal constructor(
    @ApplicationContext context: Context,
    private val versionRemoteSource: VersionRemoteSource,
    private val versionLocalSource: VersionLocalSource,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) {

    private val downloadDir: File =
        context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.apply {
            if (!exists()) {
                mkdirs()
            }
        }

    private val _updateInfoFlow = MutableStateFlow<UpdateInfo>(UpdateInfo.NoUpdate)
    val updateInfoFlow = _updateInfoFlow.asStateFlow()

    private val fileFetchCoroutineScope = CoroutineScope(dispatcher)
    private var fileFetchDeferred: Deferred<*>? = null

    suspend fun fetchUpdate() = runCatching {
        versionRemoteSource.fetchUpdate().androidUpdateInfo.apply {
            Xlog.d(TAG, "fetch update success")
            _updateInfoFlow.value = toUpdateInfo()
        }
    }.onFailure { exception ->
        Xlog.e(TAG, "fetch update failed", exception)
        _updateInfoFlow.value = versionLocalSource.getDownloadedApk()?.takeIf {
            it.versionCode > BuildConfig.VERSION_CODE && it.completed
        }?.let {
            UpdateInfo.LocalUpdate(
                it.versionCode,
                it.versionName,
                it.absolutePath,
                getLocalChangelogContent(it.versionName),
                instantInstall = false
            )
        } ?: UpdateInfo.NoUpdate
        throw exception
    }.getOrNull()?.takeIf { it.changelog.isNotBlank() }?.let {
        val changelogFile = File(downloadDir, it.versionName + PREF_DOWNLOAD_CHANGELOG_EXTENSION)
        val changelogContent = if (!changelogFile.exists()) {
            versionRemoteSource.fetchChangelog(it.changelog).also {
                changelogFile.writeText(it)
            }
        } else changelogFile.readText()
        _updateInfoFlow.update {
            when (it) {
                is UpdateInfo.RemoteUpdate -> {
                    it.copy(changelog = changelogContent)
                }

                is UpdateInfo.LocalUpdate -> {
                    it.copy(changelog = changelogContent)
                }

                else -> it
            }
        }
    }

    private fun getLocalChangelogContent(versionName: String): String {
        val changelogFile = File(downloadDir, versionName + PREF_DOWNLOAD_CHANGELOG_EXTENSION)
        return if (!changelogFile.exists()) {
            ""
        } else changelogFile.readText()
    }

    private fun AndroidUpdateInfo.toUpdateInfo(): UpdateInfo {
        val resumableApkFile = versionLocalSource.getDownloadedApk()
        return when {
            versionCode <= BuildConfig.VERSION_CODE -> {
                UpdateInfo.NoUpdate
            }

            versionCode == resumableApkFile?.versionCode -> {
                if (resumableApkFile.completed) {
                    UpdateInfo.LocalUpdate(
                        versionCode,
                        versionName,
                        resumableApkFile.absolutePath.orEmpty(),
                        getLocalChangelogContent(versionName),
                        instantInstall = false
                    )
                } else {
                    if (fileFetchDeferred?.isActive != true) {
                        val oldValue = _updateInfoFlow.value
                        if (oldValue is UpdateInfo.RemoteUpdate && oldValue.downloadState is DownloadState.Error) {
                            oldValue
                        } else {
                            UpdateInfo.RemoteUpdate(
                                versionCode,
                                versionName,
                                link,
                                oldValue.changelog,
                                DownloadState.Pause(resumableApkFile.progress)
                            )
                        }
                    } else _updateInfoFlow.value
                }
            }

            else -> {
                UpdateInfo.RemoteUpdate(
                    versionCode,
                    versionName,
                    link
                )
            }
        }
    }

    fun fetchUpdateFile(updateInfo: UpdateInfo.RemoteUpdate) {
        fileFetchDeferred = fileFetchCoroutineScope.async {
            try {
                val file = File(downloadDir, updateInfo.versionName + PREF_DOWNLOAD_APK_EXTENSION)
                val resumableApkFile = versionLocalSource.getDownloadedApk()?.takeIf {
                    it.downloadedPath == file.absolutePath && !it.completed || it.downloadedPath != file.absolutePath
                }?.also { resumableApkFile ->
                    _updateInfoFlow.update {
                        updateInfo.copy(
                            url = it.url,
                            changelog = it.changelog,
                            downloadState = DownloadState.Downloading(resumableApkFile.progress)
                        )
                    }
                }
                val remoteFileInfo = versionRemoteSource.fetchUpdateFileInfo(updateInfo.url)
                val targetFile =
                    resumableApkFile?.takeIf { it.isMatchRemoteFile(remoteFileInfo) } ?: run {
                        versionLocalSource.setDownloadedApk(
                            updateInfo.versionCode,
                            updateInfo.versionName,
                            file,
                            remoteFileInfo.size,
                            remoteFileInfo.rangeIdentifier.orEmpty()
                        )
                        _updateInfoFlow.update {
                            updateInfo.copy(
                                downloadState = DownloadState.Start
                            )
                        }
                        file
                    }
                versionRemoteSource.fetchUpdateFile(updateInfo.url, targetFile).collectLatest {
                    _updateInfoFlow.value = if (it is DownloadState.End)
                        UpdateInfo.LocalUpdate(
                            versionCode = _updateInfoFlow.value.versionCode,
                            versionName = _updateInfoFlow.value.versionName,
                            url = targetFile.absolutePath,
                            changelog = _updateInfoFlow.value.changelog,
                            instantInstall = true
                        )
                    else
                        UpdateInfo.RemoteUpdate(
                            versionCode = _updateInfoFlow.value.versionCode,
                            versionName = _updateInfoFlow.value.versionName,
                            url = _updateInfoFlow.value.url,
                            changelog = _updateInfoFlow.value.changelog,
                            downloadState = it
                        )
                }
            } catch (e: Exception) {
                Xlog.e(TAG, "File fetch failed - ${updateInfo.url}", e)
                if (e !is CancellationException)
                    _updateInfoFlow.value = UpdateInfo.RemoteUpdate(
                        versionCode = _updateInfoFlow.value.versionCode,
                        versionName = _updateInfoFlow.value.versionName,
                        url = _updateInfoFlow.value.url,
                        changelog = _updateInfoFlow.value.changelog,
                        downloadState = DownloadState.Error(e)
                    )
            }
        }
        fileFetchCoroutineScope.launch {
            try {
                fileFetchDeferred?.await()
            } catch (e: CancellationException) {
                Xlog.d(TAG, "File fetch canceled - ${updateInfo.url}")
                _updateInfoFlow.value = UpdateInfo.RemoteUpdate(
                    versionCode = _updateInfoFlow.value.versionCode,
                    versionName = _updateInfoFlow.value.versionName,
                    url = _updateInfoFlow.value.url,
                    changelog = _updateInfoFlow.value.changelog,
                    downloadState = (updateInfoFlow.value as? UpdateInfo.RemoteUpdate)?.downloadState?.let {
                        DownloadState.Pause(it.progress)
                    } ?: DownloadState.Idle
                )
            }
        }
    }

    fun pauseDownload() {
        fileFetchDeferred?.takeIf { it.isActive && it.isCompleted.not() }
            ?.cancel(cause = CancellationException("Download paused"))
        fileFetchDeferred = null
    }

    companion object {
        private const val TAG = "VersionRepository"
    }
}