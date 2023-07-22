@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.crossbowffs.quotelock.app.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ManageSearch
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.crossbowffs.quotelock.app.emptySnackBarEvent
import com.crossbowffs.quotelock.app.font.FontInfo
import com.crossbowffs.quotelock.app.quote.QuotePage
import com.crossbowffs.quotelock.app.quote.QuoteUiState
import com.crossbowffs.quotelock.app.quote.QuoteViewModel
import com.crossbowffs.quotelock.app.quote.style.CardStylePopup
import com.crossbowffs.quotelock.app.quote.style.CardStyleUiState
import com.crossbowffs.quotelock.app.quote.style.CardStyleViewModel
import com.crossbowffs.quotelock.consts.Urls
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.data.api.QuoteDataWithCollectState
import com.crossbowffs.quotelock.data.api.contextString
import com.crossbowffs.quotelock.data.api.isQuoteGeneratedByConfiguration
import com.crossbowffs.quotelock.ui.components.AlertDialog
import com.crossbowffs.quotelock.ui.components.MainAppBar
import com.crossbowffs.quotelock.ui.components.Snapshotables
import com.crossbowffs.quotelock.ui.components.TopAppBarDropdownMenu
import com.crossbowffs.quotelock.utils.XposedUtils
import com.yubyf.quotelockx.R
import kotlinx.coroutines.launch
import org.koin.androidx.compose.navigation.koinNavViewModel
import kotlin.math.roundToInt


