package com.crossbowffs.quotelock.app.quote

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crossbowffs.quotelock.data.CardStyleRepository
import com.crossbowffs.quotelock.data.ShareRepository
import com.crossbowffs.quotelock.data.api.CardStyle
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.data.api.QuoteDataWithCollectState
import com.crossbowffs.quotelock.data.modules.collections.QuoteCollectionRepository
import com.crossbowffs.quotelock.ui.components.Snapshotables
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

/**
 * UI state for the quote screen.
 */
data class QuoteUiState(
    val cardStyle: CardStyle,
    val collectState: Boolean? = null,
)

/**
 * @author Yubyf
 */
@KoinViewModel
class QuoteViewModel(
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
        mutableStateOf(QuoteUiState(CardStyle()))
    val uiState: State<QuoteUiState> = _uiState

    init {
        cardStyleRepository.cardStyleFlow.onEach { cardStyle ->
            _uiState.value = _uiState.value.copy(cardStyle = cardStyle)
        }.launchIn(viewModelScope)
        collectionRepository.getAllStream().onEach { collections ->
            val quoteData = quoteData.copy()
            val currentQuoteCollected = collections.find { quoteData.uid == it.uid } != null
            _uiState.value = _uiState.value.copy(collectState = currentQuoteCollected)
        }.launchIn(viewModelScope)
    }

    fun queryQuoteCollectState() {
        viewModelScope.launch {
            val state = collectionRepository.getByUid(quoteData.uid) != null
            _uiState.value = _uiState.value.copy(collectState = state)
        }
    }

    fun switchCollectionState(quoteData: QuoteDataWithCollectState) {
        viewModelScope.launch {
            val currentState = collectionRepository.getByUid(quoteData.uid) != null
            if (currentState) {
                collectionRepository.delete(quoteData.uid)
            } else {
                collectionRepository.insert(quoteData.quote)
            }
        }
    }

    fun setSnapshotables(snapshotables: Snapshotables) {
        shareRepository.currentSnapshotables = snapshotables
    }
}