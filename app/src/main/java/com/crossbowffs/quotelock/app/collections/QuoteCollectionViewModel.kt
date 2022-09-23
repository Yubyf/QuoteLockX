package com.crossbowffs.quotelock.app.collections

import android.content.Intent
import android.net.Uri
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crossbowffs.quotelock.account.SyncAccountManager
import com.crossbowffs.quotelock.account.google.GoogleAccountManager
import com.crossbowffs.quotelock.app.SnackBarEvent
import com.crossbowffs.quotelock.app.emptySnackBarEvent
import com.crossbowffs.quotelock.data.AsyncResult
import com.crossbowffs.quotelock.data.api.GoogleAccount
import com.crossbowffs.quotelock.data.failedMessage
import com.crossbowffs.quotelock.data.modules.collections.QuoteCollectionRepository
import com.crossbowffs.quotelock.data.modules.collections.database.QuoteCollectionEntity
import com.crossbowffs.quotelock.di.ResourceProvider
import com.yubyf.quotelockx.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
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
sealed class QuoteCollectionDialogUiState {
    data class ProgressDialog(val message: String? = null) : QuoteCollectionDialogUiState()

    object None : QuoteCollectionDialogUiState()
}

/**
 * UI state for the quote collection menu.
 */
data class QuoteCollectionMenuUiState(
    val exportEnabled: Boolean = false,
    val syncEnabled: Boolean = false,
    val googleAccount: GoogleAccount? = null,
    val syncTime: String? = null,
)

/**
 * UI state for the quote collection list screen.
 */
data class QuoteCollectionListUiState(
    val items: List<QuoteCollectionEntity> = emptyList(),
)

/**
 * @author Yubyf
 */
