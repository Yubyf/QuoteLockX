package com.crossbowffs.quotelock.app.settings

import android.content.Context
import android.os.Build
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crossbowffs.quotelock.app.font.FontManager
import com.crossbowffs.quotelock.consts.*
import com.crossbowffs.quotelock.data.ConfigurationRepository
import com.crossbowffs.quotelock.data.api.QuoteModule
import com.crossbowffs.quotelock.data.api.QuoteModuleData
import com.crossbowffs.quotelock.data.modules.ModuleNotFoundException
import com.crossbowffs.quotelock.data.modules.Modules
import com.crossbowffs.quotelock.data.modules.QuoteRepository
import com.crossbowffs.quotelock.di.ResourceProvider
import com.crossbowffs.quotelock.utils.WorkUtils
import com.crossbowffs.quotelock.utils.XposedUtils
import com.crossbowffs.quotelock.utils.findProcessAndKill
import com.crossbowffs.quotelock.xposed.LockscreenHook
import com.yubyf.quotelockx.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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
        val duration: SnackbarDuration = SnackbarDuration.Short,
        val actionText: String? = null,
    ) : SettingsUiEvent()
}

/**
 * UI state for the settings screen.
 */
data class SettingsUiState(
    val enableAod: Boolean,
    val displayOnAod: Boolean,
    val enableFontFamily: Boolean,
    val unmeteredOnly: Boolean,
    val moduleData: QuoteModuleData?,
    val updateInfo: String,
)

/**
 * UI state for the settings screen.
 */
sealed class SettingsDialogUiState {
    data class ModuleProviderDialog(
        val modules: Pair<Array<String>, Array<String?>>,
        val currentModule: String,
    ) : SettingsDialogUiState()

    data class RefreshIntervalDialog(
        val currentInterval: Int,
    ) : SettingsDialogUiState()

    data class QuoteSizeDialog(
        val currentSize: Int,
    ) : SettingsDialogUiState()

    data class SourceSizeDialog(
        val currentSize: Int,
    ) : SettingsDialogUiState()

    data class QuoteStylesDialog(
        val currentStyles: Set<String>?,
    ) : SettingsDialogUiState()

    data class SourceStylesDialog(
        val currentStyles: Set<String>?,
    ) : SettingsDialogUiState()

    data class FontFamilyDialog(
        val fonts: Pair<Array<String>, Array<String>>?,
        val currentFont: String,
    ) : SettingsDialogUiState()

    data class QuoteSpacingDialog(
        val spacing: Int,
    ) : SettingsDialogUiState()

    data class PaddingTopDialog(
        val padding: Int,
    ) : SettingsDialogUiState()

    data class PaddingBottomDialog(
        val padding: Int,
    ) : SettingsDialogUiState()

    object CreditsDialog : SettingsDialogUiState()

    object None : SettingsDialogUiState()
}

