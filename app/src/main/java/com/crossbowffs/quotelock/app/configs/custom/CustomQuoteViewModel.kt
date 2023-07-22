package com.crossbowffs.quotelock.app.configs.custom

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crossbowffs.quotelock.app.SnackBarEvent
import com.crossbowffs.quotelock.data.api.AndroidString
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.data.modules.custom.CustomQuoteRepository
import com.crossbowffs.quotelock.data.modules.custom.database.CustomQuoteEntity
import com.yubyf.quotelockx.R
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.annotation.KoinViewModel

/**
 * UI state for the custom quote list screen.
 */
data class CustomQuoteListUiState(val items: List<CustomQuoteEntity>)

/**
 * @author Yubyf
 */
@KoinViewModel
class CustomQuoteViewModel(
    private val customQuoteRepository: CustomQuoteRepository,
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<SnackBarEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _uiListState = mutableStateOf(CustomQuoteListUiState(emptyList()))
    val uiListState = _uiListState

    init {
        viewModelScope.run {
            launch {
                customQuoteRepository.getAll().stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList()
                ).collect {
                    _uiListState.value = _uiListState.value.copy(items = it)
                }
            }
        }
    }

    fun queryQuote(rowId: Long): QuoteData {
        return if (rowId < 0) {
            QuoteData()
        } else runBlocking {
            customQuoteRepository.getById(rowId)?.let {
                QuoteData(it.text, it.source)
            } ?: QuoteData()
        }
    }

    fun persistQuote(rowId: Long, text: String, source: String) {
        viewModelScope.launch {
            if (rowId >= 0) {
                customQuoteRepository
                    .update(CustomQuoteEntity(rowId.toInt(), text, source))
            } else {
                customQuoteRepository
                    .insert(CustomQuoteEntity(text = text, source = source))
            }
            _uiEvent.emit(
                SnackBarEvent(
                    AndroidString.StringRes(R.string.module_custom_saved_quote)
                )
            )
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            customQuoteRepository.delete(id)
            _uiEvent.emit(
                SnackBarEvent(
                    AndroidString.StringRes(R.string.module_custom_deleted_quote)
                )
            )
        }
    }

    fun snackBarShown() = viewModelScope.launch {
        _uiEvent.emit(SnackBarEvent())
    }
}