@file:OptIn(ExperimentalMaterial3Api::class)

package com.crossbowffs.quotelock.ui.components

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
                Icon(painter = painterResource(id = R.drawable.ic_text_style_24dp),
                    contentDescription = "Text style")
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
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun LockscreenStylesAppBar(
    onBack: () -> Unit,
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
                    Icon(Icons.Rounded.Edit,
                        contentDescription = stringResource(id = R.string.module_custom_create_quote))
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
fun HistoryAppBar(
    onBack: () -> Unit,
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
            onClear?.let {
                IconButton(onClick = it) {
                    Icon(Icons.Rounded.Delete,
                        contentDescription = stringResource(id = R.string.clear))
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
fun DetailAppBar(
    onStyle: () -> Unit,
    onBackPressed: () -> Unit,
) {
    CenterAlignedTopAppBar(
        title = { Text(text = stringResource(id = R.string.pref_detail_title)) },
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(Icons.Rounded.Close, contentDescription = stringResource(id = R.string.close))
            }
        },
        actions = {
            IconButton(onClick = onStyle) {
                Icon(painter = painterResource(id = R.drawable.ic_text_style_24dp),
                    contentDescription = "Text style")
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
                Icon(Icons.Rounded.BrandingWatermark,
                    contentDescription = stringResource(id = R.string.quote_card_style_share_watermark),
                    modifier = Modifier.padding(2.dp))
            }
            var darkModeChecked by remember {
                mutableStateOf(false)
            }
            IconToggleButton(checked = darkModeChecked,
                onCheckedChange = { darkModeChecked = it; onDarkModeChecked(it) },
                colors = IconButtonDefaults.iconToggleButtonColors(
                    checkedContentColor = LocalContentColor.current)) {
                if (darkModeChecked) {
                    Icon(Icons.Rounded.LightMode,
                        contentDescription = "Light Mode")
                } else {
                    Icon(Icons.Rounded.DarkMode,
                        contentDescription = "Dark Mode")
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

@Preview(name = "Main App Bar Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Main App Bar Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MainTopAppBarPreview() {
    QuoteLockTheme {
        Surface {
            MainAppBar {}
        }
    }
}

@Preview(name = "Settings Bar Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Settings Bar Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsTopAppBarPreview() {
    QuoteLockTheme {
        Surface {
            SettingsAppBar {}
        }
    }
}

@Preview(name = "About Bar Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "About Bar Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AboutTopAppBarPreview() {
    QuoteLockTheme {
        Surface {
            AboutAppBar {}
        }
    }
}

@Preview(name = "Custom quote App Bar Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Custom quote App Bar Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CustomQuoteTopAppBarPreview() {
    QuoteLockTheme {
        Surface {
            CustomQuoteAppBar({}) {}
        }
    }
}

@Preview(name = "Collection App Bar Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Collection App Bar Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CollectionTopAppBarPreview() {
    QuoteLockTheme {
        Surface {
            CollectionAppBar({}) {}
        }
    }
}

@Preview(name = "History App Bar Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "History App Bar Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HistoryTopAppBarPreview() {
    QuoteLockTheme {
        Surface {
            HistoryAppBar({}) {}
        }
    }
}

@Preview(name = "Detail App Bar Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Detail App Bar Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DetailTopAppBarPreview() {
    QuoteLockTheme {
        Surface {
            DetailAppBar({}, {})
        }
    }
}