@HiltViewModel
class QuoteCollectionViewModel @Inject constructor(
    private val collectionRepository: QuoteCollectionRepository,
    private val googleAccountManager: GoogleAccountManager,
    private val syncAccountManager: SyncAccountManager,
    private val resourceProvider: ResourceProvider,
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<SnackBarEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _uiDialogState =
        mutableStateOf<QuoteCollectionDialogUiState>(QuoteCollectionDialogUiState.None)
    val uiDialogState: State<QuoteCollectionDialogUiState> = _uiDialogState

    private val _uiMenuState = mutableStateOf(QuoteCollectionMenuUiState(
        syncEnabled = googleAccountManager.checkGooglePlayService(),
        googleAccount = googleAccountManager.getSignedInGoogleAccount(),
    ))
    val uiMenuState: State<QuoteCollectionMenuUiState> = _uiMenuState

    private val _uiListState = mutableStateOf(QuoteCollectionListUiState())
    val uiListState: State<QuoteCollectionListUiState> = _uiListState

    init {
        viewModelScope.launch {
            collectionRepository.getAllStream().stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            ).onEach {
                _uiListState.value = _uiListState.value.copy(items = it)
                _uiMenuState.value = _uiMenuState.value.copy(
                    exportEnabled = it.isNotEmpty()
                )
            }.launchIn(this)
            if (googleAccountManager.checkGooglePlayService()) {
                collectionRepository.getGDriveFileTimestamp().onEach {
                    _uiMenuState.value = _uiMenuState.value.copy(
                        syncTime = if (it > 0) DATE_FORMATTER.format(Date(it)) else null
                    )
                }.launchIn(this)
                if (googleAccountManager.isGoogleAccountSignedIn()) {
                    collectionRepository.queryDriveFileTimestamp()
                }
            }
        }
    }

    fun onPermissionDenied() {
        viewModelScope.launch {
            _uiEvent.emit(
                SnackBarEvent(
                    message = resourceProvider.getString(R.string.grant_storage_permission_tips),
                    duration = SnackbarDuration.Short
                )
            )
        }
    }

    fun export(@LocalBackupType type: Int) {
        viewModelScope.launch {
            _uiDialogState.value = QuoteCollectionDialogUiState.ProgressDialog(
                message = resourceProvider.getString(
                    if (type == LocalBackupType.CSV) R.string.exporting_csv
                    else R.string.exporting_database
                )
            )
            val result =
                if (type == LocalBackupType.CSV) collectionRepository.exportCsv()
                else collectionRepository.exportDatabase()
            _uiDialogState.value = QuoteCollectionDialogUiState.None
            when (result) {
                is AsyncResult.Success -> {
                    _uiEvent.emit(SnackBarEvent(
                        message = resourceProvider.getString(
                            if (type == LocalBackupType.CSV) R.string.csv_exported
                            else R.string.database_exported)
                            .plus(" ")
                            .plus(result.data),
                        duration = SnackbarDuration.Long,
                        actionText = resourceProvider.getString(R.string.ok),
                    ))
                }
                is AsyncResult.Error -> {
                    _uiEvent.emit(SnackBarEvent(message = result.failedMessage))
                }
                is AsyncResult.Loading -> {}
            }
        }
    }

    fun import(@LocalBackupType type: Int, uri: Uri) {
        viewModelScope.launch {
            _uiDialogState.value = QuoteCollectionDialogUiState.ProgressDialog(
                message = resourceProvider.getString(
                    if (type == LocalBackupType.CSV) R.string.importing_csv
                    else R.string.importing_database
                )
            )
            val result =
                if (type == LocalBackupType.CSV) collectionRepository.importCsv(uri)
                else collectionRepository.importDatabase(uri)
            _uiDialogState.value = QuoteCollectionDialogUiState.None
            when (result) {
                is AsyncResult.Success -> {
                    _uiEvent.emit(SnackBarEvent(
                        message = resourceProvider.getString(
                            if (type == LocalBackupType.CSV) R.string.csv_imported
                            else R.string.database_imported)
                    ))
                }
                is AsyncResult.Error -> {
                    _uiEvent.emit(SnackBarEvent(message = result.failedMessage))
                }
                is AsyncResult.Loading -> {}
            }
        }
    }

    fun getGoogleAccountSignInIntent(): Intent = googleAccountManager.getSignInIntent()

    fun handleSignInResult(result: Intent?) = viewModelScope.launch {
        val account = googleAccountManager.handleSignInResult(result)
        _uiMenuState.value = _uiMenuState.value.copy(googleAccount = account)
        if (account != null) {
            collectionRepository.updateDriveService()
            collectionRepository.queryDriveFileTimestamp()
            _uiEvent.emit(SnackBarEvent(
                message = resourceProvider.getString(R.string.google_account_connected))
            )
            if (account.email.isNotEmpty()) {
                syncAccountManager.addOrUpdateAccount(account.email)
            }
        } else {
            _uiEvent.emit(SnackBarEvent(
                message = resourceProvider.getString(R.string.google_account_sign_in_failed))
            )
        }
    }

    fun signOut() = viewModelScope.launch {
        _uiDialogState.value = QuoteCollectionDialogUiState.ProgressDialog(
            message = resourceProvider.getString(R.string.sign_out_google_account)
        )
        val result = googleAccountManager.signOutAccount()
        _uiDialogState.value = QuoteCollectionDialogUiState.None
        if (result.first) {
            _uiMenuState.value = _uiMenuState.value.copy(googleAccount = null)
            syncAccountManager.removeAccount(result.second)
        } else {
            _uiEvent.emit(SnackBarEvent(message = result.second))
        }
    }

    private fun ensureDriveService() = collectionRepository.ensureDriveService()

    fun gDriveBackup() {
        ensureDriveService()
        viewModelScope.launch {
            _uiDialogState.value = QuoteCollectionDialogUiState.ProgressDialog(
                message = resourceProvider.getString(R.string.start_google_drive_backup)
            )
            collectionRepository.gDriveBackup().collect {
                when (it) {
                    is AsyncResult.Success -> {
                        _uiDialogState.value = QuoteCollectionDialogUiState.None
                        _uiEvent.emit(SnackBarEvent(
                            message = resourceProvider.getString(R.string.remote_backup_completed)
                        ))
                    }
                    is AsyncResult.Error -> {
                        _uiDialogState.value = QuoteCollectionDialogUiState.None
                        _uiEvent.emit(SnackBarEvent(message = it.failedMessage))
                    }
                    is AsyncResult.Loading -> {
                        _uiDialogState.value =
                            QuoteCollectionDialogUiState.ProgressDialog(it.message)
                    }
                }
            }
        }
    }

    fun gDriveRestore() {
        ensureDriveService()
        viewModelScope.launch {
            _uiDialogState.value = QuoteCollectionDialogUiState.ProgressDialog(
                message = resourceProvider.getString(R.string.start_google_drive_restore)
            )
            collectionRepository.gDriveRestore().collect {
                when (it) {
                    is AsyncResult.Success -> {
                        _uiDialogState.value = QuoteCollectionDialogUiState.None
                        _uiEvent.emit(SnackBarEvent(
                            message = resourceProvider.getString(R.string.remote_restore_completed)
                        ))
                    }
                    is AsyncResult.Error -> {
                        _uiDialogState.value = QuoteCollectionDialogUiState.None
                        _uiEvent.emit(SnackBarEvent(message = it.failedMessage))
                    }
                    is AsyncResult.Loading -> {
                        _uiDialogState.value =
                            QuoteCollectionDialogUiState.ProgressDialog(it.message)
                    }
                }
            }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            collectionRepository.delete(id)
            _uiEvent.emit(SnackBarEvent(
                resourceProvider.getString(R.string.module_custom_deleted_quote))
            )
        }
    }

    fun snackBarShown() = viewModelScope.launch {
        _uiEvent.emit(emptySnackBarEvent)
    }

    companion object {
        private val DATE_FORMATTER = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    }
}