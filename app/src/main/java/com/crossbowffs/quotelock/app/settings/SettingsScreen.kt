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
import com.crossbowffs.quotelock.app.font.app.FontManagementActivity
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
    onCollectionItemClicked: () -> Unit,
    onHistoryItemClicked: () -> Unit,
) {
    val uiPreferenceState by viewModel.uiState
    val uiDialogState by viewModel.uiDialogState
    val uiEvent by viewModel.uiEvent.collectAsState(initial = null)
    SettingsScreen(
        modifier = modifier,
        uiState = uiPreferenceState,
        uiEvent = uiEvent,
        onDisplayOnAodChanged = { viewModel.switchDisplayOnAod(it) },
        onModuleProviderItemClicked = { viewModel.loadModuleProviders() },
        onRefreshIntervalItemClicked = { viewModel.loadRefreshInterval() },
        onUnmeteredOnlyChanged = { viewModel.switchUnmeteredOnly(it) },
        onQuoteSizeItemClicked = { viewModel.loadQuoteSize() },
        onSourceSizeItemClicked = { viewModel.loadSourceSize() },
        onQuoteStylesItemClicked = { viewModel.loadQuoteStyles() },
        onSourceStylesItemClicked = { viewModel.loadSourceStyles() },
        onFontFamilyItemClicked = { viewModel.loadFontFamily() },
        onQuoteSpacingItemClicked = { viewModel.loadQuoteSpacing() },
        onPaddingTopItemClicked = { viewModel.loadPaddingTop() },
        onPaddingBottomItemClicked = { viewModel.loadPaddingBottom() },
        onCollectionItemClicked = onCollectionItemClicked,
        onHistoryItemClicked = onHistoryItemClicked,
        onCreditsItemClicked = { viewModel.showCreditsDialog() },
        onRestartSystemUiItemClicked = { viewModel.restartSystemUi() },
    )
    SettingsDialogs(
        uiDialogState = uiDialogState,
        onModuleSelected = { viewModel.selectModule(it) },
        onRefreshIntervalSelected = { viewModel.selectRefreshInterval(it) },
        onQuoteSizeSelected = { viewModel.selectQuoteSize(it) },
        onSourceSizeSelected = { viewModel.selectSourceSize(it) },
        onQuoteStylesSelected = { viewModel.selectQuoteStyles(it) },
        onSourceStylesSelected = { viewModel.selectSourceStyles(it) },
        onFontFamilySelected = { viewModel.selectFontFamily(it) },
        onQuoteSpacingSelected = { viewModel.selectQuoteSpacing(it) },
        onPaddingTopSelected = { viewModel.selectPaddingTop(it) },
        onPaddingBottomSelected = { viewModel.selectPaddingBottom(it) },
        onDialogDismiss = { viewModel.cancelDialog() }
    )
}

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    uiState: SettingsUiState,
    uiEvent: SettingsUiEvent?,
    onDisplayOnAodChanged: (Boolean) -> Unit = {},
    onModuleProviderItemClicked: () -> Unit = {},
    onRefreshIntervalItemClicked: () -> Unit = {},
    onUnmeteredOnlyChanged: (Boolean) -> Unit = {},
    onQuoteSizeItemClicked: () -> Unit = {},
    onSourceSizeItemClicked: () -> Unit = {},
    onQuoteStylesItemClicked: () -> Unit = {},
    onSourceStylesItemClicked: () -> Unit = {},
    onFontFamilyItemClicked: () -> Unit = {},
    onQuoteSpacingItemClicked: () -> Unit = {},
    onPaddingTopItemClicked: () -> Unit = {},
    onPaddingBottomItemClicked: () -> Unit = {},
    onCollectionItemClicked: () -> Unit = {},
    onHistoryItemClicked: () -> Unit = {},
    onCreditsItemClicked: () -> Unit = {},
    onRestartSystemUiItemClicked: () -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
            PreferenceTitle(R.string.settings)
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
                enabled = uiState.moduleData?.configActivity != null
            ) {
                uiState.moduleData?.configActivity?.let {
                    with(context) { startActivity(Intent().apply { component = it }) }
                }
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
            PreferenceItem(
                titleRes = R.string.pref_font_size_text_title,
                summaryRes = R.string.pref_font_size_text_summary,
                onClick = onQuoteSizeItemClicked
            )
            PreferenceItem(
                titleRes = R.string.pref_font_size_source_title,
                summaryRes = R.string.pref_font_size_source_summary,
                onClick = onSourceSizeItemClicked
            )
            PreferenceItem(
                titleRes = R.string.pref_font_style_text_title,
                summaryRes = R.string.pref_font_style_text_summary,
                onClick = onQuoteStylesItemClicked
            )
            PreferenceItem(
                titleRes = R.string.pref_font_style_source_title,
                summaryRes = R.string.pref_font_style_source_summary,
                onClick = onSourceStylesItemClicked
            )
            PreferenceItem(
                titleRes = R.string.pref_font_family_title,
                summaryRes = R.string.pref_font_family_summary,
                enabled = uiState.enableFontFamily,
                onClick = onFontFamilyItemClicked
            )
            PreferenceItem(
                titleRes = R.string.pref_layout_quote_spacing_title,
                summaryRes = R.string.pref_layout_quote_spacing_summary,
                onClick = onQuoteSpacingItemClicked
            )
            PreferenceItem(
                titleRes = R.string.pref_layout_padding_top_title,
                summaryRes = R.string.pref_layout_padding_top_summary,
                onClick = onPaddingTopItemClicked
            )
            PreferenceItem(
                titleRes = R.string.pref_layout_padding_bottom_title,
                summaryRes = R.string.pref_layout_padding_bottom_summary,
                onClick = onPaddingBottomItemClicked
            )
            Divider()
            PreferenceTitle(R.string.features)
            PreferenceItem(
                titleRes = R.string.pref_collection_title,
                onClick = onCollectionItemClicked
            )
            PreferenceItem(
                titleRes = R.string.pref_history_title,
                onClick = onHistoryItemClicked
            )
            Divider()
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
    onQuoteSizeSelected: (Int) -> Unit,
    onSourceSizeSelected: (Int) -> Unit,
    onQuoteStylesSelected: (Set<String>?) -> Unit,
    onSourceStylesSelected: (Set<String>?) -> Unit,
    onFontFamilySelected: (String) -> Unit = {},
    onQuoteSpacingSelected: (Int) -> Unit,
    onPaddingTopSelected: (Int) -> Unit,
    onPaddingBottomSelected: (Int) -> Unit,
    onDialogDismiss: () -> Unit = {},
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
    is SettingsDialogUiState.QuoteSizeDialog -> {
        ListPreferenceDialog(
            title = stringResource(id = R.string.pref_font_size_text_title),
            entries = stringArrayResource(id = R.array.font_size_entries),
            entryValues = integerArrayResource(id = R.array.font_size_values).toTypedArray(),
            selectedItem = uiDialogState.currentSize,
            onItemSelected = onQuoteSizeSelected,
            onDismiss = onDialogDismiss
        )
    }
    is SettingsDialogUiState.SourceSizeDialog -> {
        ListPreferenceDialog(
            title = stringResource(id = R.string.pref_font_size_source_title),
            entries = stringArrayResource(id = R.array.font_size_entries),
            entryValues = integerArrayResource(id = R.array.font_size_values).toTypedArray(),
            selectedItem = uiDialogState.currentSize,
            onItemSelected = onSourceSizeSelected,
            onDismiss = onDialogDismiss
        )
    }
    is SettingsDialogUiState.QuoteStylesDialog -> {
        MultiSelectListPreferenceDialog(
            title = stringResource(id = R.string.pref_font_style_text_title),
            entries = stringArrayResource(id = R.array.font_style_entries),
            entryValues = stringArrayResource(id = R.array.font_style_values),
            selectedItems = uiDialogState.currentStyles,
            onItemsSelected = onQuoteStylesSelected,
            onDismiss = onDialogDismiss
        )
    }
    is SettingsDialogUiState.SourceStylesDialog -> {
        MultiSelectListPreferenceDialog(
            title = stringResource(id = R.string.pref_font_style_source_title),
            entries = stringArrayResource(id = R.array.font_style_entries),
            entryValues = stringArrayResource(id = R.array.font_style_values),
            selectedItems = uiDialogState.currentStyles,
            onItemsSelected = onSourceStylesSelected,
            onDismiss = onDialogDismiss
        )
    }
    is SettingsDialogUiState.FontFamilyDialog -> {
        val context = LocalContext.current
        FontListPreferenceDialog(
            title = stringResource(id = R.string.pref_font_style_source_title),
            entries = stringArrayResource(id = R.array.default_font_family_entries)
                    + (uiDialogState.fonts?.first ?: emptyArray()),
            entryValues = stringArrayResource(id = R.array.default_font_family_values)
                    + (uiDialogState.fonts?.second ?: emptyArray()),
            selectedItem = uiDialogState.currentFont,
            onItemSelected = onFontFamilySelected,
            onCustomize = {
                with(context) { startActivity(Intent(context, FontManagementActivity::class.java)) }
            },
            onDismiss = onDialogDismiss
        )
    }
    is SettingsDialogUiState.QuoteSpacingDialog -> {
        ListPreferenceDialog(
            title = stringResource(id = R.string.pref_layout_quote_spacing_title),
            entries = stringArrayResource(id = R.array.layout_quote_spacing_entries),
            entryValues = integerArrayResource(id = R.array.layout_quote_spacing_values).toTypedArray(),
            selectedItem = uiDialogState.spacing,
            onItemSelected = onQuoteSpacingSelected,
            onDismiss = onDialogDismiss
        )
    }
    is SettingsDialogUiState.PaddingTopDialog -> {
        ListPreferenceDialog(
            title = stringResource(id = R.string.pref_layout_padding_top_title),
            entries = stringArrayResource(id = R.array.layout_padding_entries),
            entryValues = integerArrayResource(id = R.array.layout_padding_values).toTypedArray(),
            selectedItem = uiDialogState.padding,
            onItemSelected = onPaddingTopSelected,
            onDismiss = onDialogDismiss
        )
    }
    is SettingsDialogUiState.PaddingBottomDialog -> {
        ListPreferenceDialog(
            title = stringResource(id = R.string.pref_layout_padding_bottom_title),
            entries = stringArrayResource(id = R.array.layout_padding_entries),
            entryValues = integerArrayResource(id = R.array.layout_padding_values).toTypedArray(),
            selectedItem = uiDialogState.padding,
            onItemSelected = onPaddingBottomSelected,
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
                    enableFontFamily = true,
                    unmeteredOnly = true,
                    moduleData = null,
                    updateInfo = ""),
                uiEvent = null,
            )
        }
    }
}