@file:OptIn(ExperimentalMaterial3Api::class)

package com.crossbowffs.quotelock.app.settings

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.integerArrayResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.crossbowffs.quotelock.app.SnackBarEvent
import com.crossbowffs.quotelock.app.emptySnackBarEvent
import com.crossbowffs.quotelock.data.api.AndroidString
import com.crossbowffs.quotelock.data.api.contextString
import com.crossbowffs.quotelock.ui.components.ListPreferenceDialog
import com.crossbowffs.quotelock.ui.components.PreferenceItem
import com.crossbowffs.quotelock.ui.components.SettingsAppBar
import com.crossbowffs.quotelock.ui.components.SwitchablePreferenceItem
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import com.yubyf.quotelockx.R
import kotlinx.coroutines.launch

@Composable
fun SettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    onLanguageItemClicked: () -> Unit,
    onModuleConfigItemClicked: (String) -> Unit,
    onAboutItemClicked: () -> Unit,
    onBack: () -> Unit,
) {
    val uiPreferenceState by viewModel.uiState
    val uiDialogState by viewModel.uiDialogState
    val uiEvent by viewModel.uiEvent.collectAsState(initial = emptySnackBarEvent)

    SettingsScreen(
        modifier = modifier,
        uiState = uiPreferenceState,
        uiEvent = uiEvent,
        onLanguageItemClicked = onLanguageItemClicked,
        onDisplayOnAodChanged = viewModel::switchDisplayOnAod,
        onModuleProviderItemClicked = viewModel::loadModuleProviders,
        onModuleConfigItemClicked = onModuleConfigItemClicked,
        onRefreshIntervalItemClicked = viewModel::loadRefreshInterval,
        onUnmeteredOnlyChanged = viewModel::switchUnmeteredOnly,
        onRestartSystemUiItemClicked = viewModel::restartSystemUi,
        onClearCacheItemClicked = viewModel::clearCache,
        onAboutItemClicked = onAboutItemClicked,
        snackBarShown = viewModel::snackBarShown,
        onBack = onBack
    )
    SettingsDialogs(
        uiDialogState = uiDialogState,
        onModuleSelected = viewModel::selectModule,
        onRefreshIntervalSelected = viewModel::selectRefreshInterval,
        onDialogDismiss = viewModel::cancelDialog
    )
}

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    uiState: SettingsUiState,
    uiEvent: SnackBarEvent,
    onLanguageItemClicked: () -> Unit = {},
    onDisplayOnAodChanged: (Boolean) -> Unit = {},
    onModuleProviderItemClicked: () -> Unit = {},
    onModuleConfigItemClicked: (String) -> Unit = {},
    onRefreshIntervalItemClicked: () -> Unit = {},
    onUnmeteredOnlyChanged: (Boolean) -> Unit = {},
    onRestartSystemUiItemClicked: () -> Unit = {},
    onClearCacheItemClicked: () -> Unit = {},
    onAboutItemClicked: () -> Unit = {},
    snackBarShown: () -> Unit,
    onBack: () -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val topAppBarScrollState = rememberTopAppBarState()
    var scrollable by remember { mutableStateOf(false) }
    val scrollBehavior =
        TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarScrollState, { scrollable })
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = { SettingsAppBar(onBack = onBack, scrollBehavior = scrollBehavior) }
    ) { padding ->
        val context = LocalContext.current
        uiEvent.message?.let {
            val messageText = it
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = messageText.contextString(context),
                    duration = uiEvent.duration,
                    actionLabel = uiEvent.actionText.contextString(context)
                )
            }
            snackBarShown()
        }
        val scrollState = rememberScrollState()
        Column(modifier = modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(state = scrollState)
            .onGloballyPositioned {
                if (!scrollable && it.size.height > (it.parentCoordinates?.size?.height ?: 0)) {
                    scrollable = true
                }
            }
        ) {
            val currentLanguage = AppCompatDelegate.getApplicationLocales()[0]?.toLanguageTag()
            PreferenceItem(
                title = stringResource(id = R.string.pref_language_title),
                summary = stringArrayResource(id = R.array.lang_entries)
                    .zip(stringArrayResource(id = R.array.lang_values))
                    .find { it.second == currentLanguage }?.first
                    ?: stringResource(id = R.string.pref_language_system),
                onClick = onLanguageItemClicked
            )
            if (uiState.enableAod) {
                SwitchablePreferenceItem(
                    titleRes = R.string.pref_display_on_aod_title,
                    summaryRes = R.string.pref_display_on_aod_summary,
                    checked = uiState.displayOnAod,
                    onSwitchChange = onDisplayOnAodChanged
                )
            }
            PreferenceItem(
                titleRes = R.string.pref_quote_module_title,
                summaryRes = R.string.pref_quote_module_summary,
                onClick = onModuleProviderItemClicked
            )
            val providerConfigSupported = uiState.moduleData?.configRoute != null
            val providerSummary = if (providerConfigSupported)
                R.string.pref_module_preferences_summary
            else R.string.pref_module_preferences_summary_alt
            PreferenceItem(
                titleRes = R.string.pref_module_preferences_title,
                summaryRes = providerSummary,
                enabled = providerConfigSupported
            ) {
                uiState.moduleData?.configRoute?.let(onModuleConfigItemClicked::invoke)
            }
            // Set refresh interval override and disable preference if necessary.
            // This is kind of a lazy solution, but it's better than nothing.
            val refreshIntervalSupported = uiState.moduleData?.minimumRefreshInterval == 0
            val refreshIntervalSummary = if (refreshIntervalSupported)
                R.string.pref_refresh_interval_summary
            else R.string.pref_refresh_interval_summary_alt
            PreferenceItem(
                titleRes = R.string.pref_refresh_interval_title,
                summaryRes = refreshIntervalSummary,
                enabled = refreshIntervalSupported,
                onClick = onRefreshIntervalItemClicked
            )
            PreferenceItem(
                title = stringResource(R.string.pref_refresh_info_title),
                summary = uiState.updateInfo.contextString(context),
                enabled = false
            )
            // If the module doesn't require internet connectivity, disable the
            // unmetered only toggle and set the requires internet preference to false.
            val requiresInternet = uiState.moduleData?.requiresInternetConnectivity ?: false
            SwitchablePreferenceItem(
                titleRes = R.string.pref_unmetered_only_title,
                summaryRes = if (requiresInternet) R.string.pref_unmetered_only_summary
                else R.string.pref_unmetered_only_summary_alt,
                enabled = requiresInternet,
                checked = uiState.unmeteredOnly,
                onSwitchChange = onUnmeteredOnlyChanged
            )
            PreferenceItem(
                titleRes = R.string.pref_restart_system_ui_title,
                summaryRes = R.string.pref_debug_restart_system_ui_summary,
                onClick = onRestartSystemUiItemClicked
            )
            PreferenceItem(
                title = stringResource(id = R.string.pref_clear_cache),
                info = uiState.cacheSize,
                onClick = onClearCacheItemClicked
            )
            PreferenceItem(
                titleRes = R.string.about,
                onClick = onAboutItemClicked
            )
        }
    }
}

