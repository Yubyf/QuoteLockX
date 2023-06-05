package com.crossbowffs.quotelock.app.history

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crossbowffs.quotelock.app.SnackBarEvent
import com.crossbowffs.quotelock.app.emptySnackBarEvent
import com.crossbowffs.quotelock.data.api.AndroidString
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
 * UI state for the quote history list screen.
 */
data class QuoteHistoryListUiState(
    val allItems: List<QuoteHistoryEntity>,
    val searchKeyword: String,
    val searchedItems: List<QuoteHistoryEntity>,
    val showClearAndSearchMenu: Boolean,
)

/**
 * @author Yubyf
 */
@HiltViewModel
class QuoteHistoryViewModel @Inject constructor(
    private val historyRepository: QuoteHistoryRepository,
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<SnackBarEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _uiListState =
        mutableStateOf(QuoteHistoryListUiState(emptyList(), "", emptyList(), false))
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
                    _uiListState.value = _uiListState.value.copy(
                        allItems = it, showClearAndSearchMenu = it.isNotEmpty()
                    )
                }
            }
        }
    }

    fun prepareSearch() {
        _uiListState.value =
            _uiListState.value.copy(searchKeyword = "", searchedItems = emptyList())
    }

    fun search(keyword: String) {
        if (keyword.trim() == _uiListState.value.searchKeyword.trim()) {
            if (keyword != _uiListState.value.searchKeyword) {
                _uiListState.value = _uiListState.value.copy(searchKeyword = keyword)
            }
            return
        }
        if (keyword.isBlank()) {
            _uiListState.value =
                _uiListState.value.copy(searchKeyword = "", searchedItems = emptyList())
            return
        }
        _uiListState.value = _uiListState.value.copy(searchKeyword = keyword)
        viewModelScope.launch {
            historyRepository.search(keyword.trim()).collect {
                _uiListState.value = _uiListState.value.copy(searchedItems = it)
            }
        }
    }

    fun clear() {
        viewModelScope.launch {
            historyRepository.deleteAll()
            _uiEvent.emit(
                SnackBarEvent(AndroidString.StringRes(R.string.quote_histories_cleared_quote))
            )
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            historyRepository.delete(id)
            _uiEvent.emit(
                SnackBarEvent(AndroidString.StringRes(R.string.module_custom_deleted_quote))
            )
        }
    }

    fun snackBarShown() = viewModelScope.launch {
        _uiEvent.emit(emptySnackBarEvent)
    }
}