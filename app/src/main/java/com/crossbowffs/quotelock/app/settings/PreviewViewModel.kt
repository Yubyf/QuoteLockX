package com.crossbowffs.quotelock.app.settings

import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crossbowffs.quotelock.app.font.FontManager
import com.crossbowffs.quotelock.consts.*
import com.crossbowffs.quotelock.data.ConfigurationRepository
import com.crossbowffs.quotelock.data.api.QuoteStyle
import com.crossbowffs.quotelock.data.modules.QuoteRepository
import com.crossbowffs.quotelock.di.ResourceProvider
import com.crossbowffs.quotelock.utils.dp2px
import com.crossbowffs.quotelock.utils.getTypefaceStyle
import com.yubyf.quotelockx.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the settings screen.
 */
data class PreviewUiState(val quoteViewData: QuoteViewData, val quoteStyle: QuoteStyle)

data class QuoteViewData(val text: String = "", val source: String = "")

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
            configurationRepository.getQuoteStyle()))
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
                                    quoteViewData = buildQuoteViewData(quote, source, author)
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
                                        ?: PREF_COMMON_FONT_SIZE_TEXT_DEFAULT).toFloat())
                            )
                        }
                        PREF_COMMON_FONT_SIZE_SOURCE -> _uiState.update { currentState ->
                            currentState.copy(
                                quoteStyle = currentState.quoteStyle.copy(
                                    sourceSize = (preferences[stringPreferencesKey(
                                        PREF_COMMON_FONT_SIZE_SOURCE)]
                                        ?: PREF_COMMON_FONT_SIZE_SOURCE_DEFAULT).toFloat())
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
                                        quoteStyle = getTypefaceStyle(quoteStyles)
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
                                        sourceStyle = getTypefaceStyle(sourceStyles)
                                    )
                                )
                            }
                        }
                        PREF_COMMON_QUOTE_SPACING -> _uiState.update { currentState ->
                            currentState.copy(
                                quoteStyle = currentState.quoteStyle.copy(
                                    quoteSpacing = (preferences[stringPreferencesKey(
                                        PREF_COMMON_QUOTE_SPACING)]
                                        ?: PREF_COMMON_QUOTE_SPACING_DEFAULT).toInt().dp2px()
                                        .toInt()
                                )
                            )
                        }
                        PREF_COMMON_PADDING_TOP -> _uiState.update { currentState ->
                            currentState.copy(
                                quoteStyle = currentState.quoteStyle.copy(
                                    paddingTop = (preferences[stringPreferencesKey(
                                        PREF_COMMON_PADDING_TOP)]
                                        ?: PREF_COMMON_PADDING_TOP_DEFAULT).toInt().dp2px().toInt()
                                )
                            )
                        }
                        PREF_COMMON_PADDING_BOTTOM -> _uiState.update { currentState ->
                            currentState.copy(
                                quoteStyle = currentState.quoteStyle.copy(
                                    paddingBottom = (preferences[stringPreferencesKey(
                                        PREF_COMMON_PADDING_BOTTOM)]
                                        ?: PREF_COMMON_PADDING_BOTTOM_DEFAULT).toInt().dp2px()
                                        .toInt()
                                )
                            )
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun getQuoteViewData(): QuoteViewData {
        val quoteData = quoteRepository.getCurrentQuote()
        return buildQuoteViewData(quoteData.quoteText, quoteData.quoteSource, quoteData.quoteAuthor)
    }

    private fun buildQuoteViewData(text: String, source: String?, author: String?): QuoteViewData {
        return QuoteViewData(
            text,
            author.let {
                if (!it.isNullOrBlank()) {
                    "$PREF_QUOTE_SOURCE_PREFIX$it${if (source.isNullOrBlank()) "" else " $source"}"
                } else {
                    if (source == resourceProvider.getString(R.string.module_custom_setup_line2)
                        || source == resourceProvider.getString(R.string.module_collections_setup_line2)
                    ) {
                        source
                    } else {
                        if (source.isNullOrBlank()) {
                            ""
                        } else {
                            "$PREF_QUOTE_SOURCE_PREFIX$source"
                        }
                    }
                }
            }
        )
    }
}