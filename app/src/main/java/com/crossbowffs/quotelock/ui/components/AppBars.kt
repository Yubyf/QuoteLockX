@file:OptIn(ExperimentalMaterial3Api::class)

package com.crossbowffs.quotelock.ui.components

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BrandingWatermark
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import com.yubyf.quotelockx.R

@Composable
fun MainAppBar(
    onStyle: () -> Unit = {},
    onMenuMore: @Composable RowScope.() -> Unit,
) {
    CenterAlignedTopAppBar(
        title = { Text(text = stringResource(id = R.string.quotelockx)) },
        actions = {
            IconButton(onClick = onStyle) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_text_style_24dp),
                    contentDescription = "Text style"
                )
            }
            onMenuMore()
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun SettingsAppBar(
    onBack: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    LargeTopAppBar(
        title = { Text(text = stringResource(id = R.string.settings)) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
        ),
        modifier = Modifier.fillMaxWidth(),
        scrollBehavior = scrollBehavior
    )
}

@Composable
fun AboutAppBar(
    onBack: () -> Unit,
) {
    CenterAlignedTopAppBar(
        title = { },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun LockscreenStylesAppBar(
    onBack: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    LargeTopAppBar(
        title = { Text(text = stringResource(id = R.string.lockscreen_styles)) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
        ),
        modifier = Modifier.fillMaxWidth(),
        scrollBehavior = scrollBehavior
    )
}

@Composable
fun CustomQuoteAppBar(
    onBack: () -> Unit,
    onAdd: (() -> Unit)? = null,
) {
    CenterAlignedTopAppBar(
        title = { Text(text = stringResource(id = R.string.module_custom_activity_label)) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            onAdd?.let {
                IconButton(onClick = it) {
                    Icon(
                        Icons.Rounded.Edit,
                        contentDescription = stringResource(id = R.string.module_custom_create_quote)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun SearchBar(
    keyword: String,
    onClose: () -> Unit,
    onSearch: (String) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var textFieldValue by remember(keyword) {
        mutableStateOf(
            TextFieldValue(
                keyword,
                TextRange(keyword.length)
            )
        )
    }
    BackHandler(true) {
        onClose(); textFieldValue = TextFieldValue()
    }
    DisposableEffect(Unit) {
        focusRequester.requestFocus()
        onDispose { }
    }
    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .height(64.dp),
        value = textFieldValue,
        onValueChange = { textFieldValue = it },
        placeholder = { Text(text = stringResource(R.string.search)) },
        singleLine = true,
        shape = RectangleShape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
        ),
        leadingIcon = {
            Icon(
                Icons.Filled.Search,
                contentDescription = stringResource(id = R.string.search)
            )
        },
        trailingIcon = {
            IconButton(onClick = {
                onClose(); textFieldValue = TextFieldValue()
            }) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = stringResource(id = R.string.close)
                )
            }
        },
        keyboardActions = KeyboardActions(onSearch = {
            focusManager.clearFocus()
            onSearch(textFieldValue.text)
        }),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
    )
}

@Composable
fun HistoryAppBar(
    onBack: () -> Unit,
    onSearch: (() -> Unit)? = null,
    onClear: (() -> Unit)? = null,
) {
    CenterAlignedTopAppBar(
        title = { Text(text = stringResource(id = R.string.pref_history_title)) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            onSearch?.let {
                IconButton(onClick = onSearch) {
                    Icon(
                        Icons.Rounded.Search,
                        contentDescription = stringResource(id = R.string.search)
                    )
                }
            }
            onClear?.let {
                IconButton(onClick = it) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = stringResource(id = R.string.clear)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun CollectionAppBar(
    onBack: () -> Unit,
    onSearch: (() -> Unit)? = null,
    dataRetentionMenu: @Composable RowScope.() -> Unit,
) {
    CenterAlignedTopAppBar(
        title = { Text(text = stringResource(id = R.string.pref_collection_title)) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            onSearch?.let {
                IconButton(onClick = onSearch) {
                    Icon(
                        Icons.Rounded.Search,
                        contentDescription = stringResource(id = R.string.search)
                    )
                }
            }
            dataRetentionMenu()
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun TopAppBarDropdownMenu(
    minWidth: Dp = 180.dp,
    iconContent: @Composable () -> Unit,
    content: @Composable ColumnScope.(Modifier, () -> Unit) -> Unit,
    extraContent: @Composable BoxScope.(Modifier, () -> Unit) -> Unit = { _, _ -> },
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier) {
        IconButton(onClick = { expanded = !expanded }) {
            iconContent()
        }
        val modifier = Modifier.requiredWidthIn(min = minWidth)
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = modifier,
        ) {
            content(modifier) { expanded = false }
        }
        extraContent(modifier) {}
    }
}

@Composable
fun QuoteAppBar(
    onStyle: () -> Unit,
    onBackPressed: () -> Unit,
) {
    CenterAlignedTopAppBar(
        title = { Text(text = stringResource(id = R.string.pref_quote_title)) },
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(Icons.Rounded.Close, contentDescription = stringResource(id = R.string.close))
            }
        },
        actions = {
            IconButton(onClick = onStyle) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_text_style_24dp),
                    contentDescription = "Text style"
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun ShareAppBar(
    cardInDarkMode: Boolean,
    onDarkModeChecked: (Boolean) -> Unit,
    onWatermarkChecked: (Boolean) -> Unit,
    onBackPressed: () -> Unit,
) {
    CenterAlignedTopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(Icons.Rounded.Close, contentDescription = stringResource(id = R.string.close))
            }
        },
        actions = {
            var watermarkChecked by remember {
                mutableStateOf(true)
            }
            IconToggleButton(checked = watermarkChecked,
                onCheckedChange = { watermarkChecked = it; onWatermarkChecked(it) }) {
                Icon(
                    Icons.Rounded.BrandingWatermark,
                    contentDescription = stringResource(id = R.string.quote_card_style_share_watermark),
                    modifier = Modifier.padding(2.dp)
                )
            }
            IconToggleButton(
                checked = cardInDarkMode,
                onCheckedChange = { onDarkModeChecked(it) },
                colors = IconButtonDefaults.iconToggleButtonColors(
                    checkedContentColor = LocalContentColor.current
                )
            ) {
                if (cardInDarkMode) {
                    Icon(
                        Icons.Rounded.LightMode,
                        contentDescription = "Light Mode"
                    )
                } else {
                    Icon(
                        Icons.Rounded.DarkMode,
                        contentDescription = "Dark Mode"
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun ConfigsAppBar(
    @StringRes titleRes: Int,
    onBack: () -> Unit,
) {
    CenterAlignedTopAppBar(
        title = { Text(text = stringResource(id = titleRes)) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun FontManagementAppBar(
    onBack: (() -> Unit)?,
) {
    CenterAlignedTopAppBar(
        title = { Text(text = stringResource(id = R.string.quote_fonts_management_screen_label)) },
        navigationIcon = {
            onBack?.let {
                IconButton(onClick = it) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close")
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun DetailTopBar(
    onBackPressed: () -> Unit,
) {
    CenterAlignedTopAppBar(
        title = { Text(text = stringResource(id = R.string.pref_detail_title)) },
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
            }
        },
        actions = { },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Preview(
    name = "Main App Bar Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Main App Bar Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun MainTopAppBarPreview() {
    QuoteLockTheme {
        Surface {
            MainAppBar {}
        }
    }
}

@Preview(
    name = "Settings Bar Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Settings Bar Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SettingsTopAppBarPreview() {
    QuoteLockTheme {
        Surface {
            SettingsAppBar({})
        }
    }
}

@Preview(
    name = "About Bar Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "About Bar Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun AboutTopAppBarPreview() {
    QuoteLockTheme {
        Surface {
            AboutAppBar {}
        }
    }
}

@Preview(
    name = "Custom quote App Bar Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Custom quote App Bar Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun CustomQuoteTopAppBarPreview() {
    QuoteLockTheme {
        Surface {
            CustomQuoteAppBar({}) {}
        }
    }
}

@Preview(
    name = "Collection App Bar Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Collection App Bar Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun CollectionTopAppBarPreview() {
    QuoteLockTheme {
        Surface {
            CollectionAppBar({}) {}
        }
    }
}

@Preview(
    name = "History App Bar Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "History App Bar Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun HistoryTopAppBarPreview() {
    QuoteLockTheme {
        Surface {
            HistoryAppBar({}, {})
        }
    }
}

@Preview(
    name = "Quote App Bar Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Quote App Bar Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun QuoteTopAppBarPreview() {
    QuoteLockTheme {
        Surface {
            QuoteAppBar({}, {})
        }
    }
}

@Preview(
    name = "Search Bar Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Search Bar Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SearchBarPreview() {
    QuoteLockTheme {
        Surface {
            SearchBar("Search Quotes", {}, {})
        }
    }
}

@Preview(
    name = "Detail App Bar Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Detail App Bar Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun DetailTopAppBarPreview() {
    QuoteLockTheme {
        Surface {
            DetailTopBar {}
        }
    }
}