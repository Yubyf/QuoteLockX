@file:OptIn(ExperimentalLayoutApi::class)

package com.crossbowffs.quotelock.app.configs.openai

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.webkit.URLUtil
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowDropUp
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.crossbowffs.quotelock.app.SnackBarEvent
import com.crossbowffs.quotelock.app.configs.openai.OpenAIPrefKeys.PREF_OPENAI_API_HOST_DEFAULT
import com.crossbowffs.quotelock.app.configs.openai.OpenAIPrefKeys.PREF_OPENAI_API_KEY_PREFIX
import com.crossbowffs.quotelock.app.configs.openai.OpenAIPrefKeys.PREF_OPENAI_API_KEY_SUPPORT_LINK
import com.crossbowffs.quotelock.app.configs.openai.OpenAIPrefKeys.PREF_PASSWORD_MASK
import com.crossbowffs.quotelock.app.emptySnackBarEvent
import com.crossbowffs.quotelock.data.AsyncResult
import com.crossbowffs.quotelock.data.api.contextString
import com.crossbowffs.quotelock.ui.components.ConfigsAppBar
import com.crossbowffs.quotelock.ui.components.ContentAlpha
import com.crossbowffs.quotelock.ui.components.PREFERENCE_ITEM_HORIZONTAL_PADDING
import com.crossbowffs.quotelock.ui.components.PREFERENCE_SINGLE_LINE_ITEM_HEIGHT
import com.crossbowffs.quotelock.ui.components.PreferenceItem
import com.crossbowffs.quotelock.ui.components.PreferenceTitle
import com.crossbowffs.quotelock.ui.components.SecurityTextToolbar
import com.crossbowffs.quotelock.ui.components.SegmentedControl
import com.crossbowffs.quotelock.ui.components.SegmentedLabelItem
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import com.yubyf.quotelockx.R
import kotlinx.coroutines.launch


@Composable
fun OpenAIRoute(
    modifier: Modifier = Modifier,
    viewModel: OpenAIConfigsViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState
    val uiEvent by viewModel.uiEvent.collectAsState(initial = emptySnackBarEvent)
    OpenAIScreen(
        modifier = modifier,
        uiState = uiState,
        uiEvent = uiEvent,
        onLangChanged = viewModel::selectLanguage,
        onModelChanged = viewModel::selectModel,
        onQuoteTypeChanged = viewModel::selectQuoteType,
        onApiKeyChanged = viewModel::setApiKey,
        onApiHostChanged = viewModel::setApiHost,
        onValidate = viewModel::validate,
        snackBarShown = viewModel::snackBarShown,
        onBack = onBack
    )
}

