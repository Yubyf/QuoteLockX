package com.crossbowffs.quotelock.app.settings

import androidx.datastore.preferences.core.longPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crossbowffs.quotelock.consts.*
import com.crossbowffs.quotelock.data.ConfigurationRepository
import com.crossbowffs.quotelock.data.api.QuoteModule
import com.crossbowffs.quotelock.data.api.QuoteModuleData
import com.crossbowffs.quotelock.data.modules.ModuleNotFoundException
import com.crossbowffs.quotelock.data.modules.Modules
import com.crossbowffs.quotelock.data.modules.QuoteRepository
import com.crossbowffs.quotelock.di.ResourceProvider
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.yubyf.quotelockx.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * UI event for the settings screen.
 */
sealed class SettingsUiEvent {
    data class SnackBarMessage(
        val message: String? = null,
        @BaseTransientBottomBar.Duration val duration: Int = Snackbar.LENGTH_SHORT,
        val actionText: String? = null,
    ) : SettingsUiEvent()

    data class ProgressMessage(
        val show: Boolean = false,
        val message: String? = null,
    ) : SettingsUiEvent()

    data class SelectModule(val module: String) : SettingsUiEvent()

    object StartWorker : SettingsUiEvent()
}

/**
 * UI state for the settings screen.
 */
data class SettingsUiState(val moduleData: QuoteModuleData?, val updateInfo: String)

/**
 * @author Yubyf
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val configurationRepository: ConfigurationRepository,
    private val quoteRepository: QuoteRepository,
    private val resourceProvider: ResourceProvider,
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<SettingsUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _uiState: MutableStateFlow<SettingsUiState> =
        MutableStateFlow(SettingsUiState(null, resourceProvider.getString(
            R.string.pref_refresh_info_summary, "-")))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.run {
            launch {
                configurationRepository.observeConfigurationDataStore { _, key ->
                    when (key?.name) {
                        PREF_COMMON_QUOTE_MODULE -> onSelectedModuleChanged()
                        PREF_COMMON_REFRESH_RATE, PREF_COMMON_REFRESH_RATE_OVERRIDE ->
                            _uiEvent.emit(SettingsUiEvent.StartWorker)
                        else -> {}
                    }
                }
            }
            launch {
                quoteRepository.observeQuoteData { preferences, key ->
                    if (key?.name != PREF_QUOTES_LAST_UPDATED) {
                        return@observeQuoteData
                    }
                    preferences[longPreferencesKey(PREF_QUOTES_LAST_UPDATED)]?.let {
                        _uiState.update { currentState ->
                            currentState.copy(updateInfo = resourceProvider.getString(
                                R.string.pref_refresh_info_summary,
                                if (it > 0) DATE_FORMATTER.format(Date(it)) else "-"))
                        }
                    }
                }
            }
        }
    }

    fun getModuleList(): Pair<Array<String?>, Array<String?>> {
        // Get quote module list
        val quoteModules = quoteRepository.getAllModules()
        val moduleNames = arrayOfNulls<String>(quoteModules.size)
        val moduleClsNames = arrayOfNulls<String>(quoteModules.size)
        moduleNames.indices.forEach { i ->
            val module = quoteModules[i]
            val moduleData = quoteRepository.getQuoteModuleData(module)
            moduleNames[i] = moduleData.displayName
            moduleClsNames[i] = module::class.qualifiedName
        }
        return Pair(moduleNames, moduleClsNames)
    }

    fun refreshSelectedModule() = viewModelScope.launch { onSelectedModuleChanged() }

    private suspend fun onSelectedModuleChanged() {
        val module = loadSelectedModule()

        val quoteModuleData = quoteRepository.getQuoteModuleData(module)
        _uiState.update { currentState ->
            currentState.copy(moduleData = quoteModuleData)
        }
        configurationRepository.updateConfiguration(quoteModuleData)
        // Update quotes.
        quoteRepository.downloadQuote()
    }

    private suspend fun loadSelectedModule(): QuoteModule {
        val moduleClsName = configurationRepository.getCurrentModuleName()
        return try {
            Modules[moduleClsName]
        } catch (e: ModuleNotFoundException) {
            // Reset to the default module if the currently
            // selected one was not found. Change through the
            // ListPreference so that it updates its value.
            _uiEvent.emit(SettingsUiEvent.SelectModule(PREF_COMMON_QUOTE_MODULE_DEFAULT))
            _uiEvent.emit(SettingsUiEvent.SnackBarMessage(
                message = resourceProvider.getString(R.string.selected_module_not_found)))
            loadSelectedModule()
        }
    }

    companion object {
        private val DATE_FORMATTER = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    }
}