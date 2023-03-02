package com.crossbowffs.quotelock.app.detail.jinrishici

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crossbowffs.quotelock.data.CardStyleRepository
import com.crossbowffs.quotelock.data.api.CardStyle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class DetailJinrishiciUiState(
    val cardStyle: CardStyle,
)

@HiltViewModel
class DetailJinrishiciViewModel @Inject constructor(
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