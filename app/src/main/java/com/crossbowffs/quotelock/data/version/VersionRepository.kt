package com.crossbowffs.quotelock.data.version

import com.crossbowffs.quotelock.di.IoDispatcher
import com.crossbowffs.quotelock.utils.DownloadState
import com.crossbowffs.quotelock.utils.DownloadState.Start.progress
import com.crossbowffs.quotelock.utils.Xlog
import com.yubyf.quotelockx.BuildConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

sealed class UpdateInfo(
    open val versionCode: Int = -1,
    open val versionName: String = "",
    open val url: String = "",
) {
    object NoUpdate : UpdateInfo()
    data class LocalUpdate(
        override val versionCode: Int,
        override val versionName: String,
        override val url: String,
        val instantInstall: Boolean = false,
    ) : UpdateInfo(versionCode, versionName, url)

    data class RemoteUpdate(
        override val versionCode: Int,
        override val versionName: String,
        override val url: String,
        val downloadState: DownloadState = DownloadState.Idle,
    ) : UpdateInfo(versionCode, versionName, url)

    val hasUpdate: Boolean
        get() = this !is NoUpdate
}

@Singleton
class VersionRepository @Inject internal constructor(
    private val versionRemoteSource: VersionRemoteSource,
    private val versionLocalSource: VersionLocalSource,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) {

    private val _updateInfoFlow = MutableStateFlow<UpdateInfo>(UpdateInfo.NoUpdate)
    val updateInfoFlow = _updateInfoFlow.asStateFlow()

    private val fileFetchCoroutineScope = CoroutineScope(dispatcher)
    private var fileFetchDeferred: Deferred<*>? = null

    suspend fun fetchUpdate() = runCatching {
        versionRemoteSource.fetchUpdate().androidUpdateInfo
    }.onFailure { exception ->
        Xlog.e("TAG", "fetch update failed", exception)
        _updateInfoFlow.value = versionLocalSource.getDownloadedApk()?.takeIf {
            it.versionCode > BuildConfig.VERSION_CODE && it.completed
        }?.let {
            UpdateInfo.LocalUpdate(
                it.versionCode,
                it.versionName,
                it.absolutePath,
                instantInstall = false
            )
        } ?: UpdateInfo.NoUpdate
        throw exception
    }.onSuccess {
        Xlog.d("TAG", "fetch update success")
        val updateInfo = with(it) {
            val resumableApkFile = versionLocalSource.getDownloadedApk()
            when {
                versionCode <= BuildConfig.VERSION_CODE -> {
                    UpdateInfo.NoUpdate
                }

                versionCode == resumableApkFile?.versionCode -> {
                    if (resumableApkFile.completed) {
                        UpdateInfo.LocalUpdate(
                            versionCode,
                            versionName,
                            resumableApkFile.absolutePath.orEmpty(),
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
        _updateInfoFlow.value = updateInfo
    }

    fun fetchUpdateFile(updateInfo: UpdateInfo.RemoteUpdate, file: File) {
        fileFetchDeferred = fileFetchCoroutineScope.async {
            try {
                val resumableApkFile = versionLocalSource.getDownloadedApk()?.takeIf {
                    it.downloadedPath == file.path && !it.completed || it.downloadedPath != file.path
                }?.also {
                    _updateInfoFlow.value = UpdateInfo.RemoteUpdate(
                        versionCode = _updateInfoFlow.value.versionCode,
                        versionName = _updateInfoFlow.value.versionName,
                        url = _updateInfoFlow.value.url,
                        downloadState = DownloadState.Downloading(it.progress)
                    )
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
                        _updateInfoFlow.value = UpdateInfo.RemoteUpdate(
                            versionCode = _updateInfoFlow.value.versionCode,
                            versionName = _updateInfoFlow.value.versionName,
                            url = _updateInfoFlow.value.url,
                            downloadState = DownloadState.Start
                        )
                        file
                    }
                versionRemoteSource.fetchUpdateFile(updateInfo.url, targetFile).collectLatest {
                    _updateInfoFlow.value = if (it is DownloadState.End)
                        UpdateInfo.LocalUpdate(
                            versionCode = _updateInfoFlow.value.versionCode,
                            versionName = _updateInfoFlow.value.versionName,
                            url = targetFile.absolutePath,
                            instantInstall = true
                        )
                    else
                        UpdateInfo.RemoteUpdate(
                            versionCode = _updateInfoFlow.value.versionCode,
                            versionName = _updateInfoFlow.value.versionName,
                            url = _updateInfoFlow.value.url,
                            downloadState = it
                        )
                }
            } catch (e: Exception) {
                Xlog.e("TAG", "File fetch failed - ${file.path}", e)
                if (e !is CancellationException)
                    _updateInfoFlow.value = UpdateInfo.RemoteUpdate(
                        versionCode = _updateInfoFlow.value.versionCode,
                        versionName = _updateInfoFlow.value.versionName,
                        url = _updateInfoFlow.value.url,
                        downloadState = DownloadState.Error(e)
                    )
            }
        }
        fileFetchCoroutineScope.launch {
            try {
                fileFetchDeferred?.await()
            } catch (e: CancellationException) {
                Xlog.d("TAG", "File fetch canceled - ${file.path}")
                _updateInfoFlow.value = UpdateInfo.RemoteUpdate(
                    versionCode = _updateInfoFlow.value.versionCode,
                    versionName = _updateInfoFlow.value.versionName,
                    url = _updateInfoFlow.value.url,
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
}