package com.crossbowffs.quotelock.app.detail.jinrishici

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crossbowffs.quotelock.data.CardStyleRepository
import com.crossbowffs.quotelock.data.api.CardStyle
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.annotation.KoinViewModel

data class DetailJinrishiciUiState(
    val cardStyle: CardStyle,
)

@KoinViewModel
class DetailJinrishiciViewModel(
    cardStyleRepository: CardStyleRepository,
) : ViewModel() {

    private val _uiState =
        mutableStateOf(DetailJinrishiciUiState(CardStyle()))
    val uiState: State<DetailJinrishiciUiState> = _uiState

    init {
        cardStyleRepository.cardStyleFlow.onEach { cardStyle ->
            _uiState.value = _uiState.value.copy(cardStyle = cardStyle)
        }.launchIn(viewModelScope)
    }
}