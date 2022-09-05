@file:OptIn(ExperimentalMaterial3Api::class)

package com.crossbowffs.quotelock.app.settings

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.integerArrayResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import androidx.core.text.HtmlCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.crossbowffs.quotelock.consts.Urls
import com.crossbowffs.quotelock.ui.components.*
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import com.yubyf.quotelockx.BuildConfig
import com.yubyf.quotelockx.R
import kotlinx.coroutines.launch

@Composable
fun SettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    onModuleConfigItemClicked: (String) -> Unit,
    onBack: () -> Unit,
) {
    val uiPreferenceState by viewModel.uiState
    val uiDialogState by viewModel.uiDialogState
    val uiEvent by viewModel.uiEvent.collectAsState(initial = null)

    SettingsScreen(
        modifier = modifier,
        uiState = uiPreferenceState,
        uiEvent = uiEvent,
        onDisplayOnAodChanged = viewModel::switchDisplayOnAod,
        onModuleProviderItemClicked = viewModel::loadModuleProviders,
        onModuleConfigItemClicked = onModuleConfigItemClicked,
        onRefreshIntervalItemClicked = viewModel::loadRefreshInterval,
        onUnmeteredOnlyChanged = viewModel::switchUnmeteredOnly,
        onCreditsItemClicked = viewModel::showCreditsDialog,
        onRestartSystemUiItemClicked = viewModel::restartSystemUi,
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
    uiEvent: SettingsUiEvent?,
    onDisplayOnAodChanged: (Boolean) -> Unit = {},
    onModuleProviderItemClicked: () -> Unit = {},
    onModuleConfigItemClicked: (String) -> Unit = {},
    onRefreshIntervalItemClicked: () -> Unit = {},
    onUnmeteredOnlyChanged: (Boolean) -> Unit = {},
    onCreditsItemClicked: () -> Unit = {},
    onRestartSystemUiItemClicked: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = { SettingsAppBar(onBack = onBack) }
    ) { padding ->
        when (uiEvent) {
            is SettingsUiEvent.SnackBarMessage -> {
                uiEvent.message?.let {
                    val messageText = it
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = messageText,
                            duration = uiEvent.duration,
                            actionLabel = uiEvent.actionText
                        )
                    }
                }
            }
            null -> {}
        }
        var versionTapCount by remember { mutableStateOf(0) }
        val scrollState = rememberScrollState()
        Column(modifier = modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(state = scrollState)
        ) {
            val context = LocalContext.current
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
            PreferenceItem(
                titleRes = R.string.pref_module_preferences_title,
                summaryRes = R.string.pref_module_preferences_summary,
                enabled = uiState.moduleData?.configRoute != null
            ) {
                uiState.moduleData?.configRoute?.let(onModuleConfigItemClicked::invoke)
            }
            // Set refresh interval override and disable preference if necessary.
            // This is kind of a lazy solution, but it's better than nothing.
            PreferenceItem(
                titleRes = R.string.pref_refresh_interval_title,
                summaryRes = R.string.pref_refresh_interval_summary,
                onClick = onRefreshIntervalItemClicked
            )
            PreferenceItem(
                title = stringResource(R.string.pref_refresh_info_title),
                summary = uiState.updateInfo,
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
            PreferenceTitle(R.string.about)
            PreferenceItem(
                titleRes = R.string.pref_about_credits_title,
                summaryRes = R.string.pref_about_credits_summary,
                onClick = onCreditsItemClicked
            )
            PreferenceItem(
                title = stringResource(id = R.string.pref_about_github),
                summary = "github.com/Yubyf/QuoteLockX",
                onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse(Urls.GITHUB_QUOTELOCK)))
                }
            )
            PreferenceItem(
                titleRes = R.string.pref_about_restart_system_ui_title,
                summaryRes = R.string.pref_debug_restart_system_ui_summary,
                onClick = onRestartSystemUiItemClicked
            )
            val easterMessage = stringResource(id = R.string.easter_egg)
            PreferenceItem(
                title = stringResource(id = R.string.pref_about_version),
                summary = "${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})",
                onClick = {
                    versionTapCount++
                    if (versionTapCount == 7) {
                        versionTapCount = 0
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = easterMessage,
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                }
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
                item?.let { it -> onModuleSelected(it) }
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
    SettingsDialogUiState.CreditsDialog -> {
        AlertDialog(
            onDismissRequest = onDialogDismiss,
            confirmButton = {
                TextButton(onClick = onDialogDismiss) {
                    Text(text = stringResource(id = R.string.close))
                }
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            ),
            title = { Text(text = stringResource(id = R.string.credits_title)) },
            text = {
                // TODO: Replace TextView with Text() with AnnotatedString.
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = { context ->
                        TextView(context).apply {
                            text = HtmlCompat.fromHtml(context.getString(R.string.credits_message),
                                HtmlCompat.FROM_HTML_MODE_COMPACT)
                            movementMethod = LinkMovementMethod.getInstance()
                        }
                    })
            }
        )
    }
    is SettingsDialogUiState.None -> {}
}

@Preview(name = "Settings Screen Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Settings Screen Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsScreenPreview() {
    QuoteLockTheme {
        Surface {
            SettingsScreen(
                uiState = SettingsUiState(enableAod = true,
                    displayOnAod = true,
                    unmeteredOnly = true,
                    moduleData = null,
                    updateInfo = ""),
                uiEvent = null,
            )
        }
    }
}