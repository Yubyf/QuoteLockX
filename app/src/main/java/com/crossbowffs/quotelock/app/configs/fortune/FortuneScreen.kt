@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.crossbowffs.quotelock.app.configs.fortune

import android.content.res.Configuration
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumedWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.crossbowffs.quotelock.app.configs.ConfigsViewModel
import com.crossbowffs.quotelock.ui.components.ConfigsAppBar
import com.crossbowffs.quotelock.ui.components.RadioButtonItemList
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import com.yubyf.quotelockx.R

@Composable
fun FortuneRoute(
    modifier: Modifier = Modifier,
    viewModel: ConfigsViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    FortuneScreen(
        modifier = modifier,
        selectedItemIndex = viewModel.loadFortuneCategoryIndex(),
        onItemSelected = { index, item ->
            viewModel.selectFortuneCategory(index, item)
        },
        onBack = onBack
    )
}

@Composable
fun FortuneScreen(
    modifier: Modifier = Modifier,
    selectedItemIndex: Int = 0,
    onItemSelected: (Int, String) -> Unit = { _, _ -> },
    onBack: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            ConfigsAppBar(titleRes = R.string.module_fortune_config_label, onBack = onBack)
        }
    ) { padding ->
        RadioButtonItemList(
            modifier = modifier
                .padding(padding)
                .consumedWindowInsets(padding),
            entries = stringArrayResource(id = R.array.fortune_categories).map {
                it.replaceFirstChar(Char::titlecase)
            }.toTypedArray(),
            entryValues = stringArrayResource(id = R.array.fortune_categories),
            selectedItemIndex = selectedItemIndex,
            onItemSelected = { index, item ->
                onItemSelected(index, item)
            }
        )
    }
}

@Preview(
    name = "Fortune Screen Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(name = "Fortune Screen Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FortuneScreenPreview() {
    QuoteLockTheme {
        Surface {
            FortuneScreen()
        }
    }
}