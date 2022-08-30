@file:OptIn(ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class)

package com.crossbowffs.quotelock.app.font

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.navigation.compose.hiltViewModel
import com.crossbowffs.quotelock.consts.Urls
import com.crossbowffs.quotelock.ui.components.DeletableFontListItem
import com.crossbowffs.quotelock.ui.components.FontManagementAppBar
import com.crossbowffs.quotelock.ui.components.LoadingDialog
import com.yubyf.quotelockx.R
import kotlinx.coroutines.launch

@Composable
fun FontManagementRoute(
    modifier: Modifier = Modifier,
    viewModel: FontManagementViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiListState
    val uiDialogState by viewModel.uiDialogState
    val uiEvent by viewModel.uiEvent.collectAsState(initial = null)
    var pickedFontFileUri by remember { mutableStateOf<Uri?>(null) }
    val pickedFontFileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            pickedFontFileUri = uri
        }
    FontManagementScreen(
        modifier = modifier,
        uiState = uiState,
        uiDialogState = uiDialogState,
        uiEvent = uiEvent,
        onBack = onBack,
        onImportButtonClick = {
            viewModel.listScrolled()
            pickedFontFileLauncher.launch(arrayOf("font/ttf", "font/otf"))
        },
        onDeleteMenuClick = viewModel::delete,
        onDialogDismiss = viewModel::cancelDialog,
        listScrolled = viewModel::listScrolled,
        snackbarShown = viewModel::snackbarShown
    )
    pickedFontFileUri?.let { uri ->
        viewModel.importFont(uri)
        pickedFontFileUri = null
    }
}

@Composable
fun FontManagementScreen(
    modifier: Modifier = Modifier,
    uiState: FontManagementListUiState,
    uiDialogState: FontManagementDialogUiState,
    uiEvent: FontManagementUiEvent?,
    onBack: () -> Unit,
    onImportButtonClick: () -> Unit,
    onDeleteMenuClick: (FontInfoWithState) -> Unit,
    onDialogDismiss: () -> Unit,
    snackbarShown: () -> Unit,
    listScrolled: () -> Unit,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            FontManagementAppBar(onBack = onBack)
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(text = stringResource(id = R.string.quote_fonts_management_import)) },
                icon = {
                    Icon(Icons.Rounded.Add,
                        contentDescription = stringResource(id = R.string.quote_fonts_management_import))
                },
                onClick = onImportButtonClick
            )
        },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->
        when (uiEvent) {
            is FontManagementUiEvent.SnackBarMessage -> {
                uiEvent.message?.let {
                    val messageText = it
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = messageText,
                            duration = uiEvent.duration,
                            actionLabel = uiEvent.actionText
                        )
                    }
                    snackbarShown()
                }
            }
            null -> {}
        }
        val fontActiveHint = stringResource(id = R.string.quote_fonts_management_activate_tips)
        FontInfoItemList(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .consumedWindowInsets(padding),
            listState = listState,
            entities = uiState.items,
            onDeleteMenuClicked = onDeleteMenuClick,
            onFontActiveHintClick = {
                scope.launch { snackbarHostState.showSnackbar(fontActiveHint) }
            }
        )
        when (uiDialogState) {
            FontManagementDialogUiState.EnableMagiskModuleDialog -> {
                AlertDialog(
                    onDismissRequest = onDialogDismiss,
                    title = { Text(text = stringResource(id = R.string.quote_fonts_magisk_module_needed_title)) },
                    text = { Text(text = stringResource(id = R.string.quote_fonts_magisk_module_needed_message)) },
                    icon = { Icon(Icons.Rounded.Warning, contentDescription = null) },
                    dismissButton = {
                        TextButton(onClick = { onDialogDismiss(); onBack() }) {
                            Text(text = stringResource(id = R.string.ignore))
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW,
                                Uri.parse(Urls.GITHUB_QUOTELOCK_CUSTOM_FONTS_RELEASE)))
                        }) {
                            Text(text = stringResource(id = R.string.download))
                        }
                    }
                )
            }
            is FontManagementDialogUiState.ProgressDialog -> {
                LoadingDialog(message = uiDialogState.message) {}
            }
            FontManagementDialogUiState.None -> {}
        }
        if (uiState.scrollToBottom) {
            LaunchedEffect(uiState.scrollToBottom) {
                listState.animateScrollToItem(uiState.items.lastIndex)
                listScrolled()
            }
        }
    }
}

@Composable
private fun FontInfoItemList(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    entities: List<FontInfoWithState>,
    onDeleteMenuClicked: (FontInfoWithState) -> Unit,
    onFontActiveHintClick: () -> Unit,
) {
    Surface {
        LazyColumn(
            modifier = modifier,
            state = listState
        ) {
            val animationSpec: FiniteAnimationSpec<IntOffset> = tween(
                durationMillis = 300,
                easing = LinearOutSlowInEasing,
            )
            itemsIndexed(entities, key = { _, item -> item.fontInfo.fileName }) { index, entity ->
                DeletableFontListItem(
                    modifier = Modifier
                        .animateItemPlacement(animationSpec)
                        .fillMaxWidth(),
                    fontInfoWithState = entity,
                    onFontActiveHintClick = onFontActiveHintClick
                ) {
                    onDeleteMenuClicked.invoke(it)
                }
                if (index < entities.lastIndex) {
                    Divider(Modifier
                        .animateItemPlacement(animationSpec)
                        .fillMaxWidth())
                }
            }
        }
    }
}
