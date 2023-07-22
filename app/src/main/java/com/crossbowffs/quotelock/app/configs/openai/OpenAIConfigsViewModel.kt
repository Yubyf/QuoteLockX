package com.crossbowffs.quotelock.app.configs.openai

import android.webkit.URLUtil
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crossbowffs.quotelock.app.SnackBarEvent
import com.crossbowffs.quotelock.app.emptySnackBarEvent
import com.crossbowffs.quotelock.data.AsyncResult
import com.crossbowffs.quotelock.data.api.AndroidString
import com.crossbowffs.quotelock.data.api.OpenAIConfigs
import com.crossbowffs.quotelock.data.modules.openai.OpenAIException
import com.crossbowffs.quotelock.data.modules.openai.OpenAIRepository
import com.crossbowffs.quotelock.data.modules.openai.OpenAIUsage
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.prefix
import com.yubyf.quotelockx.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the OpenAI setup screen.
 */
data class OpenAIUiState(
    val openAIConfigs: OpenAIConfigs = OpenAIConfigs(),
    val validateResult: AsyncResult<Unit>? = null,
    val openAIUsage: OpenAIUsage? = null,
)

@HiltViewModel
class OpenAIConfigsViewModel @Inject constructor(
    private val openAIRepository: OpenAIRepository,
) : ViewModel() {

    private val _uiState = mutableStateOf(
        OpenAIUiState(
            openAIConfigs = openAIRepository.openAIConfigs,
        )
    )
    val uiState: State<OpenAIUiState> = _uiState

    private val _uiEvent = MutableSharedFlow<SnackBarEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            openAIRepository.openAIConfigsFlow.onEach {
                _uiState.value = _uiState.value.copy(
                    openAIConfigs = it,
                )
            }.launchIn(this)
            openAIRepository.openAIUsageFlow.onEach {
                _uiState.value = _uiState.value.copy(
                    openAIUsage = it,
                )
            }.launchIn(this)
        }
    }

    fun selectLanguage(language: String) {
        openAIRepository.language = language
    }

    fun selectModel(model: String) {
        openAIRepository.model = model
    }

    fun selectQuoteType(quoteType: Int) {
        openAIRepository.quoteType = quoteType
    }

    fun setApiKey(apiKey: String) {
        if (openAIRepository.apiKey != apiKey) {
            openAIRepository.apiKey = apiKey
            _uiState.value = _uiState.value.copy(
                openAIUsage = null,
            )
            viewModelScope.launch {
                runCatching { openAIRepository.fetchAccountInfo() }
            }
        }
    }

    fun setApiHost(host: String?) {
        openAIRepository.apiHost =
            host?.takeIf { it.isNotBlank() }
                ?.let { if (!URLUtil.isNetworkUrl(it)) it.prefix("https://") else it }
    }

    fun validate() = viewModelScope.launch {
        runCatching {
            _uiState.value = _uiState.value.copy(
                validateResult = AsyncResult.Loading(AndroidString.StringText("")),
            )
            openAIRepository.fetchAccountInfo()
        }.onFailure {
            Xlog.e(TAG, "Failed to validate API", it)
            when (it) {
                is OpenAIException.RegionNotSupportedException -> {
                    _uiEvent.emit(SnackBarEvent(message = AndroidString.StringRes(R.string.module_openai_not_support_region)))
                }

                is OpenAIException.ApiKeyNotSetException -> {
                    _uiEvent.emit(SnackBarEvent(message = AndroidString.StringRes(R.string.module_openai_api_key_not_set)))
                }

                is OpenAIException.ApiKeyInvalidException -> {
                    _uiEvent.emit(SnackBarEvent(message = AndroidString.StringRes(R.string.module_openai_api_key_invalid)))
                }

                else -> {
                    _uiEvent.emit(SnackBarEvent(message = AndroidString.StringRes(R.string.module_openai_api_connect_error)))
                }
            }
            _uiState.value = _uiState.value.copy(
                validateResult = AsyncResult.Error.Message(AndroidString.StringText(""))
            )
        }.onSuccess {
            _uiState.value = _uiState.value.copy(
                validateResult = AsyncResult.Success(Unit),
            )
        }
    }

    fun snackBarShown() = viewModelScope.launch {
        _uiEvent.emit(emptySnackBarEvent)
    }

    companion object {
        private const val TAG = "OpenAIConfigsViewModel"
    }
}