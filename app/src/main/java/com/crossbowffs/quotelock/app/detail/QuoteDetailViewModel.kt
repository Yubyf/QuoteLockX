package com.crossbowffs.quotelock.app.detail

import android.graphics.Typeface
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crossbowffs.quotelock.app.font.FontManager
import com.crossbowffs.quotelock.consts.PREF_COMMON_FONT_FAMILY
import com.crossbowffs.quotelock.consts.PREF_COMMON_FONT_FAMILY_DEFAULT
import com.crossbowffs.quotelock.data.ConfigurationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the settings screen.
 */
data class QuoteDetailUiState(
    val quoteTypeface: Typeface? = null,
    val sourceTypeface: Typeface? = null,
)

/**
 * @author Yubyf
 */
@HiltViewModel
class QuoteDetailViewModel @Inject constructor(
    private val configurationRepository: ConfigurationRepository,
) : ViewModel() {

    private val _uiState: MutableStateFlow<QuoteDetailUiState> =
        MutableStateFlow(QuoteDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        _uiState.update { currentState ->
            val style = configurationRepository.quoteStyle
            currentState.copy(quoteTypeface = style.quoteTypeface,
                sourceTypeface = style.sourceTypeface)
        }
        viewModelScope.launch {
            configurationRepository.observeConfigurationDataStore { preferences, key ->
                if (key?.name == PREF_COMMON_FONT_FAMILY) {
                    val font =
                        preferences[stringPreferencesKey(PREF_COMMON_FONT_FAMILY)]
                            ?: PREF_COMMON_FONT_FAMILY_DEFAULT
                    val typeface = if (PREF_COMMON_FONT_FAMILY_DEFAULT != font) {
                        runCatching { FontManager.loadTypeface(font) }.getOrNull()
                    } else {
                        null
                    }
                    _uiState.update { currentState ->
                        currentState.copy(quoteTypeface = typeface,
                            sourceTypeface = typeface)
                    }
                }
            }
        }
    }
}