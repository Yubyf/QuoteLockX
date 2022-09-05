package com.crossbowffs.quotelock.app.lockscreen.styles

import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crossbowffs.quotelock.app.font.FontManager
import com.crossbowffs.quotelock.consts.*
import com.crossbowffs.quotelock.data.ConfigurationRepository
import com.crossbowffs.quotelock.data.api.QuoteStyle
import com.crossbowffs.quotelock.data.api.QuoteViewData
import com.crossbowffs.quotelock.data.api.buildQuoteViewData
import com.crossbowffs.quotelock.data.modules.QuoteRepository
import com.crossbowffs.quotelock.di.ResourceProvider
import com.crossbowffs.quotelock.utils.getComposeFontStyle
import com.crossbowffs.quotelock.utils.getComposeFontWeight
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the preview screen in the settings page.
 */
data class PreviewUiState(val quoteViewData: QuoteViewData, val quoteStyle: QuoteStyle)

/**
 * @author Yubyf
 */
@HiltViewModel
class PreviewViewModel @Inject constructor(
    private val configurationRepository: ConfigurationRepository,
    private val quoteRepository: QuoteRepository,
    private val resourceProvider: ResourceProvider,
) : ViewModel() {

    private val _uiState: MutableStateFlow<PreviewUiState> =
        MutableStateFlow(PreviewUiState(getQuoteViewData(),
            configurationRepository.quoteStyle))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.apply {
            launch {
                quoteRepository.observeQuoteData { preferences, key ->
                    when (key?.name) {
                        PREF_QUOTES_TEXT,
                        PREF_QUOTES_AUTHOR,
                        PREF_QUOTES_SOURCE,
                        -> {
                            val quote =
                                preferences[stringPreferencesKey(PREF_QUOTES_TEXT)] ?: ""
                            val source = preferences[stringPreferencesKey(PREF_QUOTES_SOURCE)]
                            val author = preferences[stringPreferencesKey(PREF_QUOTES_AUTHOR)]
                            _uiState.update { currentState ->
                                currentState.copy(
                                    quoteViewData = resourceProvider.buildQuoteViewData(
                                        quote,
                                        source,
                                        author)
                                )
                            }
                        }
                    }
                }
            }

            launch {
                configurationRepository.observeConfigurationDataStore { preferences, key ->
                    when (key?.name) {
                        PREF_COMMON_FONT_SIZE_TEXT -> _uiState.update { currentState ->
                            currentState.copy(
                                quoteStyle = currentState.quoteStyle.copy(
                                    quoteSize = (preferences[stringPreferencesKey(
                                        PREF_COMMON_FONT_SIZE_TEXT)]
                                        ?: PREF_COMMON_FONT_SIZE_TEXT_DEFAULT).toInt())
                            )
                        }
                        PREF_COMMON_FONT_SIZE_SOURCE -> _uiState.update { currentState ->
                            currentState.copy(
                                quoteStyle = currentState.quoteStyle.copy(
                                    sourceSize = (preferences[stringPreferencesKey(
                                        PREF_COMMON_FONT_SIZE_SOURCE)]
                                        ?: PREF_COMMON_FONT_SIZE_SOURCE_DEFAULT).toInt())
                            )
                        }
                        PREF_COMMON_FONT_FAMILY -> {
                            val font =
                                preferences[stringPreferencesKey(PREF_COMMON_FONT_FAMILY)]
                                    ?: PREF_COMMON_FONT_FAMILY_DEFAULT
                            val typeface = if (PREF_COMMON_FONT_FAMILY_DEFAULT != font) {
                                runCatching { FontManager.loadTypeface(font) }.getOrNull()
                            } else {
                                null
                            }
                            _uiState.update { currentState ->
                                currentState.copy(
                                    quoteStyle = currentState.quoteStyle.copy(
                                        quoteTypeface = typeface,
                                        sourceTypeface = typeface
                                    )
                                )
                            }
                        }
                        PREF_COMMON_FONT_STYLE_TEXT -> {
                            val quoteStyles =
                                preferences[stringSetPreferencesKey(PREF_COMMON_FONT_STYLE_TEXT)]
                            _uiState.update { currentState ->
                                currentState.copy(
                                    quoteStyle = currentState.quoteStyle.copy(
                                        quoteFontWeight = getComposeFontWeight(quoteStyles),
                                        quoteFontStyle = getComposeFontStyle(quoteStyles),
                                    )
                                )
                            }
                        }
                        PREF_COMMON_FONT_STYLE_SOURCE -> {
                            val sourceStyles =
                                preferences[stringSetPreferencesKey(
                                    PREF_COMMON_FONT_STYLE_SOURCE)]
                            _uiState.update { currentState ->
                                currentState.copy(
                                    quoteStyle = currentState.quoteStyle.copy(
                                        sourceFontWeight = getComposeFontWeight(sourceStyles),
                                        sourceFontStyle = getComposeFontStyle(sourceStyles),
                                    )
                                )
                            }
                        }
                        PREF_COMMON_QUOTE_SPACING -> _uiState.update { currentState ->
                            currentState.copy(
                                quoteStyle = currentState.quoteStyle.copy(
                                    quoteSpacing = (preferences[stringPreferencesKey(
                                        PREF_COMMON_QUOTE_SPACING)]
                                        ?: PREF_COMMON_QUOTE_SPACING_DEFAULT).toInt()
                                )
                            )
                        }
                        PREF_COMMON_PADDING_TOP -> _uiState.update { currentState ->
                            currentState.copy(
                                quoteStyle = currentState.quoteStyle.copy(
                                    paddingTop = (preferences[stringPreferencesKey(
                                        PREF_COMMON_PADDING_TOP)]
                                        ?: PREF_COMMON_PADDING_TOP_DEFAULT).toInt()
                                )
                            )
                        }
                        PREF_COMMON_PADDING_BOTTOM -> _uiState.update { currentState ->
                            currentState.copy(
                                quoteStyle = currentState.quoteStyle.copy(
                                    paddingBottom = (preferences[stringPreferencesKey(
                                        PREF_COMMON_PADDING_BOTTOM)]
                                        ?: PREF_COMMON_PADDING_BOTTOM_DEFAULT).toInt()
                                )
                            )
                        }
                        else -> {}
                    }
                }
            }
            _uiState.update { currentState ->
                currentState.copy(quoteStyle = configurationRepository.quoteStyle)
            }
        }
    }

    private fun getQuoteViewData(): QuoteViewData {
        val quoteData = quoteRepository.getCurrentQuote()
        return resourceProvider.buildQuoteViewData(
            quoteData.quoteText,
            quoteData.quoteSource,
            quoteData.quoteAuthor)
    }
}