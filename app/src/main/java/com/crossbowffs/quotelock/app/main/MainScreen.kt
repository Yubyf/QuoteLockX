@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.crossbowffs.quotelock.app.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.crossbowffs.quotelock.app.detail.QuoteDetailPage
import com.crossbowffs.quotelock.app.detail.QuoteDetailViewModel
import com.crossbowffs.quotelock.app.detail.shareImage
import com.crossbowffs.quotelock.app.detail.style.CardStylePopup
import com.crossbowffs.quotelock.app.detail.style.CardStyleViewModel
import com.crossbowffs.quotelock.consts.Urls
import com.crossbowffs.quotelock.ui.components.AlertDialog
import com.crossbowffs.quotelock.ui.components.MainAppBar
import com.crossbowffs.quotelock.ui.components.Snapshotables
import com.crossbowffs.quotelock.ui.components.TopAppBarDropdownMenu
import com.crossbowffs.quotelock.utils.XposedUtils
import com.yubyf.quotelockx.R
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = hiltViewModel(),
    detailViewModel: QuoteDetailViewModel = hiltViewModel(),
    cardStyleViewModel: CardStyleViewModel = hiltViewModel(),
    onSettingsItemClick: () -> Unit,
    onLockscreenStylesItemClick: () -> Unit,
    onCollectionItemClick: () -> Unit,
    onHistoryItemClick: () -> Unit,
    onFontCustomize: () -> Unit,
) {
    val mainUiEvent by mainViewModel.uiEvent.collectAsState(initial = null)
    val mainUiState by mainViewModel.uiState
    val mainDialogUiState by mainViewModel.uiDialogState
    val detailUiState by detailViewModel.uiState
    val detailUiEvent by detailViewModel.uiEvent.collectAsState(initial = null)
    val cardStyleUiState by cardStyleViewModel.uiState
    detailUiEvent?.shareFile?.let { file ->
        LocalContext.current.shareImage(file)
        detailViewModel.quoteShared()
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val snapshotStates = Snapshotables()
    val rotationAnimation = remember { Animatable(0f) }
    val rotation by rotationAnimation.asState()
    if (mainUiState.refreshing && !rotationAnimation.isRunning) {
        LaunchedEffect(Unit) {
            scope.launch {
                rotationAnimation.snapTo(0F)
                rotationAnimation.animateTo(targetValue = 360F,
                    animationSpec = repeatable(
                        animation = tween(500, easing = LinearEasing),
                        iterations = AnimationConstants.DefaultDurationMillis,
                        repeatMode = RepeatMode.Restart
                    ))
            }
        }
    } else if (!mainUiState.refreshing && rotationAnimation.isRunning) {
        LaunchedEffect(Unit) {
            scope.launch {
                rotationAnimation.stop()
                rotationAnimation.animateTo(targetValue = 360F,
                    animationSpec = TweenSpec(
                        ((360F - rotationAnimation.value) / 360 * 500).roundToInt(),
                        easing = LinearEasing
                    ))
            }
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            MainAppBar(onStyle = cardStyleViewModel::showStylePopup) {
                MainDropdownMenu(
                    onSettingsItemClick = onSettingsItemClick,
                    onLockscreenStylesItemClick = onLockscreenStylesItemClick,
                    onShareItemClick = {
                        snapshotStates.bounds?.let {
                            detailViewModel.shareQuote(it) { canvas ->
                                snapshotStates.forEach { snapshot ->
                                    snapshot.snapshot(canvas)
                                }
                            }
                        }
                    },
                    onCollectionItemClick = onCollectionItemClick,
                    onHistoryItemClick = onHistoryItemClick
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(text = stringResource(id = R.string.refresh_quote)) },
                icon = {
                    Icon(Icons.Rounded.Refresh,
                        contentDescription = stringResource(id = R.string.refresh_quote),
                        modifier = Modifier.rotate(rotation))
                },
                onClick = {
                    if (!mainUiState.refreshing) {
                        mainViewModel.refreshQuote()
                    }
                }
            )
        },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->
        mainUiEvent?.also { event ->
            when (event) {
                is MainUiEvent.SnackBarMessage -> {
                    event.message?.let { message ->
                        scope.launch {
                            snackbarHostState.currentSnackbarData?.takeIf {
                                it.visuals.message == message
                            }?.dismiss()
                            snackbarHostState.showSnackbar(
                                message = message,
                                duration = SnackbarDuration.Short,
                                actionLabel = event.actionText
                            )
                        }
                        mainViewModel.snackbarShown()
                    }
                }
            }
        }
        Box(modifier = modifier
            .fillMaxSize()
            .padding(padding)
            .consumedWindowInsets(padding)
        ) {
            QuoteDetailPage(
                modifier = modifier
                    .fillMaxSize(),
                quoteData = mainUiState.quoteData,
                cardStyle = detailUiState.cardStyle,
                snapshotStates = snapshotStates,
                onCollectClick = detailViewModel::switchCollectionState
            )
            cardStyleUiState.takeIf { cardStyleUiState.show }?.let {
                CardStylePopup(
                    fonts = it.fonts,
                    cardStyle = it.cardStyle,
                    onFontSelected = cardStyleViewModel::selectFontFamily,
                    onFontAdd = onFontCustomize,
                    onQuoteSizeChange = cardStyleViewModel::setQuoteSize,
                    onSourceSizeChange = cardStyleViewModel::setSourceSize,
                    onLineSpacingChange = cardStyleViewModel::setLineSpacing,
                    onCardPaddingChange = cardStyleViewModel::setCardPadding,
                    onDismiss = cardStyleViewModel::dismissStylePopup
                )
            }
        }
    }
    val context = LocalContext.current
    MainDialogs(
        uiDialogState = mainDialogUiState,
        onStartXposedPage = { with(mainViewModel) { context.startXposedPage(it) } },
        onStartBrowserActivity = { with(mainViewModel) { context.startBrowserActivity(it) } },
        onDialogDismiss = mainViewModel::cancelDialog
    )
}

@Composable
fun MainDropdownMenu(
    onSettingsItemClick: () -> Unit,
    onLockscreenStylesItemClick: () -> Unit,
    onShareItemClick: () -> Unit,
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
                    Icon(Icons.Rounded.Settings,
                        contentDescription = stringResource(id = R.string.settings))
                },
                text = { Text(text = stringResource(id = R.string.settings)) },
                onClick = {
                    closeMenu()
                    onSettingsItemClick()
                }
            )
            DropdownMenuItem(
                leadingIcon = {
                    Icon(Icons.Rounded.Style,
                        contentDescription = stringResource(id = R.string.lockscreen_styles))
                },
                text = { Text(text = stringResource(id = R.string.lockscreen_styles)) },
                onClick = {
                    closeMenu()
                    onLockscreenStylesItemClick()
                }
            )
            DropdownMenuItem(
                leadingIcon = {
                    Icon(Icons.Rounded.Share,
                        contentDescription = stringResource(id = R.string.quote_image_share))
                },
                text = { Text(text = stringResource(id = R.string.quote_image_share)) },
                onClick = {
                    closeMenu()
                    onShareItemClick()
                }
            )
            DropdownMenuItem(
                leadingIcon = {
                    Icon(Icons.Rounded.Star,
                        contentDescription = stringResource(id = R.string.pref_collection_title))
                },
                text = { Text(text = stringResource(id = R.string.pref_collection_title)) },
                onClick = {
                    closeMenu()
                    onCollectionItemClick()
                }
            )
            DropdownMenuItem(
                leadingIcon = {
                    Icon(Icons.Rounded.ManageSearch,
                        contentDescription = stringResource(id = R.string.pref_history_title))
                },
                text = { Text(text = stringResource(id = R.string.pref_history_title)) },
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
                    Icon(painter = painterResource(id = R.drawable.ic_round_warning_24dp),
                        contentDescription = null)
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
                    Icon(painter = painterResource(id = R.drawable.ic_round_warning_24dp),
                        contentDescription = null)
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
