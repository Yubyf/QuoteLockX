@file:OptIn(ExperimentalMaterial3Api::class)

package com.crossbowffs.quotelock.app.lockscreen.styles

import android.content.res.Configuration
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.integerArrayResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.crossbowffs.quotelock.app.font.FontInfo
import com.crossbowffs.quotelock.data.api.QuoteDataWithCollectState
import com.crossbowffs.quotelock.data.api.TextFontStyle
import com.crossbowffs.quotelock.ui.components.FontListPreferenceDialog
import com.crossbowffs.quotelock.ui.components.FontStylePreferenceDialog
import com.crossbowffs.quotelock.ui.components.ListPreferenceDialog
import com.crossbowffs.quotelock.ui.components.LockscreenStylesAppBar
import com.crossbowffs.quotelock.ui.components.PreferenceItem
import com.crossbowffs.quotelock.ui.components.PreferenceTitle
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import com.yubyf.quotelockx.R

@Composable
fun LockscreenStylesRoute(
    modifier: Modifier = Modifier,
    viewModel: LockscreenStylesViewModel = hiltViewModel(),
    onPreviewClick: (QuoteDataWithCollectState) -> Unit,
    onFontCustomize: () -> Unit,
    onBack: () -> Unit,
) {
    val uiPreferenceState by viewModel.uiState
    val uiDialogState by viewModel.uiDialogState
    val topAppBarScrollState = rememberTopAppBarState()
    var scrollable by remember { mutableStateOf(false) }
    val scrollBehavior =
        TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarScrollState, { scrollable })

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { LockscreenStylesAppBar(onBack, scrollBehavior) }) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            PreviewRoute(onPreviewClick = onPreviewClick)
            LockscreenStylesScreen(
                modifier = modifier,
                uiState = uiPreferenceState,
                onScrollable = { scrollable = true },
                onQuoteSizeItemClicked = viewModel::loadQuoteSize,
                onSourceSizeItemClicked = viewModel::loadSourceSize,
                onQuoteStylesItemClicked = viewModel::loadQuoteStyle,
                onSourceStylesItemClicked = viewModel::loadSourceStyles,
                onFontFamilyItemClicked = viewModel::loadFontFamily,
                onQuoteSpacingItemClicked = viewModel::loadQuoteSpacing,
                onPaddingTopItemClicked = viewModel::loadPaddingTop,
                onPaddingBottomItemClicked = viewModel::loadPaddingBottom,
            )
            LockscreenStylesDialogs(
                uiDialogState = uiDialogState,
                onQuoteSizeSelected = viewModel::selectQuoteSize,
                onSourceSizeSelected = viewModel::selectSourceSize,
                onQuoteStyleSelected = viewModel::selectQuoteStyle,
                onSourceStyleSelected = viewModel::selectSourceStyles,
                onFontFamilySelected = viewModel::selectFontFamily,
                onFontCustomize = onFontCustomize,
                onQuoteSpacingSelected = viewModel::selectQuoteSpacing,
                onPaddingTopSelected = viewModel::selectPaddingTop,
                onPaddingBottomSelected = viewModel::selectPaddingBottom,
                onDialogDismiss = viewModel::cancelDialog
            )
        }
    }
}

@Composable
fun LockscreenStylesScreen(
    modifier: Modifier = Modifier,
    uiState: LockscreenStylesUiState,
    nestedScrollConnection: NestedScrollConnection? = null,
    onScrollable: () -> Unit = {},
    onQuoteSizeItemClicked: () -> Unit = {},
    onSourceSizeItemClicked: () -> Unit = {},
    onQuoteStylesItemClicked: () -> Unit = {},
    onSourceStylesItemClicked: () -> Unit = {},
    onFontFamilyItemClicked: () -> Unit = {},
    onQuoteSpacingItemClicked: () -> Unit = {},
    onPaddingTopItemClicked: () -> Unit = {},
    onPaddingBottomItemClicked: () -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        modifier = Modifier.apply { nestedScrollConnection?.let { nestedScroll(it) } },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        var scrollable by remember { mutableStateOf(false) }
        val scrollState = rememberScrollState()
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            PreferenceTitle(R.string.style)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(state = scrollState)
                    .onGloballyPositioned {
                        if (!scrollable
                            && it.size.height > (it.parentCoordinates?.size?.height ?: 0)
                        ) {
                            scrollable = true
                            onScrollable()
                        }
                    }
            ) {
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
            }
        }
    }
}

