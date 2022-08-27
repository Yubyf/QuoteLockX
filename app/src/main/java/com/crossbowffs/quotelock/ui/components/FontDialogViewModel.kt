package com.crossbowffs.quotelock.ui.components

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crossbowffs.quotelock.app.font.FontManager
import com.crossbowffs.quotelock.consts.PREF_COMMON_FONT_FAMILY_DEFAULT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

data class FontDialogUiState(
    val fontDisplayNames: List<String?>,
)

@HiltViewModel
class FontDialogViewModel @Inject constructor() : ViewModel() {

    private val _uiState = mutableStateOf(FontDialogUiState(emptyList()))
    val uiState: State<FontDialogUiState> = _uiState

    fun loadFontFamilies(fontPaths: Array<String>) {
        viewModelScope.launch {
            val lazyFontDisplayNames = mutableListOf<String?>()
            fontPaths.forEach {
                val displayName =
                    if (it == PREF_COMMON_FONT_FAMILY_DEFAULT) {
                        null
                    } else {
                        withContext(Dispatchers.IO) {
                            FontManager.loadFontInfo(File(it))
                        }?.name
                    }
                lazyFontDisplayNames += displayName
                _uiState.value = FontDialogUiState(lazyFontDisplayNames.toList())
            }
        }
    }
}