@file:OptIn(ExperimentalMaterial3Api::class)

package com.crossbowffs.quotelock.app.about

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.crossbowffs.quotelock.consts.Urls
import com.crossbowffs.quotelock.data.version.UpdateInfo
import com.crossbowffs.quotelock.ui.components.AboutAppBar
import com.crossbowffs.quotelock.ui.components.ContentAlpha
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import com.crossbowffs.quotelock.utils.DownloadState
import com.crossbowffs.quotelock.utils.DownloadState.Start.progress
import com.crossbowffs.quotelock.utils.installApk
import com.yubyf.quotelockx.BuildConfig
import com.yubyf.quotelockx.R
import io.noties.markwon.Markwon
import io.noties.markwon.utils.NoCopySpannableFactory
import kotlinx.coroutines.launch
import org.koin.androidx.compose.navigation.koinNavViewModel

private val ItemHeight = 56.dp

@Composable
fun AboutRoute(
    modifier: Modifier = Modifier,
    viewModel: AboutViewModel = koinNavViewModel(),
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState
    val context = LocalContext.current
    AboutScreen(
        modifier = modifier,
        uiState = uiState,
        onDownload = viewModel::fetchUpdateFile,
        onPaused = viewModel::pauseDownload,
        onInstall = context::installApk,
        onBack = onBack
    )
}

