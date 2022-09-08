package com.crossbowffs.quotelock.app.lockscreen.styles

import android.os.Build
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crossbowffs.quotelock.app.font.FontInfo
import com.crossbowffs.quotelock.app.font.FontManager
import com.crossbowffs.quotelock.data.ConfigurationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * UI event for the lockscreen styles screen.
 */
sealed class LockscreenStylesUiEvent {
    data class SnackBarMessage(
        val message: String? = null,
        val duration: SnackbarDuration = SnackbarDuration.Short,
        val actionText: String? = null,
    ) : LockscreenStylesUiEvent()
}

/**
 * UI state for the lockscreen styles screen.
 */
data class LockscreenStylesUiState(
    val enableFontFamily: Boolean,
)

/**
 * UI state for the lockscreen styles screen.
 */
sealed class LockscreenStylesDialogUiState {
    data class QuoteSizeDialog(
        val currentSize: Int,
    ) : LockscreenStylesDialogUiState()

    data class SourceSizeDialog(
        val currentSize: Int,
    ) : LockscreenStylesDialogUiState()

    data class QuoteStylesDialog(
        val currentStyles: Set<String>?,
    ) : LockscreenStylesDialogUiState()

    data class SourceStylesDialog(
        val currentStyles: Set<String>?,
    ) : LockscreenStylesDialogUiState()

    data class FontFamilyDialog(
        val fonts: List<FontInfo>,
        val currentFont: String,
    ) : LockscreenStylesDialogUiState()

    data class QuoteSpacingDialog(
        val spacing: Int,
    ) : LockscreenStylesDialogUiState()

    data class PaddingTopDialog(
        val padding: Int,
    ) : LockscreenStylesDialogUiState()

    data class PaddingBottomDialog(
        val padding: Int,
    ) : LockscreenStylesDialogUiState()

    object None : LockscreenStylesDialogUiState()
}

/**
 * @author Yubyf
 */
@HiltViewModel
class LockscreenStylesViewModel @Inject constructor(
    private val configurationRepository: ConfigurationRepository,
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<LockscreenStylesUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _uiState = mutableStateOf(
        LockscreenStylesUiState(
            // Only enable font family above API26
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
    )
    val uiState: State<LockscreenStylesUiState> = _uiState

    private val _uiDialogState =
        mutableStateOf<LockscreenStylesDialogUiState>(LockscreenStylesDialogUiState.None)
    val uiDialogState: State<LockscreenStylesDialogUiState> = _uiDialogState

    fun loadQuoteSize() {
        _uiDialogState.value =
            LockscreenStylesDialogUiState.QuoteSizeDialog(configurationRepository.quoteSize)
    }

    fun selectQuoteSize(size: Int) {
        configurationRepository.quoteSize = size
    }

    fun loadSourceSize() {
        _uiDialogState.value =
            LockscreenStylesDialogUiState.SourceSizeDialog(configurationRepository.sourceSize)
    }

    fun selectSourceSize(size: Int) {
        configurationRepository.sourceSize = size
    }

    fun loadQuoteStyles() {
        _uiDialogState.value =
            LockscreenStylesDialogUiState.QuoteStylesDialog(configurationRepository.quoteStyles)
    }

    fun selectQuoteStyles(styles: Set<String>?) {
        configurationRepository.quoteStyles = styles
    }

    fun loadSourceStyles() {
        _uiDialogState.value =
            LockscreenStylesDialogUiState.SourceStylesDialog(configurationRepository.sourceStyles)
    }

    fun selectSourceStyles(styles: Set<String>?) {
        configurationRepository.sourceStyles = styles
    }

    fun loadFontFamily() {
        val activeFonts = (FontManager.loadActiveFontsList() ?: emptyList())
        _uiDialogState.value = LockscreenStylesDialogUiState.FontFamilyDialog(fonts = activeFonts,
            currentFont = configurationRepository.fontFamily)
        viewModelScope.launch {
            activeFonts.forEach { (_, fileName, path) ->
                val fontInfo = FontManager.loadFontInfo(File(path)) ?: return@forEach
                (_uiDialogState.value as? LockscreenStylesDialogUiState.FontFamilyDialog)?.let {
                    _uiDialogState.value = it.copy(
                        fonts = it.fonts.toMutableList().apply {
                            set(indexOfFirst { font -> font.fileName == fileName }, fontInfo)
                        }.toList()
                    )
                }
            }
        }
    }

    fun selectFontFamily(fontFamily: String) {
        configurationRepository.fontFamily = fontFamily
    }

    fun loadQuoteSpacing() {
        _uiDialogState.value =
            LockscreenStylesDialogUiState.QuoteSpacingDialog(configurationRepository.quoteSpacing)
    }

    fun selectQuoteSpacing(spacing: Int) {
        configurationRepository.quoteSpacing = spacing
    }

    fun loadPaddingTop() {
        _uiDialogState.value =
            LockscreenStylesDialogUiState.PaddingTopDialog(configurationRepository.paddingTop)
    }

    fun selectPaddingTop(padding: Int) {
        configurationRepository.paddingTop = padding
    }

    fun loadPaddingBottom() {
        _uiDialogState.value =
            LockscreenStylesDialogUiState.PaddingBottomDialog(configurationRepository.paddingBottom)
    }

    fun selectPaddingBottom(padding: Int) {
        configurationRepository.paddingBottom = padding
    }

    fun cancelDialog() {
        _uiDialogState.value = LockscreenStylesDialogUiState.None
    }
}