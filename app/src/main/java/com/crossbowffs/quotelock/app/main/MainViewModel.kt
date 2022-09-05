package com.crossbowffs.quotelock.app.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crossbowffs.quotelock.consts.PREF_QUOTES_AUTHOR
import com.crossbowffs.quotelock.consts.PREF_QUOTES_SOURCE
import com.crossbowffs.quotelock.consts.PREF_QUOTES_TEXT
import com.crossbowffs.quotelock.consts.Urls
import com.crossbowffs.quotelock.data.api.QuoteViewData
import com.crossbowffs.quotelock.data.api.buildQuoteViewData
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

data class MainUiState(val quoteViewData: QuoteViewData, val refreshing: Boolean = false)

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

    private val _uiState = mutableStateOf(MainUiState(getQuoteViewData()))
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

        viewModelScope.apply {
            launch {
                quoteRepository.observeQuoteData { preferences, key ->
                    when (key?.name) {
                        PREF_QUOTES_TEXT,
                        PREF_QUOTES_AUTHOR,
                        PREF_QUOTES_SOURCE,
                        -> {
                            val quote =
                                preferences[stringPreferencesKey(PREF_QUOTES_TEXT)] ?: ""
                            val source = preferences[stringPreferencesKey(PREF_QUOTES_SOURCE)]
                            val author = preferences[stringPreferencesKey(PREF_QUOTES_AUTHOR)]
                            _uiState.value = _uiState.value.copy(
                                quoteViewData = resourceProvider.buildQuoteViewData(
                                    quote,
                                    source,
                                    author)
                            )
                        }
                    }
                }
            }
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

    private fun getQuoteViewData(): QuoteViewData {
        val quoteData = quoteRepository.getCurrentQuote()
        return resourceProvider.buildQuoteViewData(
            quoteData.quoteText,
            quoteData.quoteSource,
            quoteData.quoteAuthor)
    }

    fun cancelDialog() {
        _uiDialogState.value = MainDialogUiState.None
    }
}