package com.crossbowffs.quotelock.app.lockscreen.styles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crossbowffs.quotelock.data.ConfigurationRepository
import com.crossbowffs.quotelock.data.api.QuoteDataWithCollectState
import com.crossbowffs.quotelock.data.api.QuoteStyle
import com.crossbowffs.quotelock.data.modules.QuoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.annotation.KoinViewModel

/**
 * UI state for the preview screen in the settings page.
 */
data class PreviewUiState(val quoteData: QuoteDataWithCollectState, val quoteStyle: QuoteStyle)

/**
 * @author Yubyf
 */
@KoinViewModel
class PreviewViewModel(
    configurationRepository: ConfigurationRepository,
    quoteRepository: QuoteRepository,
) : ViewModel() {

    private val _uiState: MutableStateFlow<PreviewUiState> =
        MutableStateFlow(PreviewUiState(quoteRepository.getCurrentQuote(), QuoteStyle()))
    val uiState = _uiState.asStateFlow()

    init {
        quoteRepository.quoteDataFlow.onEach {
            _uiState.value = _uiState.value.copy(quoteData = it)
        }.launchIn(viewModelScope)
        configurationRepository.quoteStyleFlow.onEach {
            _uiState.value = _uiState.value.copy(quoteStyle = it)
        }.launchIn(viewModelScope)
    }
}