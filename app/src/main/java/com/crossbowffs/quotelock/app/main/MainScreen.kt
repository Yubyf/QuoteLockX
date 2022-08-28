@file:OptIn(ExperimentalMaterial3Api::class)

package com.crossbowffs.quotelock.app.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.crossbowffs.quotelock.app.settings.PreviewRoute
import com.crossbowffs.quotelock.app.settings.SettingsRoute
import com.crossbowffs.quotelock.consts.Urls
import com.crossbowffs.quotelock.ui.components.LoadingDialog
import com.crossbowffs.quotelock.ui.components.MainAppBar
import com.crossbowffs.quotelock.utils.XposedUtils
import com.yubyf.quotelockx.R
import kotlinx.coroutines.launch

@SuppressLint("BatteryLife")
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel(),
    onModuleConfigItemClicked: (String) -> Unit,
    onCollectionItemClicked: () -> Unit,
    onHistoryItemClicked: () -> Unit,
) {
    val uiEvent by viewModel.uiEvent.collectAsState(initial = null)
    val uiState by viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = { MainAppBar { viewModel.refreshQuote() } }
    ) { padding ->
        uiEvent?.also { event ->
            when (event) {
                is MainUiEvent.SnackBarMessage -> {
                    event.message?.let {
                        val messageText = it
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = messageText,
                                duration = SnackbarDuration.Short,
                                actionLabel = event.actionText
                            )
                        }
                    }
                }
            }
        }
        Column(modifier = modifier
            .fillMaxSize()
            .padding(padding)
        ) {
            PreviewRoute()
            SettingsRoute(
                onModuleConfigItemClicked = onModuleConfigItemClicked,
                onCollectionItemClicked = onCollectionItemClicked,
                onHistoryItemClicked = onHistoryItemClicked
            )
        }
    }
    fun cancelDialog() = viewModel.cancelDialog()
    with(uiState) {
        when (this) {
            is MainUiState.ProgressDialog -> {
                LoadingDialog(message = message,
                    dismissOnClickOutside = false,
                    onDismiss = ::cancelDialog)
            }
            MainUiState.EnableModuleDialog -> {
                com.crossbowffs.quotelock.ui.components.AlertDialog(
                    onDismissRequest = ::cancelDialog,
                    title = { Text(text = stringResource(id = R.string.enable_xposed_module_title)) },
                    text = { Text(text = stringResource(id = R.string.enable_xposed_module_message)) },
                    icon = {
                        Icon(painter = painterResource(id = R.drawable.ic_round_warning_24dp),
                            contentDescription = null)
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            cancelDialog()
                            with(viewModel) { context.startXposedPage(XposedUtils.XPOSED_SECTION_MODULES) }
                        }) {
                            Text(text = stringResource(id = R.string.enable))
                        }
                    },
                    neutralButton = {
                        TextButton(onClick = {
                            cancelDialog()
                            with(viewModel) { context.startBrowserActivity(Urls.GITHUB_QUOTELOCK_CURRENT_ISSUES) }
                        }) {
                            Text(text = stringResource(id = R.string.report_bug))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = ::cancelDialog) {
                            Text(text = stringResource(id = R.string.ignore))
                        }
                    }
                )
            }
            MainUiState.ModuleUpdatedDialog -> {
                AlertDialog(
                    onDismissRequest = ::cancelDialog,
                    title = { Text(text = stringResource(id = R.string.module_outdated_title)) },
                    text = { Text(text = stringResource(id = R.string.module_outdated_message)) },
                    icon = {
                        Icon(painter = painterResource(id = R.drawable.ic_round_warning_24dp),
                            contentDescription = null)
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            cancelDialog()
                            with(viewModel) { context.startXposedPage(XposedUtils.XPOSED_SECTION_INSTALL) }
                        }) {
                            Text(text = stringResource(id = R.string.reboot))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = ::cancelDialog) {
                            Text(text = stringResource(id = R.string.ignore))
                        }
                    }
                )
            }
            MainUiState.None -> {}
        }
        var showBatteryOptimizationDialog by remember {
            mutableStateOf(context.checkBatteryOptimization())
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && showBatteryOptimizationDialog) {
            AlertDialog(
                onDismissRequest = ::cancelDialog,
                title = { Text(text = stringResource(id = R.string.battery_optimization_title)) },
                text = { Text(text = stringResource(id = R.string.battery_optimization_message)) },
                confirmButton = {
                    TextButton(onClick = {
                        showBatteryOptimizationDialog = false
                        with(context) {
                            val intent = Intent()
                            intent.action =
                                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                            intent.data = Uri.parse("package:$packageName")
                            startActivity(Intent().apply {
                                action =
                                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                                data = Uri.parse("package:$packageName")
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
}

private fun Context.checkBatteryOptimization(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val pm = getSystemService(AppCompatActivity.POWER_SERVICE) as? PowerManager
        pm?.isIgnoringBatteryOptimizations(packageName) != true
    } else false
}
