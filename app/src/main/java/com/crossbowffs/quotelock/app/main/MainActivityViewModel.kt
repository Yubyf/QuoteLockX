package com.crossbowffs.quotelock.app.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crossbowffs.quotelock.data.ConfigurationRepository
import com.crossbowffs.quotelock.data.WidgetRepository
import com.crossbowffs.quotelock.data.version.UpdateInfo
import com.crossbowffs.quotelock.data.version.VersionRepository
import com.crossbowffs.quotelock.utils.WorkUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

/**
 * @author Yubyf
 */
@KoinViewModel
class MainActivityViewModel(
    context: Context,
    configurationRepository: ConfigurationRepository,
    widgetRepository: WidgetRepository,
    versionRepository: VersionRepository,
) : ViewModel() {

    private val _uiInstallEvent = MutableSharedFlow<String?>()
    val uiInstallEvent = _uiInstallEvent.asSharedFlow()

    init {
        // In case the user opens the app for the first time *after* rebooting,
        // we want to make sure the background work has been created.
        with(context) {
            WorkUtils.createQuoteDownloadWork(
                this,
                configurationRepository.refreshInterval,
                configurationRepository.isRequireInternet,
                configurationRepository.isUnmeteredNetworkOnly,
                false
            )
            WorkUtils.createVersionCheckWork(this)
        }
        // Trigger the initialization of the widget repository
        widgetRepository.placeholder()

        versionRepository.updateInfoFlow.onEach {
            if (it is UpdateInfo.LocalUpdate && it.instantInstall) {
                _uiInstallEvent.emit(it.url)
            }
        }.launchIn(viewModelScope)
    }

    fun installEventConsumed() = viewModelScope.launch {
        _uiInstallEvent.emit(null)
    }
}