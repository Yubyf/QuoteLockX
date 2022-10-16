@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.crossbowffs.quotelock.app.settings

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumedWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.integerArrayResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.tooling.preview.Preview
import com.crossbowffs.quotelock.ui.components.ConfigsAppBar
import com.crossbowffs.quotelock.ui.components.RadioButtonItemList
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import com.yubyf.quotelockx.R

@Composable
fun DarkModeRoute(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    DarkModeScreen(
        modifier = modifier,
        selectedItem = AppCompatDelegate.getDefaultNightMode(),
        onBack = onBack
    )
}

@Composable
fun DarkModeScreen(
    modifier: Modifier = Modifier,
    selectedItem: Int? = null,
    onBack: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            ConfigsAppBar(titleRes = R.string.pref_dark_mode_title, onBack = onBack)
        }
    ) { padding ->
        val values = integerArrayResource(id = R.array.dark_mode_values)
        RadioButtonItemList(
            modifier = modifier
                .padding(padding)
                .consumedWindowInsets(padding)
                .fillMaxWidth(),
            entries = stringArrayResource(id = R.array.dark_mode_entries),
            entryValues = values.toTypedArray(),
            selectedItemIndex = selectedItem?.let { values.indexOf(it).coerceAtLeast(0) } ?: 0,
            onItemSelected = { _, item ->
                AppCompatDelegate.setDefaultNightMode(item)
            }
        )
    }
}

@Preview(
    name = "Dark Mode Screen Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark Mode Screen Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun DarkModeScreenPreview() {
    QuoteLockTheme {
        Surface {
            DarkModeScreen()
        }
    }
}