@Composable
fun LockscreenStylesDialogs(
    uiDialogState: LockscreenStylesDialogUiState,
    onQuoteSizeSelected: (Int) -> Unit,
    onSourceSizeSelected: (Int) -> Unit,
    onQuoteStyleSelected: (TextFontStyle) -> Unit,
    onSourceStyleSelected: (TextFontStyle) -> Unit,
    onFontFamilySelected: (FontInfo) -> Unit,
    onFontCustomize: () -> Unit,
    onQuoteSpacingSelected: (Int) -> Unit,
    onPaddingTopSelected: (Int) -> Unit,
    onPaddingBottomSelected: (Int) -> Unit,
    onDialogDismiss: () -> Unit,
) = when (uiDialogState) {
    is LockscreenStylesDialogUiState.QuoteSizeDialog -> {
        ListPreferenceDialog(
            title = stringResource(id = R.string.pref_font_size_text_title),
            entries = stringArrayResource(id = R.array.font_size_entries),
            entryValues = integerArrayResource(id = R.array.font_size_values).toTypedArray(),
            selectedItem = uiDialogState.currentSize,
            onItemSelected = onQuoteSizeSelected,
            onDismiss = onDialogDismiss
        )
    }

    is LockscreenStylesDialogUiState.SourceSizeDialog -> {
        ListPreferenceDialog(
            title = stringResource(id = R.string.pref_font_size_source_title),
            entries = stringArrayResource(id = R.array.font_size_entries),
            entryValues = integerArrayResource(id = R.array.font_size_values).toTypedArray(),
            selectedItem = uiDialogState.currentSize,
            onItemSelected = onSourceSizeSelected,
            onDismiss = onDialogDismiss
        )
    }

    is LockscreenStylesDialogUiState.QuoteStylesDialog -> {
        FontStylePreferenceDialog(
            title = stringResource(id = R.string.pref_font_style_text_title),
            style = uiDialogState.currentStyle,
            onStyleChange = onQuoteStyleSelected,
            onDismiss = onDialogDismiss
        )
    }

    is LockscreenStylesDialogUiState.SourceStylesDialog -> {
        FontStylePreferenceDialog(
            title = stringResource(id = R.string.pref_font_style_source_title),
            style = uiDialogState.currentStyle,
            onStyleChange = onSourceStyleSelected,
            onDismiss = onDialogDismiss
        )
    }

    is LockscreenStylesDialogUiState.FontFamilyDialog -> {
        FontListPreferenceDialog(
            title = stringResource(id = R.string.pref_font_style_source_title),
            fonts = uiDialogState.fonts,
            selectedItem = uiDialogState.currentFont,
            onItemSelected = onFontFamilySelected,
            onCustomize = onFontCustomize,
            onDismiss = onDialogDismiss
        )
    }

    is LockscreenStylesDialogUiState.QuoteSpacingDialog -> {
        ListPreferenceDialog(
            title = stringResource(id = R.string.pref_layout_quote_spacing_title),
            entries = stringArrayResource(id = R.array.layout_quote_spacing_entries),
            entryValues = integerArrayResource(id = R.array.layout_quote_spacing_values).toTypedArray(),
            selectedItem = uiDialogState.spacing,
            onItemSelected = onQuoteSpacingSelected,
            onDismiss = onDialogDismiss
        )
    }

    is LockscreenStylesDialogUiState.PaddingTopDialog -> {
        ListPreferenceDialog(
            title = stringResource(id = R.string.pref_layout_padding_top_title),
            entries = stringArrayResource(id = R.array.layout_padding_entries),
            entryValues = integerArrayResource(id = R.array.layout_padding_values).toTypedArray(),
            selectedItem = uiDialogState.padding,
            onItemSelected = onPaddingTopSelected,
            onDismiss = onDialogDismiss
        )
    }

    is LockscreenStylesDialogUiState.PaddingBottomDialog -> {
        ListPreferenceDialog(
            title = stringResource(id = R.string.pref_layout_padding_bottom_title),
            entries = stringArrayResource(id = R.array.layout_padding_entries),
            entryValues = integerArrayResource(id = R.array.layout_padding_values).toTypedArray(),
            selectedItem = uiDialogState.padding,
            onItemSelected = onPaddingBottomSelected,
            onDismiss = onDialogDismiss
        )
    }

    is LockscreenStylesDialogUiState.None -> {}
}

@Preview(
    name = "Lockscreen Styles Screen Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Lockscreen Styles Screen Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun LockscreenStylesScreenPreview() {
    QuoteLockTheme {
        Surface {
            LockscreenStylesScreen(uiState = LockscreenStylesUiState(enableFontFamily = true))
        }
    }
}