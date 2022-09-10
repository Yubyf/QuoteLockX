package com.crossbowffs.quotelock.app.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crossbowffs.quotelock.consts.Urls
import com.crossbowffs.quotelock.data.api.QuoteDataWithCollectState
import com.crossbowffs.quotelock.data.modules.QuoteRepository
import com.crossbowffs.quotelock.di.ResourceProvider
import com.crossbowffs.quotelock.utils.WorkUtils
import com.crossbowffs.quotelock.utils.XposedUtils
import com.crossbowffs.quotelock.utils.XposedUtils.startXposedActivity
import com.yubyf.quotelockx.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI event for the settings screen.
 */
sealed class MainUiEvent {
    data class SnackBarMessage(
        val message: String? = null,
        val duration: SnackbarDuration = SnackbarDuration.Short,
        val actionText: String? = null,
    ) : MainUiEvent()
}

data class MainUiState(val quoteData: QuoteDataWithCollectState, val refreshing: Boolean = false)

sealed class MainDialogUiState {
    object EnableModuleDialog : MainDialogUiState()

    object ModuleUpdatedDialog : MainDialogUiState()

    object None : MainDialogUiState()
}

/**
 * @author Yubyf
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val quoteRepository: QuoteRepository,
    private val resourceProvider: ResourceProvider,
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<MainUiEvent?>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _uiState =
        mutableStateOf(MainUiState(quoteRepository.getCurrentQuote()))
    val uiState: State<MainUiState> = _uiState

    private val _uiDialogState = mutableStateOf<MainDialogUiState>(MainDialogUiState.None)
    val uiDialogState: State<MainDialogUiState> = _uiDialogState

    init {
        // In case the user opens the app for the first time *after* rebooting,
        // we want to make sure the background work has been created.
        WorkUtils.createQuoteDownloadWork(context, false)
        if (!XposedUtils.isModuleEnabled) {
            _uiDialogState.value = MainDialogUiState.EnableModuleDialog
        } else if (XposedUtils.isModuleUpdated) {
            _uiDialogState.value = MainDialogUiState.ModuleUpdatedDialog
        }

        viewModelScope.launch {
            quoteRepository.quoteDataFlow.onEach {
                _uiState.value = _uiState.value.copy(quoteData = it)
            }.launchIn(this)
        }
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
        _uiEvent.emit(MainUiEvent.SnackBarMessage(
            message = resourceProvider.getString(
                if (quote == null) R.string.quote_download_failed else R.string.quote_download_success)
        ))
    }

    fun snackbarShown() = viewModelScope.launch {
        _uiEvent.emit(null)
    }

    fun Context.startXposedPage(section: String) {
        if (!startXposedActivity(section)) {
            viewModelScope.launch {
                _uiEvent.emit(MainUiEvent.SnackBarMessage(
                    message = resourceProvider.getString(R.string.xposed_not_installed)
                ))
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