@Composable
fun SettingsDialogs(
    uiDialogState: SettingsDialogUiState,
    onModuleSelected: (String) -> Unit = {},
    onRefreshIntervalSelected: (Int) -> Unit,
    onDialogDismiss: () -> Unit,
) = when (uiDialogState) {
    is SettingsDialogUiState.ModuleProviderDialog -> {
        ListPreferenceDialog(
            title = stringResource(id = R.string.pref_quote_module_title),
            entries = uiDialogState.modules.first,
            entryValues = uiDialogState.modules.second,
            selectedItem = uiDialogState.currentModule,
            onItemSelected = { item ->
                item?.let { onModuleSelected(it) }
            },
            onDismiss = onDialogDismiss
        )
    }

    is SettingsDialogUiState.RefreshIntervalDialog -> {
        ListPreferenceDialog(
            title = stringResource(id = R.string.pref_refresh_interval_title),
            entries = stringArrayResource(id = R.array.refresh_interval_entries),
            entryValues = integerArrayResource(id = R.array.refresh_interval_values).toTypedArray(),
            selectedItem = uiDialogState.currentInterval,
            onItemSelected = onRefreshIntervalSelected,
            onDismiss = onDialogDismiss
        )
    }

    is SettingsDialogUiState.LanguageDialog -> {
        ListPreferenceDialog(
            title = stringResource(id = R.string.pref_language_title),
            entries = arrayOf(stringResource(id = R.string.pref_language_system))
                    + stringArrayResource(id = R.array.lang_entries),
            entryValues = arrayOf<String?>(null) + stringArrayResource(id = R.array.lang_values),
            selectedItem = uiDialogState.currentLanguage,
            onItemSelected = {
                val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(it)
                AppCompatDelegate.setApplicationLocales(appLocale)
            },
            onDismiss = onDialogDismiss
        )
    }

    is SettingsDialogUiState.None -> {}
}

@Preview(
    name = "Settings Screen Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Settings Screen Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SettingsScreenPreview() {
    QuoteLockTheme {
        Surface {
            SettingsScreen(
                uiState = SettingsUiState(
                    enableAod = true,
                    displayOnAod = true,
                    unmeteredOnly = true,
                    moduleData = null,
                    updateInfo = AndroidString.StringText(""),
                    cacheSize = "0.0 MB"
                ),
                uiEvent = emptySnackBarEvent,
                snackBarShown = {},
            )
        }
    }
}