@Composable
fun OpenAIScreen(
    modifier: Modifier = Modifier,
    uiState: OpenAIUiState = OpenAIUiState(),
    uiEvent: SnackBarEvent = emptySnackBarEvent,
    onLangChanged: (String) -> Unit = {},
    onModelChanged: (String) -> Unit = {},
    onQuoteTypeChanged: (Int) -> Unit = {},
    onApiKeyChanged: (String) -> Unit = {},
    onApiHostChanged: (String?) -> Unit = {},
    onValidate: () -> Unit = {},
    snackBarShown: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val configs = uiState.openAIConfigs
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            ConfigsAppBar(titleRes = R.string.module_openai_config_label, onBack = onBack)
        }
    ) { padding ->
        uiEvent.message?.let {
            val messageText = it
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = messageText.contextString(context),
                    duration = uiEvent.duration,
                    actionLabel = uiEvent.actionText.contextString(context)
                )
            }
            snackBarShown()
        }
        Column(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(padding)
                .consumeWindowInsets(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            val hintColor = MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.medium)
            var languageExpanded by remember { mutableStateOf(false) }
            val supportedLangs = stringArrayResource(id = R.array.openai_langs)
            PreferenceItem(
                title = stringResource(id = R.string.module_openai_quote_language),
                info = {
                    Row(
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(PREFERENCE_SINGLE_LINE_ITEM_HEIGHT),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = configs.language,
                            color = hintColor
                        )
                        Icon(
                            imageVector = if (languageExpanded) Icons.Rounded.ArrowDropUp else Icons.Rounded.ArrowDropDown,
                            contentDescription = "Expand language list",
                            tint = hintColor
                        )
                        DropdownMenu(
                            expanded = languageExpanded,
                            onDismissRequest = { languageExpanded = false },
                        ) {
                            supportedLangs.forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text(text = lang) },
                                    onClick = {
                                        if (lang != configs.language) {
                                            onLangChanged(lang)
                                        }
                                        languageExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }) {
                languageExpanded = !languageExpanded
            }
            var modelExpanded by remember { mutableStateOf(false) }
            val models = stringArrayResource(id = R.array.openai_models)
            PreferenceItem(
                title = stringResource(id = R.string.module_openai_api_model),
                info = {
                    Row(
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(PREFERENCE_SINGLE_LINE_ITEM_HEIGHT),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = configs.model,
                            color = hintColor
                        )
                        Icon(
                            imageVector = if (modelExpanded) Icons.Rounded.ArrowDropUp else Icons.Rounded.ArrowDropDown,
                            contentDescription = "Expand model list",
                            tint = hintColor
                        )
                        DropdownMenu(
                            expanded = modelExpanded,
                            onDismissRequest = { modelExpanded = false },
                        ) {
                            models.forEach { model ->
                                DropdownMenuItem(
                                    text = { Text(text = model) },
                                    onClick = {
                                        if (model != configs.model) {
                                            onModelChanged(model)
                                        }
                                        modelExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }) {
                modelExpanded = !modelExpanded
            }
            PreferenceItem(
                title = stringResource(id = R.string.module_openai_quote_type),
                info = {
                    val quoteTypes = stringArrayResource(id = R.array.openai_quote_type)
                    SegmentedControl(
                        items = quoteTypes.map { SegmentedLabelItem(label = it) },
                        selectedItemIndex = configs.quoteType,
                        onItemSelection = onQuoteTypeChanged
                    )
                })
            PreferenceTitle(R.string.module_openai_api_key)
            var textApiKey by rememberSaveable { mutableStateOf(configs.apiKey.orEmpty()) }
            val apiKeyError =
                textApiKey.length >= PREF_OPENAI_API_KEY_PREFIX.length && !textApiKey.startsWith(
                    PREF_OPENAI_API_KEY_PREFIX
                ) || textApiKey.length < PREF_OPENAI_API_KEY_PREFIX.length && !PREF_OPENAI_API_KEY_PREFIX.startsWith(
                    textApiKey
                )
            CompositionLocalProvider(
                LocalTextToolbar provides SecurityTextToolbar(LocalView.current)
            ) {
                OutlinedTextField(
                    value = textApiKey,
                    onValueChange = { textApiKey = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PREFERENCE_ITEM_HORIZONTAL_PADDING)
                        .onFocusChanged {
                            if (!it.isFocused && !apiKeyError) {
                                onApiKeyChanged(textApiKey)
                            }
                        },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                    }),
                    supportingText = {
                        Box(modifier = Modifier
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(PREF_OPENAI_API_KEY_SUPPORT_LINK)
                                    )
                                )
                            }) {
                            Text(
                                text = stringResource(id = R.string.module_openai_api_key_hint),
                                modifier = Modifier.padding(horizontal = 8.dp),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    },
                    placeholder = {
                        Text(
                            PREF_OPENAI_API_KEY_PREFIX.plus(
                                "*".toString().repeat(20)
                            ),
                            color = hintColor
                        )
                    },
                    isError = apiKeyError,
                    visualTransformation = SkPasswordVisualTransformation(),
                    singleLine = true,
                )
            }
            PreferenceTitle(R.string.module_openai_api_host)
            var textHost by rememberSaveable { mutableStateOf(configs.apiHost.orEmpty()) }
            var textHostPrefix by rememberSaveable { mutableStateOf(false) }
            OutlinedTextField(
                value = textHost,
                onValueChange = {
                    textHost = it
                    textHostPrefix = textHost.isNotBlank() && !URLUtil.isNetworkUrl(textHost)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PREFERENCE_ITEM_HORIZONTAL_PADDING)
                    .onFocusChanged {
                        if (!it.isFocused) {
                            onApiHostChanged(textHost)
                        }
                    },
                prefix = {
                    if (textHostPrefix) {
                        Text(
                            text = "https://",
                            color = hintColor
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                }),
                placeholder = {
                    Text(
                        PREF_OPENAI_API_HOST_DEFAULT,
                        color = hintColor
                    )
                },
                singleLine = true,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                uiState.openAIAccount?.takeIf { it.apiKey == configs.apiKey }?.let { account ->
                    Row(
                        modifier = Modifier
                            .padding(start = PREFERENCE_ITEM_HORIZONTAL_PADDING, top = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MonetizationOn,
                            modifier = Modifier.size(16.dp),
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(
                                id = R.string.module_openai_usage,
                                account.usageUsd,
                                account.hardLimitUsd
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Normal
                        )
                    }
                } ?: Spacer(modifier = Modifier.weight(1f, true))
                OutlinedButton(
                    onClick = {
                        focusManager.clearFocus()
                        onValidate()
                    },
                    modifier = Modifier
                        .padding(horizontal = PREFERENCE_ITEM_HORIZONTAL_PADDING, vertical = 16.dp),
                    enabled = uiState.validateResult !is AsyncResult.Loading
                ) {
                    AnimatedVisibility(visible = uiState.validateResult != null) {
                        Row {
                            when (uiState.validateResult) {
                                is AsyncResult.Success -> {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = "Validate success",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                is AsyncResult.Error -> {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Validate error",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }

                                is AsyncResult.Loading -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        strokeCap = StrokeCap.Round
                                    )
                                }

                                null -> {}
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                    Text(text = stringResource(id = R.string.module_openai_api_validate))
                }
            }
        }
    }
}

private class SkPasswordVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val maskedText = text.text.let {
            if (it.length <= PREF_OPENAI_API_KEY_PREFIX.length) it
            else {
                val prefix = it.substring(0, PREF_OPENAI_API_KEY_PREFIX.length)
                prefix + PREF_PASSWORD_MASK.toString().repeat(text.text.length - prefix.length)
            }
        }
        return TransformedText(AnnotatedString(maskedText), OffsetMapping.Identity)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is SkPasswordVisualTransformation
    }

    override fun hashCode(): Int {
        return PREF_PASSWORD_MASK.hashCode()
    }
}

@Preview(
    name = "OpenAI Screen Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "OpenAI Screen Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun OpenAIScreenPreview() {
    QuoteLockTheme {
        Surface {
            OpenAIScreen()
        }
    }
}