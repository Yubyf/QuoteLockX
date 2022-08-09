package com.crossbowffs.quotelock.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crossbowffs.quotelock.data.modules.QuoteRepository
import com.crossbowffs.quotelock.di.ResourceProvider
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.yubyf.quotelockx.R
import dagger.hilt.android.lifecycle.HiltViewModel
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
        @BaseTransientBottomBar.Duration val duration: Int = Snackbar.LENGTH_SHORT,
        val actionText: String? = null,
    ) : MainUiEvent()

    data class ProgressMessage(
        val show: Boolean = false,
        val message: String? = null,
    ) : MainUiEvent()
}

/**
 * @author Yubyf
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val quoteRepository: QuoteRepository,
    private val resourceProvider: ResourceProvider,
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<MainUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun refreshQuote() = viewModelScope.launch {
        _uiEvent.emit(MainUiEvent.ProgressMessage(true,
            resourceProvider.getString(R.string.downloading_quote)))
        val quote = try {
            quoteRepository.downloadQuote()
        } catch (e: CancellationException) {
            null
        }
        _uiEvent.emit(MainUiEvent.ProgressMessage(false))
        _uiEvent.emit(MainUiEvent.SnackBarMessage(
            message = resourceProvider.getString(
                if (quote == null) R.string.quote_download_failed else R.string.quote_download_success)
        ))
    }
}