@Composable
fun AboutScreen(
    modifier: Modifier = Modifier,
    uiState: AboutUiState,
    onDownload: (UpdateInfo.RemoteUpdate) -> Unit = {},
    onPaused: () -> Unit = {},
    onInstall: (String) -> Unit = {},
    onBack: () -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { snackbarData: SnackbarData ->
                    Card(
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .wrapContentSize(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.inverseSurface,
                            contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                        ),
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Text(
                            text = snackbarData.visuals.message,
                            modifier = Modifier
                                .padding(8.dp)
                                .alpha(ContentAlpha.high),
                            fontSize = MaterialTheme.typography.labelLarge.fontSize
                        )
                    }
                }
            )
        },
        topBar = { AboutAppBar(onBack = onBack) }
    ) { padding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(state = scrollState)
        ) {
            val easterMessage = stringResource(id = R.string.easter_egg)
            Spacer(modifier = Modifier.height(32.dp))
            Logo(modifier = Modifier.align(alignment = Alignment.CenterHorizontally)) {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = easterMessage,
                        duration = SnackbarDuration.Short
                    )
                }
            }
            Spacer(modifier = Modifier.height(64.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                if (uiState.updateInfo != UpdateInfo.NoUpdate) {
                    NewVersion(uiState.updateInfo, onDownload, onPaused, onInstall)
                    Spacer(modifier = Modifier.height(24.dp))
                }
                Repo()
                Spacer(modifier = Modifier.height(24.dp))
                Developers(stringResource(id = R.string.about_developers), uiState.developers)
                Spacer(modifier = Modifier.height(24.dp))
                Developers(stringResource(id = R.string.about_translators), uiState.translators)
                Spacer(modifier = Modifier.height(24.dp))
                QuoteProviders(uiState.quoteProviders)
                Spacer(modifier = Modifier.height(24.dp))
                Libraries(uiState.libraries)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun Logo(modifier: Modifier = Modifier, onEasterEgg: () -> Unit = {}) {
    var logoTapCount by remember { mutableStateOf(0) }
    var logoTapTimestamp by remember { mutableStateOf(0L) }
    val haptic = LocalHapticFeedback.current
    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (System.currentTimeMillis() - logoTapTimestamp < 500) logoTapCount++
                else logoTapCount = 1
                logoTapTimestamp = System.currentTimeMillis()
                if (logoTapCount == 7) {
                    logoTapCount = 0
                    logoTapTimestamp = 0L
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onEasterEgg()
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = R.drawable.ic_quotelockx,
            contentDescription = "Logo",
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.quotelockx),
            fontSize = MaterialTheme.typography.headlineSmall.fontSize,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(id = R.string.pref_about_version)
                    + " ${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})",
            fontSize = MaterialTheme.typography.bodyMedium.fontSize
        )
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun NewVersion(
    updateInfo: UpdateInfo,
    onDownload: (UpdateInfo.RemoteUpdate) -> Unit = {},
    onPaused: () -> Unit = {},
    onInstall: (String) -> Unit = {},
) {
    @Composable
    fun DownloadProgress(text: String, progress: Int, showProgress: Boolean = true) {
        val progressColor = MaterialTheme.colorScheme.primary
        val textColor = MaterialTheme.colorScheme.primary
        val textMeasurer = rememberTextMeasurer()
        val progressText = "$text ${if (showProgress) " $progress%" else ""}"
        val fontSize = MaterialTheme.typography.titleMedium.fontSize
        val textSize = textMeasurer.measure(
            progressText,
            TextStyle(fontSize = fontSize, fontWeight = FontWeight.Bold)
        )
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(ItemHeight)
        ) {
            with(drawContext.canvas.nativeCanvas) {
                val checkPoint = saveLayer(null, null)
                drawText(
                    textMeasurer = textMeasurer,
                    text = progressText,
                    style = TextStyle(
                        fontSize = fontSize,
                        color = textColor,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    ),
                    topLeft = Offset(
                        x = size.width / 2 - textSize.size.width / 2,
                        y = size.height / 2 - textSize.size.height / 2
                    )
                )
                drawRect(
                    color = progressColor,
                    size = size.copy(width = size.width * (progress / 100F)),
                    blendMode = BlendMode.SrcOut
                )
                restoreToCount(checkPoint)
            }
        }
    }

    var showChangelogDialog by remember { mutableStateOf(false) }

    AboutCard(emphasize = true, onClick = {
        when (updateInfo) {
            is UpdateInfo.NoUpdate -> {}

            is UpdateInfo.RemoteUpdate -> {
                when (updateInfo.downloadState) {
                    is DownloadState.Idle -> {
                        if (updateInfo.changelog.isNotBlank()) {
                            showChangelogDialog = true
                        }
                    }

                    is DownloadState.Downloading -> onPaused()

                    else -> onDownload(updateInfo)
                }
            }

            else -> {
                if (updateInfo.changelog.isNotBlank()) {
                    showChangelogDialog = true
                }
            }
        }
    }) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .height(ItemHeight),
            contentAlignment = Alignment.Center
        ) {
            when {
                updateInfo is UpdateInfo.LocalUpdate -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.about_install_new_version),
                            fontSize = MaterialTheme.typography.titleMedium.fontSize,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                updateInfo is UpdateInfo.RemoteUpdate &&
                        (updateInfo.downloadState != DownloadState.Idle
                                && updateInfo.downloadState != DownloadState.End) -> {
                    DownloadProgress(
                        when (updateInfo.downloadState) {
                            is DownloadState.Pause -> stringResource(id = R.string.about_download_paused)
                            is DownloadState.Error -> stringResource(id = R.string.about_download_error)
                            else -> stringResource(id = R.string.about_downloading)
                        },
                        (updateInfo.downloadState.progress * 100).toInt(),
                        updateInfo.downloadState !is DownloadState.Pause
                                && updateInfo.downloadState !is DownloadState.Error
                    )
                }

                else -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = stringResource(R.string.about_new_version),
                            fontSize = MaterialTheme.typography.titleMedium.fontSize,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Spacer(modifier = Modifier.weight(1F))
                        Text(
                            text = updateInfo.versionName,
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.alpha(ContentAlpha.medium)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                }
            }
        }
    }

    if (showChangelogDialog && updateInfo !is UpdateInfo.NoUpdate && updateInfo.changelog.isNotBlank()) {
        val markwon = Markwon.create(LocalContext.current);
        val scrollState = rememberScrollState()
        AlertDialog(
            onDismissRequest = { showChangelogDialog = false },
            text = {
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .verticalScroll(scrollState),
                    factory = { context ->
                        TextView(context).apply {
                            movementMethod = LinkMovementMethod.getInstance()
                            setSpannableFactory(NoCopySpannableFactory.getInstance())
                            markwon.setMarkdown(this, updateInfo.changelog)
                        }
                    })
            },
            confirmButton = {
                val pendingInstall = updateInfo is UpdateInfo.LocalUpdate
                TextButton(onClick = {
                    showChangelogDialog = false
                    if (pendingInstall) {
                        onInstall(updateInfo.url)
                    } else {
                        onDownload(updateInfo as UpdateInfo.RemoteUpdate)
                    }
                }) {
                    Text(
                        text = stringResource(
                            id = if (pendingInstall) R.string.install else R.string.download
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangelogDialog = false }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun Repo() {
    val context = LocalContext.current
    AboutCard(onClick = {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(Urls.GITHUB_QUOTELOCK)
            )
        )
    }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = ItemHeight)
                .padding(top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = stringResource(id = R.string.pref_about_github),
                fontSize = MaterialTheme.typography.titleMedium.fontSize
            )
            Spacer(modifier = Modifier.width(8.dp))
            Spacer(modifier = Modifier.weight(1F))
            Text(
                text = "github.com/Yubyf/QuoteLockX",
                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                modifier = Modifier.alpha(ContentAlpha.medium)
            )
            Spacer(modifier = Modifier.width(16.dp))
        }
    }
}