/**
 * @author Yubyf
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val configurationRepository: ConfigurationRepository,
    private val quoteRepository: QuoteRepository,
    private val resourceProvider: ResourceProvider,
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<SettingsUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _uiState = mutableStateOf(
        SettingsUiState(
            // Only enable DisplayOnAOD on tested devices.
            XposedUtils.isAodHookAvailable,
            configurationRepository.displayOnAod,
            // Only enable font family above API26
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O,
            configurationRepository.isUnmeteredNetworkOnly,
            null,
            resourceProvider.getString(
                R.string.pref_refresh_info_summary, "-"))
    )
    val uiState: State<SettingsUiState> = _uiState

    private val _uiDialogState = mutableStateOf<SettingsDialogUiState>(SettingsDialogUiState.None)
    val uiDialogState: State<SettingsDialogUiState> = _uiDialogState

    init {
        viewModelScope.run {
            launch {
                configurationRepository.observeConfigurationDataStore { preferences, key ->
                    when (key?.name) {
                        PREF_COMMON_QUOTE_MODULE -> onSelectedModuleChanged()
                        PREF_COMMON_REFRESH_RATE, PREF_COMMON_REFRESH_RATE_OVERRIDE ->
                            WorkUtils.createQuoteDownloadWork(context, true)
                        PREF_COMMON_DISPLAY_ON_AOD -> {
                            _uiState.value = _uiState.value.copy(
                                displayOnAod = preferences[booleanPreferencesKey(
                                    PREF_COMMON_DISPLAY_ON_AOD)] ?: false
                            )
                        }
                        PREF_COMMON_UNMETERED_ONLY -> {
                            _uiState.value = _uiState.value.copy(
                                unmeteredOnly = preferences[booleanPreferencesKey(
                                    PREF_COMMON_UNMETERED_ONLY)]
                                    ?: PREF_COMMON_UNMETERED_ONLY_DEFAULT
                            )
                        }
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
                        _uiState.value = _uiState.value.copy(
                            updateInfo = resourceProvider.getString(
                                R.string.pref_refresh_info_summary,
                                if (it > 0) DATE_FORMATTER.format(Date(it)) else "-")
                        )
                    }
                }
            }

            val updateTime = quoteRepository.getLastUpdateTime()
            _uiState.value = _uiState.value.copy(
                updateInfo = resourceProvider.getString(
                    R.string.pref_refresh_info_summary,
                    if (updateTime > 0) DATE_FORMATTER.format(Date(updateTime)) else "-")
            )
            launch {
                onSelectedModuleChanged()
            }
        }
    }

    fun switchDisplayOnAod(checked: Boolean) {
        configurationRepository.displayOnAod = checked
    }

    fun getModuleList(): Pair<Array<String>, Array<String?>> {
        // Get quote module list
        val quoteModules = quoteRepository.getAllModules()
        return quoteModules.map { module ->
            val moduleData = quoteRepository.getQuoteModuleData(module)
            moduleData.displayName to module::class.qualifiedName
        }.unzip().let {
            it.first.toTypedArray() to it.second.toTypedArray()
        }
    }

    fun loadModuleProviders() {
        _uiDialogState.value =
            SettingsDialogUiState.ModuleProviderDialog(getModuleList(),
                configurationRepository.currentModuleName)
    }

    fun selectModule(moduleName: String) {
        configurationRepository.currentModuleName = moduleName
    }

    fun loadRefreshInterval() {
        _uiDialogState.value =
            SettingsDialogUiState.RefreshIntervalDialog(configurationRepository.refreshInterval)
    }

    fun switchUnmeteredOnly(checked: Boolean) {
        configurationRepository.isUnmeteredNetworkOnly = checked
    }

    fun selectRefreshInterval(interval: Int) {
        configurationRepository.refreshInterval = interval
    }

    fun loadQuoteSize() {
        _uiDialogState.value =
            SettingsDialogUiState.QuoteSizeDialog(configurationRepository.quoteSize)
    }

    fun selectQuoteSize(size: Int) {
        configurationRepository.quoteSize = size
    }

    fun loadSourceSize() {
        _uiDialogState.value =
            SettingsDialogUiState.SourceSizeDialog(configurationRepository.sourceSize)
    }

    fun selectSourceSize(size: Int) {
        configurationRepository.sourceSize = size
    }

    fun loadQuoteStyles() {
        _uiDialogState.value =
            SettingsDialogUiState.QuoteStylesDialog(configurationRepository.quoteStyles)
    }

    fun selectQuoteStyles(styles: Set<String>?) {
        configurationRepository.quoteStyles = styles
    }

    fun loadSourceStyles() {
        _uiDialogState.value =
            SettingsDialogUiState.SourceStylesDialog(configurationRepository.sourceStyles)
    }

    fun selectSourceStyles(styles: Set<String>?) {
        configurationRepository.sourceStyles = styles
    }

    fun loadFontFamily() {
        val fonts = FontManager.loadActiveFontFilesList()?.map { file ->
            file.nameWithoutExtension to file.path
        }?.unzip()?.let {
            it.first.toTypedArray() to it.second.toTypedArray()
        }
        _uiDialogState.value =
            SettingsDialogUiState.FontFamilyDialog(fonts = fonts,
                currentFont = configurationRepository.fontFamily)
    }

    fun selectFontFamily(fontFamily: String) {
        configurationRepository.fontFamily = fontFamily
    }

    fun loadQuoteSpacing() {
        _uiDialogState.value =
            SettingsDialogUiState.QuoteSpacingDialog(configurationRepository.quoteSpacing)
    }

    fun selectQuoteSpacing(spacing: Int) {
        configurationRepository.quoteSpacing = spacing
    }

    fun loadPaddingTop() {
        _uiDialogState.value =
            SettingsDialogUiState.PaddingTopDialog(configurationRepository.paddingTop)
    }

    fun selectPaddingTop(padding: Int) {
        configurationRepository.paddingTop = padding
    }

    fun loadPaddingBottom() {
        _uiDialogState.value =
            SettingsDialogUiState.PaddingBottomDialog(configurationRepository.paddingBottom)
    }

    fun selectPaddingBottom(padding: Int) {
        configurationRepository.paddingBottom = padding
    }

    fun showCreditsDialog() {
        _uiDialogState.value = SettingsDialogUiState.CreditsDialog
    }

    fun restartSystemUi() {
        findProcessAndKill(LockscreenHook.PACKAGE_SYSTEM_UI)
    }

    fun cancelDialog() {
        _uiDialogState.value = SettingsDialogUiState.None
    }

    private suspend fun onSelectedModuleChanged() {
        val module = loadSelectedModule()

        val quoteModuleData = quoteRepository.getQuoteModuleData(module)
        if (quoteModuleData == _uiState.value.moduleData) {
            return
        }
        _uiState.value = _uiState.value.copy(moduleData = quoteModuleData)
        configurationRepository.updateConfiguration(quoteModuleData)
        // Update quotes.
        quoteRepository.downloadQuote()
    }

    private suspend fun loadSelectedModule(): QuoteModule {
        val moduleClsName = configurationRepository.currentModuleName
        return try {
            Modules[moduleClsName]
        } catch (e: ModuleNotFoundException) {
            // Reset to the default module if the currently
            // selected one was not found. Change through the
            // ListPreference so that it updates its value.
            selectModule(PREF_COMMON_QUOTE_MODULE_DEFAULT)
            _uiEvent.emit(SettingsUiEvent.SnackBarMessage(
                message = resourceProvider.getString(R.string.selected_module_not_found)))
            loadSelectedModule()
        }
    }

    companion object {
        private val DATE_FORMATTER = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    }
}