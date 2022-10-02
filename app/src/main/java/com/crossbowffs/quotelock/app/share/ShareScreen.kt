@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.crossbowffs.quotelock.app.share

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.crossbowffs.quotelock.app.detail.shareImage
import com.crossbowffs.quotelock.consts.PREF_SHARE_IMAGE_WATERMARK_PADDING
import com.crossbowffs.quotelock.data.drawShareCard
import com.crossbowffs.quotelock.data.shareBounds
import com.crossbowffs.quotelock.ui.components.ShareAppBar
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import com.yubyf.quotelockx.R
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun ShareRoute(
    modifier: Modifier = Modifier,
    viewModel: ShareViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val uiState: ShareUiState by viewModel.uiState
    val uiEvent by viewModel.uiEvent.collectAsState(initial = null)
    ShareScreen(modifier = modifier,
        uiEvent = uiEvent,
        uiState = uiState,
        onSaveCard = viewModel::saveQuoteCard,
        onShareCard = viewModel::shareQuoteCard,
        uiEventHandled = viewModel::uiEventHandled,
        onBack = onBack)
}

@Composable
fun ShareScreen(
    modifier: Modifier = Modifier,
    uiEvent: ShareUiEvent?,
    uiState: ShareUiState,
    onSaveCard: (Color, Color, Boolean) -> Unit,
    onShareCard: (Color, Color, Boolean) -> Unit,
    uiEventHandled: () -> Unit,
    onBack: () -> Unit = {},
) {
    val systemDarkMode = isSystemInDarkTheme()
    var darkMode by remember {
        mutableStateOf(false)
    }
    var watermark by remember {
        mutableStateOf(true)
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            ShareAppBar(onDarkModeChecked = { darkMode = it },
                onWatermarkChecked = { watermark = it },
                onBackPressed = onBack)
        }
    ) { internalPadding ->
        uiEvent?.let { event ->
            when (event) {
                is ShareUiEvent.ShareFile -> {
                    event.imageFile?.let { file ->
                        LocalContext.current.shareImage(file)
                        uiEventHandled()
                    }
                }
                is ShareUiEvent.SaveFile -> {
                    event.snackbar.message?.let {
                        val messageText = it
                        scope.launch {
                            snackbarHostState.showSnackbar(messageText)
                        }
                        uiEventHandled()
                    }
                }
            }
        }
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(internalPadding)
                .consumedWindowInsets(internalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            var containerColor: Color by remember {
                mutableStateOf(Color.Unspecified)
            }
            var contentColor: Color by remember {
                mutableStateOf(Color.Unspecified)
            }
            Box(modifier = Modifier
                .fillMaxWidth()
                .weight(1F)
                .padding(16.dp),
                contentAlignment = Alignment.Center) {
                QuoteLockTheme(useDarkTheme = darkMode) {
                    Layout(modifier = Modifier
                        .wrapContentSize()
                        .animateContentSize(),
                        content = {
                            containerColor = QuoteLockTheme.quotelockColors.quoteCardSurface
                            contentColor = QuoteLockTheme.quotelockColors.quoteCardOnSurface
                            val watermarkIcon = ContextCompat.getDrawable(LocalContext.current,
                                R.drawable.ic_quotelockx)
                            val watermarkText = stringResource(R.string.quotelockx)
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawIntoCanvas { canvas ->
                                    uiState.snapshot?.takeIf { it.shareBounds != null }
                                        ?.let {
                                            it.shareBounds?.let { shareBounds ->
                                                val bounds =
                                                    shareBounds.copy(height = shareBounds.height + if (watermark) PREF_SHARE_IMAGE_WATERMARK_PADDING else 0F)
                                                if (size.width < bounds.width || size.height < bounds.height) {
                                                    val scale = min(size.width / bounds.width,
                                                        size.height / bounds.height)
                                                    canvas.scale(scale, scale)
                                                }
                                                it.drawShareCard(canvas.nativeCanvas,
                                                    containerColor,
                                                    contentColor,
                                                    systemDarkMode,
                                                    if (watermark) watermarkIcon else null,
                                                    watermarkText)
                                            }
                                        }
                                }
                            }
                        }
                    ) { measurables, constraints ->
                        require(measurables.size == 1)
                        val placeable = measurables.first().measure(constraints)
                        val size = uiState.snapshot?.shareBounds?.let { shareBounds ->
                            val bounds =
                                shareBounds.copy(height = shareBounds.height
                                        + if (watermark) PREF_SHARE_IMAGE_WATERMARK_PADDING else 0F)
                            val scale = if (constraints.maxWidth < bounds.width
                                || constraints.maxHeight < bounds.height
                            ) {
                                min(constraints.maxWidth / bounds.width,
                                    constraints.maxHeight / bounds.height)
                            } else 1F
                            val width = if (constraints.hasFixedWidth) {
                                constraints.maxWidth
                            } else {
                                (shareBounds.width * scale).roundToInt()
                            }
                            val height = if (constraints.hasFixedHeight) {
                                constraints.maxHeight
                            } else {
                                ((shareBounds.height + if (watermark) PREF_SHARE_IMAGE_WATERMARK_PADDING else 0F) * scale)
                                    .roundToInt()
                            }
                            IntSize(width, height)
                        } ?: IntSize(0, 0)

                        layout(size.width, size.height) {
                            placeable.placeRelative(0, 0)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Divider(modifier = Modifier.fillMaxWidth(), thickness = Dp.Hairline)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.2F),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilledIconButton(modifier = Modifier.size(72.dp),
                    onClick = { onSaveCard(containerColor, contentColor, watermark) }) {
                    Icon(Icons.Rounded.Download,
                        contentDescription = stringResource(id = R.string.save),
                        modifier = Modifier.size(36.dp))
                }
                FilledIconButton(modifier = Modifier.size(72.dp),
                    onClick = { onShareCard(containerColor, contentColor, watermark) }) {
                    Icon(Icons.Rounded.Share,
                        contentDescription = stringResource(id = R.string.quote_image_share),
                        modifier = Modifier.size(36.dp))
                }
            }
        }
    }
}