@Composable
fun MainRoute(
    modifier: Modifier = Modifier,
    mainScreenViewModel: MainScreenViewModel = koinNavViewModel(),
    quoteViewModel: QuoteViewModel = koinNavViewModel(),
    cardStyleViewModel: CardStyleViewModel = koinNavViewModel(),
    onSettingsItemClick: () -> Unit,
    onLockscreenStylesItemClick: () -> Unit,
    onCollectionItemClick: () -> Unit,
    onHistoryItemClick: () -> Unit,
    onFontCustomize: () -> Unit,
    onShare: () -> Unit,
    onDetail: (QuoteData) -> Unit,
) {
    val mainUiMessageEvent by mainScreenViewModel.uiMessageEvent.collectAsState(initial = emptySnackBarEvent)
    val mainUiState by mainScreenViewModel.uiState
    val mainDialogUiState by mainScreenViewModel.uiDialogState
    val quoteUiState by quoteViewModel.uiState
    val cardStyleUiState by cardStyleViewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    mainUiMessageEvent.let { event ->
        event.message?.let { message ->
            scope.launch {
                snackbarHostState.currentSnackbarData?.takeIf {
                    it.visuals.message == message.contextString(context)
                }?.dismiss()
                snackbarHostState.showSnackbar(
                    message = message.contextString(context),
                    duration = SnackbarDuration.Short,
                    actionLabel = event.actionText.contextString(context)
                )
            }
            mainScreenViewModel.snackBarShown()
        }
    }
    MainScreen(
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        mainUiState = mainUiState,
        quoteUiState = quoteUiState,
        cardStyleUiState = cardStyleUiState,
        showStylePopup = cardStyleViewModel::showStylePopup,
        refreshQuote = mainScreenViewModel::refreshQuote,
        switchCollectionState = quoteViewModel::switchCollectionState,
        shareQuote = { quoteViewModel.setSnapshotables(it); onShare() },
        onDetail = onDetail,
        onSettingsItemClick = onSettingsItemClick,
        onLockscreenStylesItemClick = onLockscreenStylesItemClick,
        onCollectionItemClick = onCollectionItemClick,
        onHistoryItemClick = onHistoryItemClick,
        selectFontFamily = cardStyleViewModel::selectFontFamily,
        onFontCustomize = onFontCustomize,
        onQuoteSizeChange = cardStyleViewModel::setQuoteSize,
        onSourceSizeChange = cardStyleViewModel::setSourceSize,
        onLineSpacingChange = cardStyleViewModel::setLineSpacing,
        onCardPaddingChange = cardStyleViewModel::setCardPadding,
        onQuoteWeightChange = cardStyleViewModel::setQuoteWeight,
        onQuoteItalicChange = cardStyleViewModel::setQuoteItalic,
        onSourceWeightChange = cardStyleViewModel::setSourceWeight,
        onSourceItalicChange = cardStyleViewModel::setSourceItalic,
        dismissStylePopup = cardStyleViewModel::dismissStylePopup
    )
    MainDialogs(
        uiDialogState = mainDialogUiState,
        onStartXposedPage = { with(mainScreenViewModel) { context.startXposedPage(it) } },
        onStartBrowserActivity = { with(mainScreenViewModel) { context.startBrowserActivity(it) } },
        onDialogDismiss = mainScreenViewModel::cancelDialog
    )
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    mainUiState: MainUiState,
    quoteUiState: QuoteUiState,
    cardStyleUiState: CardStyleUiState,
    showStylePopup: () -> Unit = {},
    refreshQuote: () -> Unit = {},
    switchCollectionState: (QuoteDataWithCollectState) -> Unit = {},
    shareQuote: (Snapshotables) -> Unit = {},
    onDetail: (QuoteData) -> Unit = {},
    onSettingsItemClick: () -> Unit = {},
    onLockscreenStylesItemClick: () -> Unit = {},
    onCollectionItemClick: () -> Unit = {},
    onHistoryItemClick: () -> Unit = {},
    selectFontFamily: (FontInfo) -> Unit = { },
    onFontCustomize: () -> Unit = {},
    onQuoteSizeChange: (Int) -> Unit = {},
    onSourceSizeChange: (Int) -> Unit = {},
    onLineSpacingChange: (Int) -> Unit = {},
    onCardPaddingChange: (Int) -> Unit = {},
    onQuoteWeightChange: (Int) -> Unit = {},
    onQuoteItalicChange: (Float) -> Unit = {},
    onSourceWeightChange: (Int) -> Unit = {},
    onSourceItalicChange: (Float) -> Unit = {},
    dismissStylePopup: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val snapshotStates = Snapshotables()
    val rotationAnimation = remember { Animatable(0f) }
    val rotation by rotationAnimation.asState()
    if (mainUiState.refreshing && !rotationAnimation.isRunning) {
        LaunchedEffect(Unit) {
            scope.launch {
                rotationAnimation.snapTo(0F)
                rotationAnimation.animateTo(
                    targetValue = 360F,
                    animationSpec = repeatable(
                        animation = tween(500, easing = LinearEasing),
                        iterations = AnimationConstants.DefaultDurationMillis,
                        repeatMode = RepeatMode.Restart
                    )
                )
            }
        }
    } else if (!mainUiState.refreshing && rotationAnimation.isRunning) {
        LaunchedEffect(Unit) {
            scope.launch {
                rotationAnimation.stop()
                rotationAnimation.animateTo(
                    targetValue = 360F,
                    animationSpec = TweenSpec(
                        ((360F - rotationAnimation.value) / 360 * 500).roundToInt(),
                        easing = LinearEasing
                    )
                )
            }
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            MainAppBar(onStyle = showStylePopup) {
                MainDropdownMenu(
                    showUpdate = mainUiState.showUpdate,
                    onSettingsItemClick = onSettingsItemClick,
                    onLockscreenStylesItemClick = onLockscreenStylesItemClick,
                    onCollectionItemClick = onCollectionItemClick,
                    onHistoryItemClick = onHistoryItemClick
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(text = stringResource(id = R.string.refresh_quote)) },
                icon = {
                    Icon(
                        Icons.Rounded.Refresh,
                        contentDescription = stringResource(id = R.string.refresh_quote),
                        modifier = Modifier.rotate(rotation)
                    )
                },
                shape = FloatingActionButtonDefaults.largeShape,
                onClick = {
                    if (!mainUiState.refreshing) {
                        refreshQuote()
                    }
                }
            )
        },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
        ) {
            QuotePage(
                modifier = Modifier
                    .fillMaxSize(),
                quoteData = mainUiState.quoteData,
                refreshing = mainUiState.refreshing,
                cardStyle = quoteUiState.cardStyle,
                snapshotStates = snapshotStates,
                onCollectClick = switchCollectionState,
                onShareCard = if (!LocalInspectionMode.current
                    && !LocalContext.current.isQuoteGeneratedByConfiguration(
                        mainUiState.quoteData.quoteText,
                        mainUiState.quoteData.quoteSource,
                        mainUiState.quoteData.quoteAuthor
                    )
                ) shareQuote else null,
                onDetailClick = onDetail
            )
            CardStylePopup(
                popped = cardStyleUiState.show,
                fonts = cardStyleUiState.fonts,
                cardStyle = cardStyleUiState.cardStyle,
                onFontSelected = selectFontFamily,
                onFontAdd = onFontCustomize,
                onQuoteSizeChange = onQuoteSizeChange,
                onSourceSizeChange = onSourceSizeChange,
                onLineSpacingChange = onLineSpacingChange,
                onCardPaddingChange = onCardPaddingChange,
                onQuoteWeightChange = onQuoteWeightChange,
                onQuoteItalicChange = onQuoteItalicChange,
                onSourceWeightChange = onSourceWeightChange,
                onSourceItalicChange = onSourceItalicChange,
                onDismiss = dismissStylePopup
            )
        }
    }
}

