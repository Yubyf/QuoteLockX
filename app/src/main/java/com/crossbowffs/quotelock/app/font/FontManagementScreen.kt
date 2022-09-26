@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class, ExperimentalPagerApi::class)

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.crossbowffs.quotelock.app.SnackBarEvent
import com.crossbowffs.quotelock.app.emptySnackBarEvent
import com.crossbowffs.quotelock.consts.Urls
import com.crossbowffs.quotelock.ui.components.*
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
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
    val uiEvent by viewModel.uiEvent.collectAsState(initial = emptySnackBarEvent)
    var pickedFontFileForAppUri by remember { mutableStateOf<Uri?>(null) }
    val pickedFontFileForAppLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            pickedFontFileForAppUri = uri
        }
    var pickedFontFileForSystemUri by remember { mutableStateOf<Uri?>(null) }
    val pickedFontFileForSystemLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            pickedFontFileForSystemUri = uri
        }
    FontManagementScreen(
        modifier = modifier,
        uiState = uiState,
        uiDialogState = uiDialogState,
        uiEvent = uiEvent,
        onBack = onBack,
        onInAppImportButtonClick = {
            viewModel.inAppFontListScrolled()
            pickedFontFileForAppLauncher.launch(arrayOf("font/ttf", "font/otf"))
        },
        onSystemImportButtonClick = {
            viewModel.systemFontListScrolled()
            pickedFontFileForSystemLauncher.launch(arrayOf("font/ttf", "font/otf"))
        },
        onSystemFontDeleteMenuClick = viewModel::deleteSystemFont,
        onInAppFontDeleteMenuClick = viewModel::deleteInAppFont,
        listScrolled = viewModel::systemFontListScrolled,
        snackBarShown = viewModel::snackBarShown
    )
    pickedFontFileForSystemUri?.let { uri ->
        viewModel.importFontToSystem(uri)
        pickedFontFileForSystemUri = null
    }
    pickedFontFileForAppUri?.let { uri ->
        viewModel.importFontInApp(uri)
        pickedFontFileForAppUri = null
    }
}

@Composable
fun FontManagementScreen(
    modifier: Modifier = Modifier,
    uiState: FontManagementListUiState,
    uiDialogState: FontManagementDialogUiState,
    uiEvent: SnackBarEvent,
    onBack: () -> Unit,
    onInAppImportButtonClick: () -> Unit,
    onSystemImportButtonClick: () -> Unit,
    onSystemFontDeleteMenuClick: (FontInfoWithState) -> Unit,
    onInAppFontDeleteMenuClick: (FontInfo) -> Unit,
    snackBarShown: () -> Unit,
    listScrolled: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState()
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = { FontManagementAppBar(onBack = onBack) },
        floatingActionButton = {
            if (pagerState.currentPage != 1 || uiState.systemFontEnabled) {
                ExtendedFloatingActionButton(
                    text = { Text(text = stringResource(id = R.string.quote_fonts_management_import)) },
                    icon = {
                        Icon(Icons.Rounded.Add,
                            contentDescription = stringResource(id = R.string.quote_fonts_management_import))
                    },
                    shape = FloatingActionButtonDefaults.largeShape,
                    onClick = if (pagerState.currentPage == 0) onInAppImportButtonClick
                    else onSystemImportButtonClick
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->
        uiEvent.message?.let {
            val messageText = it
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = messageText,
                    duration = uiEvent.duration,
                    actionLabel = uiEvent.actionText
                )
            }
            snackBarShown()
        }
        val tabTitles = stringArrayResource(id = R.array.font_tabs)
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .consumedWindowInsets(padding),
        ) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier
                            .pagerTabIndicatorOffset(pagerState, tabPositions)
                            .clip(RoundedCornerShape(1.dp)),
                        height = 2.dp,
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch { pagerState.scrollToPage(index) }
                        },
                        text = {
                            Text(text = title,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis)
                        }
                    )
                }
            }
            HorizontalPager(
                modifier = modifier
                    .fillMaxSize(),
                count = tabTitles.size,
                state = pagerState
            ) { page ->
                if (page == 0) {
                    val listState = rememberLazyListState()
                    InAppFontInfoItemList(
                        modifier = modifier
                            .fillMaxSize(),
                        listState = listState,
                        entities = uiState.inAppFontItems,
                        onDeleteMenuClicked = onInAppFontDeleteMenuClick,
                    )
                    if (uiState.inAppTabScrollToBottom) {
                        LaunchedEffect(Unit) {
                            listState.animateScrollToItem(uiState.inAppFontItems.lastIndex)
                            listScrolled()
                        }
                    }
                } else {
                    if (uiState.systemFontEnabled) {
                        val listState = rememberLazyListState()
                        SystemFontInfoItemList(
                            modifier = modifier
                                .fillMaxSize(),
                            listState = listState,
                            entities = uiState.systemFontItems,
                            onDeleteMenuClicked = onSystemFontDeleteMenuClick,
                        )
                        if (uiState.systemTabScrollToBottom) {
                            LaunchedEffect(Unit) {
                                listState.animateScrollToItem(uiState.systemFontItems.lastIndex)
                                listScrolled()
                            }
                        }
                    } else {
                        EnableMagiskModuleLayout()
                    }
                }
            }
        }
        when (uiDialogState) {
            is FontManagementDialogUiState.ProgressDialog -> {
                LoadingDialog(message = uiDialogState.message) {}
            }
            FontManagementDialogUiState.None -> {}
        }
    }
}

@Composable
private fun InAppFontInfoItemList(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    entities: List<FontInfo>,
    onDeleteMenuClicked: (FontInfo) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        state = listState
    ) {
        val animationSpec: FiniteAnimationSpec<IntOffset> = tween(
            durationMillis = 300,
            easing = LinearOutSlowInEasing,
        )
        itemsIndexed(entities, key = { _, item -> item.fileName }) { index, entity ->
            DeletableFontListItem(
                modifier = Modifier
                    .animateItemPlacement(animationSpec)
                    .fillMaxWidth(),
                fontInfoWithState = FontInfoWithState(fontInfo = entity,
                    systemFont = false,
                    active = true),
            ) {
                onDeleteMenuClicked.invoke(it.fontInfo)
            }
            if (index < entities.lastIndex) {
                Divider(Modifier
                    .animateItemPlacement(animationSpec)
                    .fillMaxWidth())
            } else if (index == entities.lastIndex) {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }
}

@Composable
private fun SystemFontInfoItemList(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    entities: List<FontInfoWithState>,
    onDeleteMenuClicked: (FontInfoWithState) -> Unit,
) {
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
            ) {
                onDeleteMenuClicked.invoke(it)
            }
            if (index < entities.lastIndex) {
                Divider(Modifier
                    .animateItemPlacement(animationSpec)
                    .fillMaxWidth())
            } else if (index == entities.lastIndex) {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }
}

@Composable
private fun EnableMagiskModuleLayout() {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp)
            .alpha(ContentAlpha.medium),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Warning, contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = stringResource(id = R.string.quote_fonts_magisk_module_needed_title),
                style = MaterialTheme.typography.titleLarge)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(id = R.string.quote_fonts_magisk_module_needed_message),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = {
            context.startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse(Urls.GITHUB_QUOTELOCK_CUSTOM_FONTS_RELEASE)))
        },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(text = stringResource(id = R.string.download))
        }
    }
}
