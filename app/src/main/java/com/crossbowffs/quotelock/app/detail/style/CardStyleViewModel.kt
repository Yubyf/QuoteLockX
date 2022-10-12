package com.crossbowffs.quotelock.app.detail.style

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crossbowffs.quotelock.app.font.FontInfo
import com.crossbowffs.quotelock.app.font.FontManager
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_FONT_SUPPORTED_FEATURES_SLANT
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_FONT_SUPPORTED_FEATURES_WEIGHT
import com.crossbowffs.quotelock.data.CardStyleRepository
import com.crossbowffs.quotelock.data.api.CardStyle
import com.crossbowffs.quotelock.data.api.TextFontStyle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import kotlin.math.roundToInt

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
            cardStyleRepository.cardStyleFlow.onEach { cardStyle ->
                _uiState.value = _uiState.value.copy(cardStyle = cardStyle)
            }.launchIn(viewModelScope)
        }
    }

    fun selectFontFamily(fontInfo: FontInfo) {
        val supportedFeatures = (if (!fontInfo.supportVariableWeight) 0
        else PREF_CARD_STYLE_FONT_SUPPORTED_FEATURES_WEIGHT) or (if (!fontInfo.supportVariableSlant) 0
        else PREF_CARD_STYLE_FONT_SUPPORTED_FEATURES_SLANT)

        val minWeight = FontWeight(
            fontInfo.variableWeight?.range?.start ?: FontWeight.Normal.weight
        )
        val maxWeight = FontWeight(
            fontInfo.variableWeight?.range?.endInclusive ?: FontWeight.Normal.weight
        )
        val minSlant = fontInfo.variableSlant?.range?.start ?: 0f
        val maxSlant = fontInfo.variableSlant?.range?.endInclusive ?: 0f
        fun TextFontStyle.migrateWeight() = if (fontInfo.supportVariableWeight) {
            when {
                weight < minWeight -> minWeight
                weight > maxWeight -> maxWeight
                else -> weight
            }
        } else if (weight >= FontWeight.Bold) FontWeight.Bold else FontWeight.Normal

        fun TextFontStyle.migrateItalic() = if (fontInfo.supportVariableSlant) {
            if (!supportVariableSlant) {
                if (italic.roundToInt() != FontStyle.Italic.value) {
                    fontInfo.variableSlant?.default ?: 0F
                } else minSlant
            } else {
                when {
                    italic < minSlant -> minSlant
                    italic > maxSlant -> maxSlant
                    else -> italic
                }
            }
        } else {
            if (!supportVariableSlant) {
                if (italic.roundToInt() != FontStyle.Italic.value) {
                    FontStyle.Normal.value.toFloat()
                } else {
                    FontStyle.Italic.value.toFloat()
                }
            } else {
                if (italic.roundToInt() == 0) {
                    FontStyle.Normal.value.toFloat()
                } else {
                    FontStyle.Italic.value.toFloat()
                }
            }
        }
        cardStyleRepository.quoteFontStyle =
            cardStyleRepository.quoteFontStyle.let { currentStyle ->
                currentStyle.copy(
                    family = fontInfo.path,
                    supportedFeatures = supportedFeatures,
                    weight = currentStyle.migrateWeight(),
                    minWeight = minWeight,
                    maxWeight = maxWeight,
                    italic = currentStyle.migrateItalic(),
                    minSlant = minSlant,
                    maxSlant = maxSlant
                )
            }
        cardStyleRepository.sourceFontStyle =
            cardStyleRepository.sourceFontStyle.let { currentStyle ->
                currentStyle.copy(
                    family = fontInfo.path,
                    supportedFeatures = supportedFeatures,
                    weight = currentStyle.migrateWeight(),
                    minWeight = minWeight,
                    maxWeight = maxWeight,
                    italic = currentStyle.migrateItalic(),
                    minSlant = minSlant,
                    maxSlant = maxSlant
                )
            }
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