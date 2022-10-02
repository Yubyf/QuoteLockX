package com.crossbowffs.quotelock.app.share

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crossbowffs.quotelock.app.SnackBarEvent
import com.crossbowffs.quotelock.data.ShareRepository
import com.crossbowffs.quotelock.di.ResourceProvider
import com.crossbowffs.quotelock.ui.components.Snapshotables
import com.yubyf.quotelockx.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * UI event for the share screen.
 */
sealed class ShareUiEvent {
    data class SaveFile(val snackbar: SnackBarEvent) : ShareUiEvent()
    data class ShareFile(val imageFile: File?) : ShareUiEvent()
}

/**
 * UI state for the share screen.
 */
data class ShareUiState(val snapshot: Snapshotables?)

/**
 * @author Yubyf
 */
@HiltViewModel
class ShareViewModel @Inject constructor(
    private val shareRepository: ShareRepository,
    private val resourceProvider: ResourceProvider,
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<ShareUiEvent?>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _uiState =
        mutableStateOf(ShareUiState(snapshot = shareRepository.currentSnapshotables))
    val uiState: State<ShareUiState> = _uiState

    override fun onCleared() {
        super.onCleared()
        shareRepository.currentSnapshotables = null
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    fun saveQuoteCard(
        containerColor: Color,
        contentColor: Color,
        watermark: Boolean,
    ) {
        viewModelScope.launch {
            val shareFile =
                shareRepository.generateShareBitmap(containerColor, contentColor, watermark)
                    ?.let { shareRepository.saveBitmapPublic(it) }
            if (shareFile?.exists() == true) {
                _uiEvent.emit(ShareUiEvent.SaveFile(SnackBarEvent(
                    message = resourceProvider.getString(R.string.quote_image_share_saved,
                        shareFile.parentFile!!.path)
                )))
            } else {
                _uiEvent.emit(ShareUiEvent.SaveFile(SnackBarEvent(
                    message = resourceProvider.getString(R.string.quote_image_share_save_failed)
                )))
            }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    fun shareQuoteCard(
        containerColor: Color,
        contentColor: Color,
        watermark: Boolean,
    ) {
        viewModelScope.launch {
            val shareFile =
                shareRepository.generateShareBitmap(containerColor, contentColor, watermark)
                    ?.let { shareRepository.saveBitmapInternal(it) }
            _uiEvent.emit(ShareUiEvent.ShareFile(shareFile))
        }
    }

    fun uiEventHandled() {
        viewModelScope.launch {
            _uiEvent.emit(null)
        }
    }
}