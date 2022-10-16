package com.crossbowffs.quotelock.app.share

import android.graphics.drawable.Drawable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crossbowffs.quotelock.app.SnackBarEvent
import com.crossbowffs.quotelock.data.ShareRepository
import com.crossbowffs.quotelock.data.api.AndroidString
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
    data class SnackBar(val snackbar: SnackBarEvent) : ShareUiEvent()
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

    fun saveQuoteCard(
        containerColor: Color,
        contentColor: Color,
        watermark: Pair<String, Drawable?>? = null,
    ) {
        viewModelScope.launch {
            val shareFile =
                shareRepository.generateShareBitmap(containerColor, contentColor, watermark)
                    ?.let { shareRepository.saveBitmapPublic(it) }
            if (shareFile?.exists() == true) {
                _uiEvent.emit(
                    ShareUiEvent.SnackBar(
                        SnackBarEvent(
                            message = AndroidString.StringRes(
                                R.string.quote_image_share_saved,
                                arrayOf(shareFile.parentFile!!.path)
                            )
                        )
                    )
                )
            } else {
                _uiEvent.emit(
                    ShareUiEvent.SnackBar(
                        SnackBarEvent(
                            message = AndroidString.StringRes(R.string.quote_image_share_save_failed)
                        )
                    )
                )
            }
        }
    }

    fun shareQuoteCard(
        containerColor: Color,
        contentColor: Color,
        watermark: Pair<String, Drawable?>? = null,
    ) {
        viewModelScope.launch {
            val shareBitmap =
                shareRepository.generateShareBitmap(containerColor, contentColor, watermark)
            val shareFile = shareBitmap?.let { shareRepository.saveBitmapInternal(it) }
            if (shareFile?.exists() == true) {
                _uiEvent.emit(ShareUiEvent.ShareFile(shareFile))
            } else {
                _uiEvent.emit(
                    ShareUiEvent.SnackBar(
                        SnackBarEvent(
                            message = AndroidString.StringRes(R.string.quote_image_share_failed)
                        )
                    )
                )
            }
        }
    }

    fun uiEventHandled() {
        viewModelScope.launch {
            _uiEvent.emit(null)
        }
    }
}