package com.crossbowffs.quotelock.app.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crossbowffs.quotelock.app.SnackBarEvent
import com.crossbowffs.quotelock.app.emptySnackBarEvent
import com.crossbowffs.quotelock.consts.Urls
import com.crossbowffs.quotelock.data.WidgetRepository
import com.crossbowffs.quotelock.data.api.AndroidString
import com.crossbowffs.quotelock.data.api.QuoteDataWithCollectState
import com.crossbowffs.quotelock.data.modules.QuoteRepository
import com.crossbowffs.quotelock.data.version.VersionRepository
import com.crossbowffs.quotelock.utils.XposedUtils
import com.crossbowffs.quotelock.utils.XposedUtils.startXposedActivity
import com.yubyf.quotelockx.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val quoteData: QuoteDataWithCollectState,
    val refreshing: Boolean = false,
    val showUpdate: Boolean = false,
)

sealed class MainDialogUiState {
    object EnableModuleDialog : MainDialogUiState()

    object ModuleUpdatedDialog : MainDialogUiState()

    object None : MainDialogUiState()
}

/**
 * @author Yubyf
 */
@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val quoteRepository: QuoteRepository,
    widgetRepository: WidgetRepository,
    versionRepository: VersionRepository,
) : ViewModel() {

    private val _uiMessageEvent = MutableSharedFlow<SnackBarEvent>()
    val uiMessageEvent = _uiMessageEvent.asSharedFlow()

    private val _uiState =
        mutableStateOf(MainUiState(quoteRepository.getCurrentQuote()))
    val uiState: State<MainUiState> = _uiState

    private val _uiDialogState = mutableStateOf<MainDialogUiState>(MainDialogUiState.None)
    val uiDialogState: State<MainDialogUiState> = _uiDialogState

    private val _uiInstallEvent = MutableSharedFlow<String?>()
    val uiInstallEvent = _uiInstallEvent.asSharedFlow()

    init {
        if (!XposedUtils.isModuleEnabled) {
            _uiDialogState.value = MainDialogUiState.EnableModuleDialog
        } else if (XposedUtils.isModuleUpdated) {
            _uiDialogState.value = MainDialogUiState.ModuleUpdatedDialog
        }
        // Trigger the initialization of the widget repository
        widgetRepository.placeholder()

        quoteRepository.quoteDataFlow.onEach {
            _uiState.value = _uiState.value.copy(quoteData = it)
        }.launchIn(viewModelScope)
        versionRepository.updateInfoFlow.onEach {
            _uiState.value = _uiState.value.copy(showUpdate = it.hasUpdate)
        }.launchIn(viewModelScope)
        refreshQuote()
    }

    fun refreshQuote() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(refreshing = true)
        val quote = try {
            quoteRepository.downloadQuote()
        } catch (e: CancellationException) {
            null
        }
        _uiState.value = _uiState.value.copy(refreshing = false)
        _uiMessageEvent.emit(
            SnackBarEvent(
                message = AndroidString.StringRes(
                    if (quote == null) R.string.quote_download_failed else R.string.quote_download_success
                )
            )
        )
    }

    fun snackBarShown() = viewModelScope.launch {
        _uiMessageEvent.emit(emptySnackBarEvent)
    }

    fun installEventConsumed() = viewModelScope.launch {
        _uiInstallEvent.emit(null)
    }

    fun Context.startXposedPage(section: String) {
        if (!startXposedActivity(section)) {
            viewModelScope.launch {
                _uiMessageEvent.emit(
                    SnackBarEvent(
                        message = AndroidString.StringRes(R.string.xposed_not_installed)
                    )
                )
            }
            startBrowserActivity(Urls.XPOSED_FORUM)
        }
    }

    fun Context.startBrowserActivity(url: String) =
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))

    fun cancelDialog() {
        _uiDialogState.value = MainDialogUiState.None
    }
}