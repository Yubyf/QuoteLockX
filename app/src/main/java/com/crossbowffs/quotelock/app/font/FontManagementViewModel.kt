package com.crossbowffs.quotelock.app.font

import android.net.Uri
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crossbowffs.quotelock.di.ResourceProvider
import com.yubyf.quotelockx.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * UI event for the font management list screen.
 */
sealed class FontManagementUiEvent {
    data class SnackBarMessage(
        val message: String? = null,
        val duration: SnackbarDuration = SnackbarDuration.Short,
        val actionText: String? = null,
    ) : FontManagementUiEvent()
}

/**
 * UI state for the quote history list screen.
 */
data class FontManagementListUiState(
    val items: List<FontInfoWithState>,
    val scrollToBottom: Boolean,
)

/**
 * UI state for the settings screen.
 */
sealed class FontManagementDialogUiState {
    data class ProgressDialog(val message: String?) : FontManagementDialogUiState()

    object EnableMagiskModuleDialog : FontManagementDialogUiState()

    object None : FontManagementDialogUiState()
}

/**
 * @author Yubyf
 */
@HiltViewModel
class FontManagementViewModel @Inject constructor(
    private val fontImporter: FontImporter,
    private val resourceProvider: ResourceProvider,
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<FontManagementUiEvent?>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _uiListState = mutableStateOf(FontManagementListUiState(emptyList(), false))
    val uiListState: State<FontManagementListUiState>
        get() = _uiListState

    private val _uiDialogState: MutableState<FontManagementDialogUiState> =
        mutableStateOf(FontManagementDialogUiState.None)
    val uiDialogState: State<FontManagementDialogUiState>
        get() = _uiDialogState

    init {
        if (FontManager.checkSystemCustomFontAvailable()) {
            viewModelScope.launch { loadFontsList() }
        } else {
            _uiDialogState.value = FontManagementDialogUiState.EnableMagiskModuleDialog
        }
    }

    private suspend fun loadFontsList() {
        _uiListState.value =
            _uiListState.value.copy(items = FontManager.loadFontsList() ?: emptyList())
    }

    fun delete(it: FontInfoWithState) = viewModelScope.launch {
        val message = if (it.active) {
            if (FontManager.deleteActiveSystemFont(it.fontInfo.fileName)) {
                resourceProvider.getString(R.string.quote_fonts_management_delete_active_font_successfully)
            } else {
                resourceProvider.getString(R.string.quote_fonts_management_delete_font_failed,
                    it.fontInfo.name)
            }
        } else {
            resourceProvider.getString(if (FontManager.deleteInactiveFont(it.fontInfo.fileName)) {
                R.string.quote_fonts_management_delete_inactive_font_successfully
            } else {
                R.string.quote_fonts_management_delete_font_failed
            }, it.fontInfo.name)
        }
        _uiEvent.emit(FontManagementUiEvent.SnackBarMessage(message))
        loadFontsList()
    }

    fun importFont(uri: Uri) = viewModelScope.launch {
        _uiDialogState.value = FontManagementDialogUiState.ProgressDialog(
            message = resourceProvider.getString(R.string.quote_fonts_management_importing))
        fontImporter.importFont(uri).takeIf { !it.isNullOrEmpty() }?.let { path ->
            FontManager.loadFontInfo(File(path))
        }?.let {
            if (FontManager.isFontActivated(it.fileName)) {
                FontManager.deleteInactiveFont(it.fileName)
                resourceProvider.getString(R.string.quote_fonts_management_font_already_exists,
                    it.name)
            } else {
                loadFontsList()
                _uiListState.value = _uiListState.value.copy(scrollToBottom = true)
                resourceProvider.getString(R.string.quote_fonts_management_font_imported,
                    it.name)
            }.let { message ->
                _uiEvent.emit(FontManagementUiEvent.SnackBarMessage(message))
            }
        } ?: run {
            _uiEvent.emit(FontManagementUiEvent.SnackBarMessage(
                resourceProvider.getString(R.string.quote_fonts_management_import_failed)))
        }
        _uiDialogState.value = FontManagementDialogUiState.None
    }

    fun snackbarShown() = viewModelScope.launch {
        _uiEvent.emit(null)
    }

    fun listScrolled() {
        _uiListState.value = _uiListState.value.copy(scrollToBottom = false)
    }

    fun cancelDialog() {
        _uiDialogState.value = FontManagementDialogUiState.None
    }
}