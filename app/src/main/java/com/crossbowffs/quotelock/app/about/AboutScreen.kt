@file:OptIn(ExperimentalMaterial3Api::class)

package com.crossbowffs.quotelock.app.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.crossbowffs.quotelock.consts.Urls
import com.crossbowffs.quotelock.ui.components.AboutAppBar
import com.crossbowffs.quotelock.ui.components.ContentAlpha
import com.yubyf.quotelockx.BuildConfig
import com.yubyf.quotelockx.R
import kotlinx.coroutines.launch

@Composable
fun AboutRoute(
    modifier: Modifier = Modifier,
    viewModel: AboutViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState
    AboutScreen(modifier = modifier, uiState = uiState, onBack = onBack)
}

@Composable
fun AboutScreen(
    modifier: Modifier = Modifier,
    uiState: AboutUiState,
    onBack: () -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = { AboutAppBar(onBack = onBack) }
    ) { padding ->
        val scrollState = rememberScrollState()
        Column(modifier = modifier
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
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
            ) {
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
    Column(modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally) {
        AsyncImage(model = R.drawable.ic_quotelockx,
            contentDescription = "Logo",
            modifier = Modifier
                .size(64.dp)
                .clickable(interactionSource = remember { MutableInteractionSource() },
                    indication = null) {
                    logoTapCount++
                    if (logoTapCount == 7) {
                        logoTapCount = 0
                        onEasterEgg()
                    }
                })
        Spacer(modifier = Modifier.height(4.dp))
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

@Composable
fun Repo() {
    val context = LocalContext.current
    AboutCard(onClick = {
        context.startActivity(Intent(Intent.ACTION_VIEW,
            Uri.parse(Urls.GITHUB_QUOTELOCK)))
    }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
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
        Column(modifier = Modifier
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
                        .height(56.dp)
                        .clickable(enabled = developer.profileLink != null) {
                            context.startActivity(Intent(Intent.ACTION_VIEW,
                                developer.profileLink))
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(modifier = Modifier.width(16.dp))
                    AsyncImage(model = developer.avatarUrl,
                        contentDescription = developer.name,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape),
                        placeholder = placeholder,
                        error = placeholder,
                        fallback = placeholder)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = developer.name + when {
                            developer.isMaintainer -> " (${stringResource(id = R.string.about_maintainer)})"
                            developer.isOriginalDeveloper -> " (${stringResource(id = R.string.about_original_developer)})"
                            else -> ""
                        },
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize
                    )
                }
                if (index != developers.lastIndex) {
                    Divider(modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp),
                        thickness = Dp.Hairline)
                }
            }
        }
    }
}

@Composable
private fun QuoteProviders(providers: List<QuoteProvider>) {
    AboutCard {
        val context = LocalContext.current
        Column(modifier = Modifier
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
                        .height(56.dp)
                        .clickable(enabled = provider.link != null) {
                            context.startActivity(Intent(Intent.ACTION_VIEW,
                                provider.link))
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(modifier = Modifier.width(16.dp))
                    AsyncImage(model = provider.logoRes,
                        contentDescription = provider.name,
                        modifier = Modifier
                            .size(32.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = provider.name,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize
                    )
                }
                if (index != providers.lastIndex) {
                    Divider(modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp),
                        thickness = Dp.Hairline)
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
                .heightIn(min = 56.dp)
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
                Column(modifier = Modifier
                    // Make the item fill the max width in the Dialog
                    // to ensure the ripple effect can be fully rendered
                    .requiredWidth(with(LocalDensity.current) { containerWidth.toDp() })) {
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
private fun AboutCard(onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    onClick?.let {
        Card(onClick = it,
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(0.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
            shape = MaterialTheme.shapes.small,
            content = content
        )
    } ?: Card(modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
        shape = MaterialTheme.shapes.small,
        content = content
    )
}