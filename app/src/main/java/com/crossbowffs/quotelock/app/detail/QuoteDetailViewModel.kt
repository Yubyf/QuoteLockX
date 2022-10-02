package com.crossbowffs.quotelock.app.detail

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crossbowffs.quotelock.data.CardStyleRepository
import com.crossbowffs.quotelock.data.ShareRepository
import com.crossbowffs.quotelock.data.api.*
import com.crossbowffs.quotelock.data.modules.collections.QuoteCollectionRepository
import com.crossbowffs.quotelock.ui.components.Snapshotables
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the quote detail screen.
 */
data class QuoteDetailUiState(
    val cardStyle: CardStyle,
    val collectState: Boolean? = null,
)

/**
 * @author Yubyf
 */
@HiltViewModel
class QuoteDetailViewModel @Inject constructor(
    cardStyleRepository: CardStyleRepository,
    private val collectionRepository: QuoteCollectionRepository,
    private val shareRepository: ShareRepository,
) : ViewModel() {

    var quoteData: QuoteData = QuoteData()
        set(value) {
            if (field == value) {
                return
            }
            field = value
            _uiState.value = _uiState.value.copy(collectState = null)
        }

    private val _uiState =
        mutableStateOf(QuoteDetailUiState(CardStyle()))
    val uiState: State<QuoteDetailUiState> = _uiState

    init {
        cardStyleRepository.cardStyleFlow.onEach { cardStyle ->
            _uiState.value = _uiState.value.copy(cardStyle = cardStyle)
        }.launchIn(viewModelScope)
        collectionRepository.getAllStream().onEach { collections ->
            val quoteData = quoteData.copy()
            val currentQuoteCollected = collections.find { quoteData.md5 == it.md5 } != null
            _uiState.value = _uiState.value.copy(collectState = currentQuoteCollected)
        }.launchIn(viewModelScope)
    }

    fun queryQuoteCollectState() {
        viewModelScope.launch {
            val state = collectionRepository.getByQuote(quoteData.quoteText,
                quoteData.quoteSource,
                quoteData.quoteAuthor) != null
            _uiState.value = _uiState.value.copy(collectState = state)
        }
    }

    fun switchCollectionState(quoteData: QuoteDataWithCollectState) {
        viewModelScope.launch {
            val currentState = collectionRepository.getByQuote(quoteData.quoteText,
                quoteData.quoteSource,
                quoteData.quoteAuthor) != null
            if (currentState) {
                collectionRepository.delete(quoteData.md5)
            } else {
                collectionRepository.insert(quoteData.toQuoteData())
            }
        }
    }

    fun setSnapshotables(snapshotables: Snapshotables) {
        shareRepository.currentSnapshotables = snapshotables
    }
}