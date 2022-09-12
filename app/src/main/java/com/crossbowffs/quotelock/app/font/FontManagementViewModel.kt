package com.crossbowffs.quotelock.app.font

import android.net.Uri
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crossbowffs.quotelock.data.AsyncResult
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
 * UI state for the font management list screen.
 */
data class FontManagementListUiState(
    val inAppFontItems: List<FontInfo>,
    val systemFontItems: List<FontInfoWithState>,
    val systemFontEnabled: Boolean = false,
    val systemTabScrollToBottom: Boolean,
    val inAppTabScrollToBottom: Boolean,
)

/**
 * Dialog UI state for the font management screen.
 */
sealed class FontManagementDialogUiState {
    data class ProgressDialog(val message: String?) : FontManagementDialogUiState()

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

    private val _uiListState =
        mutableStateOf(FontManagementListUiState(emptyList(), emptyList(),
            systemTabScrollToBottom = false,
            inAppTabScrollToBottom = false))
    val uiListState: State<FontManagementListUiState>
        get() = _uiListState

    private val _uiDialogState: MutableState<FontManagementDialogUiState> =
        mutableStateOf(FontManagementDialogUiState.None)
    val uiDialogState: State<FontManagementDialogUiState>
        get() = _uiDialogState

    init {
        viewModelScope.launch {
            loadInAppFontsList()
            val systemFontEnabled = FontManager.checkSystemCustomFontAvailable()
            _uiListState.value = _uiListState.value.copy(systemFontEnabled = systemFontEnabled)
            if (systemFontEnabled) {
                loadSystemFontsList()
            }
        }
    }

    private suspend fun loadInAppFontsList() {
        _uiListState.value =
            _uiListState.value.copy(inAppFontItems = FontManager.loadInAppFontsList()
                ?: emptyList())
    }

    private suspend fun loadSystemFontsList() {
        _uiListState.value =
            _uiListState.value.copy(systemFontItems = FontManager.loadSystemFontsList()
                ?: emptyList())
    }

    fun deleteInAppFont(it: FontInfo) = viewModelScope.launch {
        val message = resourceProvider.getString(if (FontManager.deleteInAppFont(it.fileName)) {
            R.string.quote_fonts_management_delete_in_app_font_successfully
        } else {
            R.string.quote_fonts_management_delete_font_failed
        }, resourceProvider.getFontLocaleName(it))
        _uiEvent.emit(FontManagementUiEvent.SnackBarMessage(message))
        loadInAppFontsList()
    }

    fun deleteSystemFont(it: FontInfoWithState) = viewModelScope.launch {
        val message = if (it.active) {
            if (FontManager.deleteActiveSystemFont(it.fontInfo.fileName)) {
                resourceProvider.getString(R.string.quote_fonts_management_delete_active_font_successfully)
            } else {
                resourceProvider.getString(R.string.quote_fonts_management_delete_font_failed,
                    resourceProvider.getFontLocaleName(it.fontInfo))
            }
        } else {
            resourceProvider.getString(if (FontManager.deleteInactiveSystemFont(it.fontInfo.fileName)) {
                R.string.quote_fonts_management_delete_inactive_font_successfully
            } else {
                R.string.quote_fonts_management_delete_font_failed
            }, resourceProvider.getFontLocaleName(it.fontInfo))
        }
        _uiEvent.emit(FontManagementUiEvent.SnackBarMessage(message))
        loadSystemFontsList()
    }

    fun importFontInApp(uri: Uri) = viewModelScope.launch {
        _uiDialogState.value = FontManagementDialogUiState.ProgressDialog(
            message = resourceProvider.getString(R.string.quote_fonts_management_importing))
        when (val result = fontImporter.importFontInApp(uri)) {
            is AsyncResult.Success -> {
                result.data.let { path ->
                    FontManager.loadFontInfo(File(path))
                }?.let {
                    loadInAppFontsList()
                    _uiListState.value = _uiListState.value.copy(inAppTabScrollToBottom = true)
                    _uiEvent.emit(FontManagementUiEvent.SnackBarMessage(
                        resourceProvider.getString(R.string.quote_fonts_management_font_imported,
                            resourceProvider.getFontLocaleName(it))))
                } ?: run {
                    _uiEvent.emit(FontManagementUiEvent.SnackBarMessage(
                        resourceProvider.getString(R.string.quote_fonts_management_import_failed)))
                }
            }
            is AsyncResult.Error -> {
                _uiEvent.emit(FontManagementUiEvent.SnackBarMessage(result.exception.message))
            }
            else -> {}
        }
        _uiDialogState.value = FontManagementDialogUiState.None
    }

    fun importFontToSystem(uri: Uri) = viewModelScope.launch {
        _uiDialogState.value = FontManagementDialogUiState.ProgressDialog(
            message = resourceProvider.getString(R.string.quote_fonts_management_importing))
        when (val result = fontImporter.importFontToSystem(uri)) {
            is AsyncResult.Success -> {
                result.data.let { path ->
                    FontManager.loadFontInfo(File(path))
                }?.let {
                    if (FontManager.isSystemFontActivated(it.fileName)) {
                        FontManager.deleteInactiveSystemFont(it.fileName)
                        resourceProvider.getString(R.string.quote_fonts_management_font_already_exists,
                            resourceProvider.getFontLocaleName(it))
                    } else {
                        loadSystemFontsList()
                        _uiListState.value = _uiListState.value.copy(systemTabScrollToBottom = true)
                        resourceProvider.getString(R.string.quote_fonts_management_font_imported,
                            resourceProvider.getFontLocaleName(it))
                    }.let { message ->
                        _uiEvent.emit(FontManagementUiEvent.SnackBarMessage(message))
                    }
                } ?: run {
                    _uiEvent.emit(FontManagementUiEvent.SnackBarMessage(
                        resourceProvider.getString(R.string.quote_fonts_management_import_failed)))
                }
            }
            is AsyncResult.Error -> {
                _uiEvent.emit(FontManagementUiEvent.SnackBarMessage(result.exception.message))
            }
            else -> {}
        }
        _uiDialogState.value = FontManagementDialogUiState.None
    }

    fun snackbarShown() = viewModelScope.launch {
        _uiEvent.emit(null)
    }

    fun inAppFontListScrolled() {
        _uiListState.value = _uiListState.value.copy(inAppTabScrollToBottom = false)
    }

    fun systemFontListScrolled() {
        _uiListState.value = _uiListState.value.copy(systemTabScrollToBottom = false)
    }
}