@Composable
fun Developers(title: String, developers: List<Developer>) {
    AboutCard {
        val placeholder = painterResource(id = R.mipmap.ic_github_identicon)
        val context = LocalContext.current
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp)
        ) {
            Text(
                text = title,
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                modifier = Modifier.padding(start = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            developers.forEachIndexed { index, developer ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ItemHeight)
                        .clickable(enabled = developer.profileLink != null) {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    developer.profileLink
                                )
                            )
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(modifier = Modifier.width(16.dp))
                    AsyncImage(
                        model = developer.avatarUrl,
                        contentDescription = developer.name,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape),
                        placeholder = placeholder,
                        error = placeholder,
                        fallback = placeholder
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = developer.name,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize
                    )
                    developer.badgeRes?.let {
                        Spacer(modifier = Modifier.width(4.dp))
                        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.outline) {
                            Box(
                                modifier = Modifier
                                    .heightIn(min = 18.dp)
                                    .border(
                                        width = Dp.Hairline,
                                        color = LocalContentColor.current,
                                        shape = RoundedCornerShape(50)
                                    )
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(id = it),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
                if (index != developers.lastIndex) {
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp),
                        thickness = Dp.Hairline
                    )
                }
            }
        }
    }
}

@Composable
private fun QuoteProviders(providers: List<QuoteProvider>) {
    AboutCard {
        val context = LocalContext.current
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp)
        ) {
            Text(
                text = stringResource(id = R.string.about_quote_providers),
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                modifier = Modifier.padding(start = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            providers.forEachIndexed { index, provider ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ItemHeight)
                        .clickable(enabled = provider.link != null) {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    provider.link
                                )
                            )
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(modifier = Modifier.width(16.dp))
                    AsyncImage(
                        model = provider.logoRes,
                        contentDescription = provider.name,
                        modifier = Modifier
                            .size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = provider.name,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize
                    )
                }
                if (index != providers.lastIndex) {
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp),
                        thickness = Dp.Hairline
                    )
                }
            }
        }
    }
}

@Composable
fun Libraries(libraries: List<Library>) {
    var showLibrariesDialog by remember {
        mutableStateOf(false)
    }
    AboutCard(onClick = { showLibrariesDialog = true }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = ItemHeight)
                .padding(top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = stringResource(id = R.string.pref_about_libraries),
                fontSize = MaterialTheme.typography.titleMedium.fontSize
            )
        }
    }

    if (showLibrariesDialog) {
        var containerWidth by remember {
            mutableStateOf(0)
        }
        AlertDialog(
            onDismissRequest = { showLibrariesDialog = false },
            modifier = Modifier.onGloballyPositioned { coordinates ->
                containerWidth = coordinates.size.width
            },
            confirmButton = {
                TextButton(onClick = { showLibrariesDialog = false }) {
                    Text(text = stringResource(id = R.string.close))
                }
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            ),
            title = { Text(text = stringResource(id = R.string.pref_about_libraries)) },
            text = {
                val context = LocalContext.current
                Column(
                    modifier = Modifier
                        // Make the item fill the max width in the Dialog
                        // to ensure the ripple effect can be fully rendered
                        .requiredWidth(with(LocalDensity.current) { containerWidth.toDp() })
                ) {
                    Divider(modifier = Modifier.fillMaxWidth(), thickness = Dp.Hairline)
                    libraries.forEach { provider ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clickable(enabled = provider.link != null) {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, provider.link))
                                },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                // Leave 24dp space from the start of Dialog
                                // since the item was fill the max width in the Dialog
                                modifier = Modifier.padding(start = 24.dp),
                                text = provider.name,
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(modifier = Modifier.fillMaxWidth(), thickness = Dp.Hairline)
                }
            }
        )
    }
}

@Composable
private fun AboutCard(
    emphasize: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val containerColor = if (emphasize) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (emphasize) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    onClick?.let {
        Card(
            onClick = it,
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(0.dp),
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = contentColor
            ),
            shape = MaterialTheme.shapes.small,
            content = content
        )
    } ?: Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = MaterialTheme.shapes.small,
        content = content
    )
}

@Preview(
    name = "About Screen Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "About Screen Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun CollectionScreenPreview() {
    QuoteLockTheme {
        Surface {
            AboutScreen(
                uiState = AboutUiState(
                    updateInfo = UpdateInfo.LocalUpdate(
                        versionName = "3.2.0",
                        versionCode = 28,
                        url = ""
                    ),
                    developers = developers,
                    translators = translators,
                    quoteProviders = providers,
                    libraries = libraries
                )
            )
        }
    }
}