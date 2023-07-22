package com.crossbowffs.quotelock.app.font

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crossbowffs.quotelock.app.SnackBarEvent
import com.crossbowffs.quotelock.app.emptySnackBarEvent
import com.crossbowffs.quotelock.data.AsyncResult
import com.crossbowffs.quotelock.data.api.AndroidString
import com.crossbowffs.quotelock.data.exceptionMessage
import com.yubyf.quotelockx.R
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import java.io.File

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
    data class ProgressDialog(val message: AndroidString?) : FontManagementDialogUiState()

    object None : FontManagementDialogUiState()
}

/**
 * @author Yubyf
 */
@KoinViewModel
class FontManagementViewModel(
    private val fontImporter: FontImporter,
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<SnackBarEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _uiListState =
        mutableStateOf(
            FontManagementListUiState(
                emptyList(), emptyList(),
                systemTabScrollToBottom = false,
                inAppTabScrollToBottom = false
            )
        )
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
            _uiListState.value.copy(
                inAppFontItems = FontManager.loadInAppFontsList()
                    ?: emptyList()
            )
    }

    private suspend fun loadSystemFontsList() {
        _uiListState.value =
            _uiListState.value.copy(
                systemFontItems = FontManager.loadSystemFontsList()
                    ?: emptyList()
            )
    }

    fun deleteInAppFont(it: FontInfo) = viewModelScope.launch {
        val message = AndroidString.StringRes(
            if (FontManager.deleteInAppFont(it.fileName)) {
                R.string.quote_fonts_management_delete_in_app_font_successfully
            } else {
                R.string.quote_fonts_management_delete_font_failed
            }, arrayOf(it)
        )
        _uiEvent.emit(SnackBarEvent(message))
        loadInAppFontsList()
    }

    fun deleteSystemFont(it: FontInfoWithState) = viewModelScope.launch {
        val message = if (it.active) {
            if (FontManager.deleteActiveSystemFont(it.fontInfo.fileName)) {
                AndroidString.StringRes(R.string.quote_fonts_management_delete_active_font_successfully)
            } else {
                AndroidString.StringRes(
                    R.string.quote_fonts_management_delete_font_failed,
                    arrayOf(it.fontInfo)
                )
            }
        } else {
            AndroidString.StringRes(
                if (FontManager.deleteInactiveSystemFont(it.fontInfo.fileName)) {
                    R.string.quote_fonts_management_delete_inactive_font_successfully
                } else {
                    R.string.quote_fonts_management_delete_font_failed
                }, arrayOf(it.fontInfo)
            )
        }
        _uiEvent.emit(SnackBarEvent(message))
        loadSystemFontsList()
    }

    fun importFontInApp(uri: Uri) = viewModelScope.launch {
        _uiDialogState.value = FontManagementDialogUiState.ProgressDialog(
            message = AndroidString.StringRes(R.string.quote_fonts_management_importing)
        )
        when (val result = fontImporter.importFontInApp(uri)) {
            is AsyncResult.Success -> {
                result.data.let { path ->
                    FontManager.loadFontInfo(File(path))
                }?.let {
                    loadInAppFontsList()
                    _uiListState.value = _uiListState.value.copy(inAppTabScrollToBottom = true)
                    _uiEvent.emit(
                        SnackBarEvent(
                            AndroidString.StringRes(
                                R.string.quote_fonts_management_font_imported, arrayOf(it)
                            )
                        )
                    )
                } ?: run {
                    _uiEvent.emit(
                        SnackBarEvent(
                            AndroidString.StringRes(R.string.quote_fonts_management_import_failed)
                        )
                    )
                }
            }

            is AsyncResult.Error.Message -> {
                _uiEvent.emit(SnackBarEvent(result.message))
            }

            is AsyncResult.Error.ExceptionWrapper -> {
                if (result.exception is FontImporter.FontAlreadyExistsException) {
                    _uiEvent.emit(
                        SnackBarEvent(
                            AndroidString.StringRes(
                                R.string.quote_fonts_management_font_already_exists,
                                arrayOf(result.exception.name)
                            )
                        )
                    )
                } else {
                    _uiEvent.emit(SnackBarEvent(result.exceptionMessage?.let(AndroidString::StringText)))
                }
            }

            else -> {}
        }
        _uiDialogState.value = FontManagementDialogUiState.None
    }

    fun importFontToSystem(uri: Uri) = viewModelScope.launch {
        _uiDialogState.value = FontManagementDialogUiState.ProgressDialog(
            message = AndroidString.StringRes(R.string.quote_fonts_management_importing)
        )
        when (val result = fontImporter.importFontToSystem(uri)) {
            is AsyncResult.Success -> {
                result.data.let { path ->
                    FontManager.loadFontInfo(File(path))
                }?.let {
                    if (FontManager.isSystemFontActivated(it.fileName)) {
                        FontManager.deleteInactiveSystemFont(it.fileName)
                        AndroidString.StringRes(
                            R.string.quote_fonts_management_font_already_exists,
                            arrayOf(it)
                        )
                    } else {
                        loadSystemFontsList()
                        _uiListState.value = _uiListState.value.copy(systemTabScrollToBottom = true)
                        AndroidString.StringRes(
                            R.string.quote_fonts_management_font_imported,
                            arrayOf(it)
                        )
                    }.let { message ->
                        _uiEvent.emit(SnackBarEvent(message))
                    }
                } ?: run {
                    _uiEvent.emit(
                        SnackBarEvent(
                            AndroidString.StringRes(R.string.quote_fonts_management_import_failed)
                        )
                    )
                }
            }

            is AsyncResult.Error.Message -> {
                _uiEvent.emit(SnackBarEvent(result.message))
            }

            is AsyncResult.Error.ExceptionWrapper -> {
                if (result.exception is FontImporter.FontAlreadyExistsException) {
                    _uiEvent.emit(
                        SnackBarEvent(
                            AndroidString.StringRes(
                                R.string.quote_fonts_management_font_already_exists,
                                arrayOf(result.exception.name)
                            )
                        )
                    )
                } else {
                    _uiEvent.emit(SnackBarEvent(result.exceptionMessage?.let(AndroidString::StringText)))
                }
            }

            else -> {}
        }
        _uiDialogState.value = FontManagementDialogUiState.None
    }

    fun snackBarShown() = viewModelScope.launch {
        _uiEvent.emit(emptySnackBarEvent)
    }

    fun inAppFontListScrolled() {
        _uiListState.value = _uiListState.value.copy(inAppTabScrollToBottom = false)
    }

    fun systemFontListScrolled() {
        _uiListState.value = _uiListState.value.copy(systemTabScrollToBottom = false)
    }
}