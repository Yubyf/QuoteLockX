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
import com.crossbowffs.quotelock.data.AsyncResult
import com.crossbowffs.quotelock.data.api.GoogleAccount
import com.crossbowffs.quotelock.data.failedMessage
import com.crossbowffs.quotelock.data.modules.collections.QuoteCollectionRepository
import com.crossbowffs.quotelock.data.modules.collections.database.QuoteCollectionEntity
import com.crossbowffs.quotelock.di.ResourceProvider
import com.yubyf.quotelockx.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
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
        val duration: SnackbarDuration = SnackbarDuration.Short,
        val actionText: String? = null,
    ) : QuoteCollectionUiEvent()

    data class ProgressMessage(
        val show: Boolean = false,
        override val message: String? = null,
    ) : QuoteCollectionUiEvent()
}

/**
 * UI state for the quote collection menu.
 */
data class QuoteCollectionMenuUiState(
    val exportEnabled: Boolean = false,
    val syncEnabled: Boolean = false,
    val googleAccount: GoogleAccount? = null,
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

    private val _uiEvent = MutableSharedFlow<QuoteCollectionUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _uiMenuState = mutableStateOf(QuoteCollectionMenuUiState(
        syncEnabled = googleAccountManager.checkGooglePlayService(),
        googleAccount = googleAccountManager.getSignedInGoogleAccount(),
    ))
    val uiMenuState: State<QuoteCollectionMenuUiState>
        get() = _uiMenuState

    private val _uiListState = mutableStateOf(QuoteCollectionListUiState())
    val uiListState: State<QuoteCollectionListUiState>
        get() = _uiListState

    init {
        viewModelScope.run {
            launch {
                collectionRepository.getAllStream().stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList()
                ).collect {
                    _uiListState.value = _uiListState.value.copy(items = it)
                    _uiMenuState.value = _uiMenuState.value.copy(
                        exportEnabled = it.isNotEmpty()
                    )
                }
            }
        }
    }

    fun onPermissionDenied() {
        viewModelScope.launch {
            _uiEvent.emit(
                QuoteCollectionUiEvent.SnackBarMessage(
                    message = resourceProvider.getString(R.string.grant_storage_permission_tips),
                    duration = SnackbarDuration.Short
                )
            )
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
                        duration = SnackbarDuration.Long,
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

    fun getGoogleAccountSignInIntent(): Intent = googleAccountManager.getSignInIntent()

    fun handleSignInResult(result: Intent?) = viewModelScope.launch {
        val account = googleAccountManager.handleSignInResult(result)
        _uiMenuState.value = _uiMenuState.value.copy(googleAccount = account)
        if (account != null) {
            collectionRepository.updateDriveService()
            _uiEvent.emit(QuoteCollectionUiEvent.SnackBarMessage(
                message = resourceProvider.getString(R.string.google_account_connected))
            )
            if (account.email.isNotEmpty()) {
                syncAccountManager.addOrUpdateAccount(account.email)
            }
        } else {
            _uiEvent.emit(QuoteCollectionUiEvent.SnackBarMessage(
                message = resourceProvider.getString(R.string.google_account_sign_in_failed))
            )
        }
    }

    fun signOut() = viewModelScope.launch {
        _uiEvent.emit(QuoteCollectionUiEvent.ProgressMessage(
            show = true,
            message = resourceProvider.getString(R.string.sign_out_google_account))
        )
        val result = googleAccountManager.signOutAccount()
        _uiEvent.emit(QuoteCollectionUiEvent.ProgressMessage(show = false))
        if (result.first) {
            _uiMenuState.value = _uiMenuState.value.copy(googleAccount = null)
            if (result.first) {
                syncAccountManager.removeAccount(result.second)
            }
        } else {
            _uiEvent.emit(QuoteCollectionUiEvent.SnackBarMessage(message = result.second))
        }
    }

    private fun ensureDriveService() = collectionRepository.ensureDriveService()

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