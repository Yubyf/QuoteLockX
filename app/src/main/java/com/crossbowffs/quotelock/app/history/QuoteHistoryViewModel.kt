package com.crossbowffs.quotelock.app.history

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crossbowffs.quotelock.data.history.QuoteHistoryEntity
import com.crossbowffs.quotelock.data.history.QuoteHistoryRepository
import com.yubyf.quotelockx.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI event for the quote history list screen.
 */
data class QuoteHistoryUiEvent(val message: Int?)

/**
 * UI state for the quote history list screen.
 */
data class QuoteHistoryListUiState(val items: List<QuoteHistoryEntity>, val showClearMenu: Boolean)

/**
 * @author Yubyf
 */
@HiltViewModel
class QuoteHistoryViewModel @Inject constructor(
    private val historyRepository: QuoteHistoryRepository,
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<QuoteHistoryUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _uiListState = mutableStateOf(QuoteHistoryListUiState(emptyList(), false))
    val uiListState: State<QuoteHistoryListUiState>
        get() = _uiListState

    init {
        viewModelScope.run {
            launch {
                historyRepository.getAll().stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList()
                ).collect {
                    _uiListState.value =
                        QuoteHistoryListUiState(items = it, showClearMenu = it.isNotEmpty())
                }
            }
        }
    }


    fun clear() {
        viewModelScope.launch {
            historyRepository.deleteAll()
            _uiEvent.emit(QuoteHistoryUiEvent(R.string.quote_histories_cleared_quote))
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            historyRepository.delete(id)
            _uiEvent.emit(QuoteHistoryUiEvent(R.string.module_custom_deleted_quote))
        }
    }
}