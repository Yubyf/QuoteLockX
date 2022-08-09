package com.crossbowffs.quotelock.app.configs.collections

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crossbowffs.quotelock.data.AsyncResult
import com.crossbowffs.quotelock.data.failedMessage
import com.crossbowffs.quotelock.data.modules.collections.QuoteCollectionRepository
import com.crossbowffs.quotelock.data.modules.collections.database.QuoteCollectionEntity
import com.crossbowffs.quotelock.di.ResourceProvider
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.yubyf.quotelockx.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@Retention(AnnotationRetention.SOURCE)
annotation class LocalBackupType {
    companion object {
        const val DB = 0
        const val CSV = 1
    }
}

/**
 * UI event for the quote collection list screen.
 */
sealed class QuoteCollectionUiEvent {
    abstract val message: String?

    data class SnackBarMessage(
        override val message: String? = null,
        @BaseTransientBottomBar.Duration val duration: Int = Snackbar.LENGTH_SHORT,
        val actionText: String? = null,
    ) : QuoteCollectionUiEvent()

    data class ProgressMessage(
        val show: Boolean = false,
        override val message: String? = null,
    ) : QuoteCollectionUiEvent()
}

/**
 * UI state for the quote collection list screen.
 */
data class QuoteCollectionListUiState(
    val items: List<QuoteCollectionEntity>,
    val exportEnabled: Boolean,
)

/**
 * @author Yubyf
 */
@HiltViewModel
class QuoteCollectionViewModel @Inject constructor(
    private val collectionRepository: QuoteCollectionRepository,
    private val resourceProvider: ResourceProvider,
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<QuoteCollectionUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _uiListState: MutableStateFlow<QuoteCollectionListUiState> =
        MutableStateFlow(QuoteCollectionListUiState(emptyList(), false))
    val uiListState = _uiListState.asStateFlow()

    init {
        viewModelScope.run {
            launch {
                collectionRepository.getAllStream().stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList()
                ).collect {
                    _uiListState.update { currentState ->
                        currentState.copy(items = it, exportEnabled = it.isNotEmpty())
                    }
                }
            }
        }
    }

    fun export(@LocalBackupType type: Int) {
        viewModelScope.launch {
            _uiEvent.emit(QuoteCollectionUiEvent.ProgressMessage(
                show = true,
                message = resourceProvider.getString(
                    if (type == LocalBackupType.CSV) R.string.exporting_csv
                    else R.string.exporting_database
                )
            ))
            val result =
                if (type == LocalBackupType.CSV) collectionRepository.exportCsv()
                else collectionRepository.exportDatabase()
            _uiEvent.emit(QuoteCollectionUiEvent.ProgressMessage(false))
            when (result) {
                is AsyncResult.Success -> {
                    _uiEvent.emit(QuoteCollectionUiEvent.SnackBarMessage(
                        message = resourceProvider.getString(R.string.database_exported)
                            .plus(" ")
                            .plus(result.data),
                        duration = Snackbar.LENGTH_LONG,
                        actionText = resourceProvider.getString(R.string.ok),
                    ))
                }
                is AsyncResult.Error -> {
                    _uiEvent.emit(QuoteCollectionUiEvent.SnackBarMessage(message = result.failedMessage))
                }
                is AsyncResult.Loading -> {}
            }
        }
    }

    fun import(@LocalBackupType type: Int, uri: Uri) {
        viewModelScope.launch {
            _uiEvent.emit(QuoteCollectionUiEvent.ProgressMessage(
                show = true,
                message = resourceProvider.getString(
                    if (type == LocalBackupType.CSV) R.string.importing_csv
                    else R.string.importing_database
                )
            ))
            val result =
                if (type == LocalBackupType.CSV) collectionRepository.importCsv(uri)
                else collectionRepository.importDatabase(uri)
            _uiEvent.emit(QuoteCollectionUiEvent.ProgressMessage(false))
            when (result) {
                is AsyncResult.Success -> {
                    _uiEvent.emit(QuoteCollectionUiEvent.SnackBarMessage(
                        message = resourceProvider.getString(R.string.database_imported)
                    ))
                }
                is AsyncResult.Error -> {
                    _uiEvent.emit(QuoteCollectionUiEvent.SnackBarMessage(message = result.failedMessage))
                }
                is AsyncResult.Loading -> {}
            }
        }
    }

    fun updateDriveService() = collectionRepository.updateDriveService()

    fun ensureDriveService() = collectionRepository.ensureDriveService()

    fun gDriveBackup() {
        ensureDriveService()
        viewModelScope.launch {
            _uiEvent.emit(QuoteCollectionUiEvent.ProgressMessage(
                show = true,
                message = resourceProvider.getString(R.string.start_google_drive_backup)
            ))
            collectionRepository.gDriveBackup().collect {
                when (it) {
                    is AsyncResult.Success -> {
                        _uiEvent.emit(QuoteCollectionUiEvent.ProgressMessage(false))
                        _uiEvent.emit(QuoteCollectionUiEvent.SnackBarMessage(
                            message = resourceProvider.getString(R.string.remote_backup_completed)
                        ))
                    }
                    is AsyncResult.Error -> {
                        _uiEvent.emit(QuoteCollectionUiEvent.ProgressMessage(false))
                        _uiEvent.emit(QuoteCollectionUiEvent.SnackBarMessage(message = it.failedMessage))
                    }
                    is AsyncResult.Loading -> {
                        _uiEvent.emit(QuoteCollectionUiEvent.ProgressMessage(
                            show = true,
                            message = it.message
                        ))
                    }
                }
            }
        }
    }

    fun gDriveRestore() {
        ensureDriveService()
        viewModelScope.launch {
            _uiEvent.emit(QuoteCollectionUiEvent.ProgressMessage(
                show = true,
                message = resourceProvider.getString(R.string.start_google_drive_restore)
            ))
            collectionRepository.gDriveRestore().collect {
                when (it) {
                    is AsyncResult.Success -> {
                        _uiEvent.emit(QuoteCollectionUiEvent.ProgressMessage(false))
                        _uiEvent.emit(QuoteCollectionUiEvent.SnackBarMessage(
                            message = resourceProvider.getString(R.string.remote_restore_completed)
                        ))
                    }
                    is AsyncResult.Error -> {
                        _uiEvent.emit(QuoteCollectionUiEvent.ProgressMessage(false))
                        _uiEvent.emit(QuoteCollectionUiEvent.SnackBarMessage(message = it.failedMessage))
                    }
                    is AsyncResult.Loading -> {
                        _uiEvent.emit(QuoteCollectionUiEvent.ProgressMessage(
                            show = true,
                            message = it.message
                        ))
                    }
                }
            }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            collectionRepository.delete(id)
            _uiEvent.emit(QuoteCollectionUiEvent.SnackBarMessage(
                resourceProvider.getString(R.string.module_custom_deleted_quote))
            )
        }
    }
}