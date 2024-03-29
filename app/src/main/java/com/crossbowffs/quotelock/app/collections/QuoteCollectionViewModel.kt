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
import com.crossbowffs.quotelock.data.api.AndroidString
import com.crossbowffs.quotelock.data.api.GoogleAccount
import com.crossbowffs.quotelock.data.exceptionMessage
import com.crossbowffs.quotelock.data.modules.collections.QuoteCollectionRepository
import com.crossbowffs.quotelock.data.modules.collections.database.QuoteCollectionEntity
import com.yubyf.quotelockx.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
    data class ProgressDialog(val message: AndroidString? = null) : QuoteCollectionDialogUiState()

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
    val allItems: List<QuoteCollectionEntity> = emptyList(),
    val searchKeyword: String = "",
    val searchedItems: List<QuoteCollectionEntity> = emptyList(),
    val gDriveFirstSyncConflict: Boolean = false,
)

/**
 * @author Yubyf
 */
@HiltViewModel
class QuoteCollectionViewModel @Inject constructor(
    private val collectionRepository: QuoteCollectionRepository,
    private val googleAccountManager: GoogleAccountManager,
    private val syncAccountManager: SyncAccountManager,
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<SnackBarEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _uiDialogState =
        mutableStateOf<QuoteCollectionDialogUiState>(QuoteCollectionDialogUiState.None)
    val uiDialogState: State<QuoteCollectionDialogUiState> = _uiDialogState

    private val _uiMenuState = mutableStateOf(
        QuoteCollectionMenuUiState(
            syncEnabled = googleAccountManager.checkGooglePlayService(),
            googleAccount = googleAccountManager.getSignedInGoogleAccount(),
        )
    )
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
                _uiListState.value = _uiListState.value.copy(allItems = it)
                _uiMenuState.value = _uiMenuState.value.copy(exportEnabled = it.isNotEmpty())
            }.launchIn(this)
            if (googleAccountManager.checkGooglePlayService()) {
                collectionRepository.getGDriveFileTimestamp().onEach {
                    _uiMenuState.value = _uiMenuState.value.copy(
                        syncTime = if (it > 0) DATE_FORMATTER.format(Date(it)) else null
                    )
                }.launchIn(this)
                if (googleAccountManager.isGoogleAccountSignedIn()) {
                    val syncTime = collectionRepository.queryDriveFileTimestamp()
                    if (syncTime > 0) {
                        if (syncAccountManager.needFirstSync()) {
                            if (collectionRepository.count() > 0) {
                                _uiListState.value =
                                    _uiListState.value.copy(gDriveFirstSyncConflict = true)
                            } else {
                                syncAccountManager.performFirstSync(false)
                            }
                        }
                    }
                }
            }
        }
    }

    fun prepareSearch() {
        _uiListState.value =
            _uiListState.value.copy(searchKeyword = "", searchedItems = emptyList())
    }

    fun search(keyword: String) {
        if (keyword.trim() == _uiListState.value.searchKeyword.trim()) {
            if (keyword != _uiListState.value.searchKeyword) {
                _uiListState.value = _uiListState.value.copy(searchKeyword = keyword)
            }
            return
        }
        if (keyword.isBlank()) {
            _uiListState.value =
                _uiListState.value.copy(searchKeyword = "", searchedItems = emptyList())
            return
        }
        _uiListState.value = _uiListState.value.copy(searchKeyword = keyword)
        viewModelScope.launch {
            collectionRepository.search(keyword.trim()).collect {
                _uiListState.value = _uiListState.value.copy(searchedItems = it)
            }
        }
    }

    fun onPermissionDenied() {
        viewModelScope.launch {
            _uiEvent.emit(
                SnackBarEvent(
                    message = AndroidString.StringRes(R.string.grant_storage_permission_tips),
                    duration = SnackbarDuration.Short
                )
            )
        }
    }

    fun export(@LocalBackupType type: Int) {
        viewModelScope.launch {
            _uiDialogState.value = QuoteCollectionDialogUiState.ProgressDialog(
                message = AndroidString.StringRes(
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
                    _uiEvent.emit(
                        SnackBarEvent(
                            message = AndroidString.StringRes(
                                if (type == LocalBackupType.CSV) R.string.csv_exported
                                else R.string.database_exported,
                                arrayOf(result.data)
                            ),
                            duration = SnackbarDuration.Long,
                            actionText = AndroidString.StringRes(R.string.ok),
                        )
                    )
                }

                is AsyncResult.Error.Message -> {
                    _uiEvent.emit(SnackBarEvent(message = result.message))
                }

                is AsyncResult.Error.ExceptionWrapper -> {
                    _uiEvent.emit(SnackBarEvent(message = result.exceptionMessage?.let(AndroidString::StringText)))
                }

                is AsyncResult.Loading -> {}
            }
        }
    }

    fun import(@LocalBackupType type: Int, uri: Uri, merge: Boolean = false) {
        viewModelScope.launch {
            _uiDialogState.value = QuoteCollectionDialogUiState.ProgressDialog(
                message = AndroidString.StringRes(
                    if (type == LocalBackupType.CSV) R.string.importing_csv
                    else R.string.importing_database
                )
            )
            val result =
                if (type == LocalBackupType.CSV) collectionRepository.importCsv(uri, merge)
                else collectionRepository.importDatabase(uri, merge)
            _uiDialogState.value = QuoteCollectionDialogUiState.None
            when (result) {
                is AsyncResult.Success -> {
                    _uiEvent.emit(
                        SnackBarEvent(
                            message = AndroidString.StringRes(
                                if (type == LocalBackupType.CSV) R.string.csv_imported
                                else R.string.database_imported
                            )
                        )
                    )
                }

                is AsyncResult.Error.Message -> {
                    _uiEvent.emit(SnackBarEvent(message = result.message))
                }

                is AsyncResult.Error.ExceptionWrapper -> {
                    _uiEvent.emit(SnackBarEvent(message = result.exceptionMessage?.let(AndroidString::StringText)))
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
            val syncTime = collectionRepository.queryDriveFileTimestamp()
            _uiEvent.emit(
                SnackBarEvent(message = AndroidString.StringRes(R.string.google_account_connected))
            )
            if (account.email.isNotEmpty()) {
                syncAccountManager.addOrUpdateAccount(account.email, true)
                if (syncTime > 0) {
                    if (uiListState.value.allItems.isNotEmpty()) {
                        _uiListState.value = _uiListState.value.copy(gDriveFirstSyncConflict = true)
                    } else {
                        syncAccountManager.performFirstSync(false)
                    }
                }
            }
        } else {
            _uiEvent.emit(
                SnackBarEvent(
                    message = AndroidString.StringRes(R.string.google_account_sign_in_failed)
                )
            )
        }
    }

    fun gDriveFirstSync(merge: Boolean = false) = viewModelScope.launch {
        syncAccountManager.performFirstSync(merge)
        _uiListState.value =
            _uiListState.value.copy(gDriveFirstSyncConflict = false)
    }

    fun signOut() = viewModelScope.launch {
        _uiDialogState.value = QuoteCollectionDialogUiState.ProgressDialog(
            message = AndroidString.StringRes(R.string.sign_out_google_account)
        )
        val result = googleAccountManager.signOutAccount()
        _uiDialogState.value = QuoteCollectionDialogUiState.None
        if (result.first) {
            _uiMenuState.value = _uiMenuState.value.copy(googleAccount = null)
            syncAccountManager.removeAccount(result.second)
        } else {
            _uiEvent.emit(SnackBarEvent(message = AndroidString.StringText(result.second)))
        }
    }

    private fun ensureDriveService() = collectionRepository.ensureDriveService()

    fun gDriveBackup() {
        ensureDriveService()
        viewModelScope.launch {
            _uiDialogState.value = QuoteCollectionDialogUiState.ProgressDialog(
                message = AndroidString.StringRes(R.string.start_google_drive_backup)
            )
            collectionRepository.gDriveBackup().collect {
                when (it) {
                    is AsyncResult.Success -> {
                        _uiDialogState.value = QuoteCollectionDialogUiState.None
                        _uiEvent.emit(
                            SnackBarEvent(
                                message = AndroidString.StringRes(R.string.remote_backup_completed)
                            )
                        )
                    }

                    is AsyncResult.Error.Message -> {
                        _uiDialogState.value = QuoteCollectionDialogUiState.None
                        _uiEvent.emit(SnackBarEvent(message = it.message))
                    }

                    is AsyncResult.Error.ExceptionWrapper -> {
                        _uiDialogState.value = QuoteCollectionDialogUiState.None
                        _uiEvent.emit(SnackBarEvent(message = it.exceptionMessage?.let(AndroidString::StringText)))
                    }

                    is AsyncResult.Loading -> {
                        _uiDialogState.value = QuoteCollectionDialogUiState.ProgressDialog(
                            message = it.message
                        )
                    }
                }
            }
        }
    }

    fun gDriveRestore(merge: Boolean = false) {
        ensureDriveService()
        viewModelScope.launch {
            _uiDialogState.value = QuoteCollectionDialogUiState.ProgressDialog(
                message = AndroidString.StringRes(R.string.start_google_drive_restore)
            )
            collectionRepository.gDriveRestore(merge).collect {
                when (it) {
                    is AsyncResult.Success -> {
                        _uiDialogState.value = QuoteCollectionDialogUiState.None
                        _uiEvent.emit(
                            SnackBarEvent(
                                message = AndroidString.StringRes(R.string.remote_restore_completed)
                            )
                        )
                    }

                    is AsyncResult.Error.Message -> {
                        _uiDialogState.value = QuoteCollectionDialogUiState.None
                        _uiEvent.emit(SnackBarEvent(message = it.message))
                    }

                    is AsyncResult.Error.ExceptionWrapper -> {
                        _uiDialogState.value = QuoteCollectionDialogUiState.None
                        _uiEvent.emit(SnackBarEvent(message = it.exceptionMessage?.let(AndroidString::StringText)))
                    }

                    is AsyncResult.Loading -> {
                        _uiDialogState.value = QuoteCollectionDialogUiState.ProgressDialog(
                            message = it.message
                        )
                    }
                }
            }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            collectionRepository.delete(id)
            _uiEvent.emit(
                SnackBarEvent(AndroidString.StringRes(R.string.module_custom_deleted_quote))
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