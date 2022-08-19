@file:OptIn(ExperimentalMaterial3Api::class)

package com.crossbowffs.quotelock.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import com.yubyf.quotelockx.R


@Composable
fun HistoryAppBar(
    onBackPressed: () -> Unit,
    onClearClicked: (() -> Unit)? = null,
) {
    CenterAlignedTopAppBar(
        title = { Text(text = stringResource(id = R.string.pref_history_title)) },
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            onClearClicked?.let {
                IconButton(onClick = it) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Clear History")
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
fun DetailAppBar(
    onBackPressed: () -> Unit,
) {
    CenterAlignedTopAppBar(
        title = { Text(text = stringResource(id = R.string.pref_detail_title)) },
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Preview(name = "History App Bar Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "History App Bar Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HistoryTopAppBarLightPreview() {
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
private fun DetailTopAppBarLightPreview() {
    QuoteLockTheme {
        Surface {
            DetailAppBar {}
        }
    }
}