@Composable
fun MainDropdownMenu(
    showUpdate: Boolean = false,
    onSettingsItemClick: () -> Unit,
    onLockscreenStylesItemClick: () -> Unit,
    onCollectionItemClick: () -> Unit,
    onHistoryItemClick: () -> Unit,
) {
    TopAppBarDropdownMenu(
        minWidth = 148.dp,
        iconContent = {
            Icon(Icons.Rounded.MoreVert, contentDescription = "More")
        },
        content = { _, closeMenu ->
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Settings,
                        contentDescription = stringResource(id = R.string.settings)
                    )
                },
                text = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(text = stringResource(id = R.string.settings))
                        if (showUpdate) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                    .align(Alignment.CenterEnd)
                            )
                        }
                    }
                },
                onClick = {
                    closeMenu()
                    onSettingsItemClick()
                }
            )
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Style,
                        contentDescription = stringResource(id = R.string.lockscreen_styles)
                    )
                },
                text = { Text(text = stringResource(id = R.string.lockscreen_styles)) },
                onClick = {
                    closeMenu()
                    onLockscreenStylesItemClick()
                }
            )
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Star,
                        contentDescription = stringResource(id = R.string.quote_collections_screen_label)
                    )
                },
                text = { Text(text = stringResource(id = R.string.quote_collections_screen_label)) },
                onClick = {
                    closeMenu()
                    onCollectionItemClick()
                }
            )
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        Icons.Rounded.ManageSearch,
                        contentDescription = stringResource(id = R.string.quote_histories_screen_label)
                    )
                },
                text = { Text(text = stringResource(id = R.string.quote_histories_screen_label)) },
                onClick = {
                    closeMenu()
                    onHistoryItemClick()
                }
            )
        })
}

@SuppressLint("BatteryLife")
@Composable
fun MainDialogs(
    uiDialogState: MainDialogUiState,
    onStartXposedPage: (String) -> Unit = {},
    onStartBrowserActivity: (String) -> Unit,
    onDialogDismiss: () -> Unit,
) = with(uiDialogState) {
    val context = LocalContext.current
    when (this) {
        MainDialogUiState.EnableModuleDialog -> {
            AlertDialog(
                onDismissRequest = onDialogDismiss,
                title = { Text(text = stringResource(id = R.string.enable_xposed_module_title)) },
                text = { Text(text = stringResource(id = R.string.enable_xposed_module_message)) },
                icon = {
                    Icon(Icons.Rounded.Warning, contentDescription = null)
                },
                confirmButton = {
                    TextButton(onClick = {
                        onDialogDismiss()
                        onStartXposedPage(XposedUtils.XPOSED_SECTION_MODULES)
                    }) {
                        Text(text = stringResource(id = R.string.enable))
                    }
                },
                neutralButton = {
                    TextButton(onClick = {
                        onDialogDismiss()
                        onStartBrowserActivity(Urls.GITHUB_QUOTELOCK_CURRENT_ISSUES)
                    }) {
                        Text(text = stringResource(id = R.string.report_bug))
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDialogDismiss) {
                        Text(text = stringResource(id = R.string.ignore))
                    }
                }
            )
        }

        MainDialogUiState.ModuleUpdatedDialog -> {
            AlertDialog(
                onDismissRequest = onDialogDismiss,
                title = { Text(text = stringResource(id = R.string.module_outdated_title)) },
                text = { Text(text = stringResource(id = R.string.module_outdated_message)) },
                icon = {
                    Icon(Icons.Rounded.Warning, contentDescription = null)
                },
                confirmButton = {
                    TextButton(onClick = {
                        onDialogDismiss()
                        onStartXposedPage(XposedUtils.XPOSED_SECTION_INSTALL)
                    }) {
                        Text(text = stringResource(id = R.string.reboot))
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDialogDismiss) {
                        Text(text = stringResource(id = R.string.ignore))
                    }
                }
            )
        }

        MainDialogUiState.None -> {}
    }
    var showBatteryOptimizationDialog by remember {
        mutableStateOf(context.checkBatteryOptimization())
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && showBatteryOptimizationDialog) {
        AlertDialog(
            onDismissRequest = onDialogDismiss,
            title = { Text(text = stringResource(id = R.string.battery_optimization_title)) },
            text = { Text(text = stringResource(id = R.string.battery_optimization_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showBatteryOptimizationDialog = false
                    with(context) {
                        val intent = Intent()
                        intent.action =
                            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                        intent.data = android.net.Uri.parse("package:$packageName")
                        startActivity(Intent().apply {
                            action =
                                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                            data = android.net.Uri.parse("package:$packageName")
                        })
                    }
                }) {
                    Text(text = stringResource(id = R.string.disable))
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatteryOptimizationDialog = false }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            }
        )
    }
}

private fun Context.checkBatteryOptimization(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val pm = getSystemService(AppCompatActivity.POWER_SERVICE) as? PowerManager
        pm?.isIgnoringBatteryOptimizations(packageName) != true
    } else false
}
