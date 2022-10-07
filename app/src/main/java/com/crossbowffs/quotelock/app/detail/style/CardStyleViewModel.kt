package com.crossbowffs.quotelock.app.detail.style

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crossbowffs.quotelock.app.font.FontInfo
import com.crossbowffs.quotelock.app.font.FontManager
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_FONT_ITALIC_SOURCE_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_FONT_ITALIC_TEXT_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_FONT_WEIGHT_SOURCE_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_FONT_WEIGHT_TEXT_DEFAULT
import com.crossbowffs.quotelock.data.CardStyleRepository
import com.crossbowffs.quotelock.data.api.CardStyle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * UI state for the quote card style popup.
 */
data class CardStyleUiState(
    val show: Boolean = false,
    val fonts: List<FontInfo>,
    val cardStyle: CardStyle,
)

@HiltViewModel
class CardStyleViewModel @Inject constructor(
    private val cardStyleRepository: CardStyleRepository,
) : ViewModel() {

    private val _uiState =
        mutableStateOf(
            CardStyleUiState(
                fonts = emptyList(),
                cardStyle = CardStyle()
            )
        )
    val uiState: State<CardStyleUiState> = _uiState

    fun showStylePopup() {
        val fonts = FontManager.loadAllFontsList()
        _uiState.value = _uiState.value.copy(
            show = true,
            fonts = fonts,
            cardStyle = cardStyleRepository.cardStyle
        )
        viewModelScope.launch {
            fonts.filter { it.families.isEmpty() }
                .forEach { (_, fileName, path) ->
                    val fontInfo = FontManager.loadFontInfo(File(path)) ?: return@forEach
                    _uiState.value.let {
                        _uiState.value = it.copy(
                            fonts = it.fonts.toMutableList().apply {
                                set(indexOfFirst { font -> font.fileName == fileName }, fontInfo)
                            }.toList()
                        )
                    }
                }
        }
    }

    fun selectFontFamily(fontFamily: String, fontSupportedFeatures: Int) {
        cardStyleRepository.quoteFontStyle = cardStyleRepository.quoteFontStyle.copy(
            family = fontFamily,
            supportedFeatures = fontSupportedFeatures,
            weight = PREF_CARD_STYLE_FONT_WEIGHT_TEXT_DEFAULT,
            italic = PREF_CARD_STYLE_FONT_ITALIC_TEXT_DEFAULT
        )
        cardStyleRepository.sourceFontStyle = cardStyleRepository.sourceFontStyle.copy(
            family = fontFamily,
            supportedFeatures = fontSupportedFeatures,
            weight = PREF_CARD_STYLE_FONT_WEIGHT_SOURCE_DEFAULT,
            italic = PREF_CARD_STYLE_FONT_ITALIC_SOURCE_DEFAULT
        )
    }

    fun setQuoteSize(size: Int) {
        cardStyleRepository.quoteSize = size
    }

    fun setSourceSize(size: Int) {
        cardStyleRepository.sourceSize = size
    }

    fun setLineSpacing(lineSpacing: Int) {
        cardStyleRepository.lineSpacing = lineSpacing
    }

    fun setCardPadding(padding: Int) {
        cardStyleRepository.cardPadding = padding
    }

    fun setQuoteWeight(weight: Int) {
        cardStyleRepository.quoteFontStyle =
            cardStyleRepository.quoteFontStyle.copy(weight = FontWeight(weight))
    }

    fun setQuoteItalic(italic: Float) {
        cardStyleRepository.quoteFontStyle =
            cardStyleRepository.quoteFontStyle.copy(italic = italic)
    }

    fun setSourceWeight(weight: Int) {
        cardStyleRepository.sourceFontStyle =
            cardStyleRepository.sourceFontStyle.copy(weight = FontWeight(weight))
    }

    fun setSourceItalic(italic: Float) {
        cardStyleRepository.sourceFontStyle =
            cardStyleRepository.sourceFontStyle.copy(italic = italic)
    }

    fun dismissStylePopup() {
        _uiState.value = _uiState.value.copy(show = false)
    }
}