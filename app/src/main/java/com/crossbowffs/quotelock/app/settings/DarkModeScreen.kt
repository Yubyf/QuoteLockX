@file:OptIn(ExperimentalLayoutApi::class)

package com.crossbowffs.quotelock.app.settings

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.integerArrayResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.tooling.preview.Preview
import com.crossbowffs.quotelock.ui.components.ConfigsAppBar
import com.crossbowffs.quotelock.ui.components.RadioButtonItemList
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import com.yubyf.quotelockx.R
import org.koin.androidx.compose.navigation.koinNavViewModel

@Composable
fun DarkModeRoute(
    modifier: Modifier = Modifier,
    viewModel: DarkModeViewModel = koinNavViewModel(),
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState
    DarkModeScreen(
        modifier = modifier,
        uiState = uiState,
        onDarkModeSelected = viewModel::setNightMode,
        onBack = onBack
    )
}

@Composable
fun DarkModeScreen(
    modifier: Modifier = Modifier,
    uiState: DarkModeUiState,
    onDarkModeSelected: (Int) -> Unit = {},
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
                .consumeWindowInsets(padding)
                .fillMaxWidth(),
            entries = stringArrayResource(id = R.array.dark_mode_entries),
            entryValues = values.toTypedArray(),
            selectedItemIndex = values.indexOf(uiState.nightMode).coerceAtLeast(0),
            onItemSelected = { _, item -> onDarkModeSelected(item) }
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
            DarkModeScreen(uiState = DarkModeUiState(AppCompatDelegate.MODE_NIGHT_NO))
        }
    }
}