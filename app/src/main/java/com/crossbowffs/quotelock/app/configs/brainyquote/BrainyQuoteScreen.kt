@file:OptIn(ExperimentalLayoutApi::class)

package com.crossbowffs.quotelock.app.configs.brainyquote

import android.content.res.Configuration
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
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
fun BrainyQuoteRoute(
    modifier: Modifier = Modifier,
    viewModel: ConfigsViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    BrainyQuoteScreen(
        modifier = modifier,
        selectedItemIndex = viewModel.loadBrainyQuoteTypeIndex(),
        onItemSelected = { index, item ->
            viewModel.selectBrainyQuoteType(index, item)
        },
        onBack = onBack
    )
}

@Composable
fun BrainyQuoteScreen(
    modifier: Modifier = Modifier,
    selectedItemIndex: Int = 0,
    onItemSelected: (Int, String) -> Unit = { _, _ -> },
    onBack: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            ConfigsAppBar(titleRes = R.string.module_brainy_config_label, onBack = onBack)
        }
    ) { padding ->
        RadioButtonItemList(
            modifier = modifier
                .padding(padding)
                .consumeWindowInsets(padding),
            stretchToFill = true,
            entries = stringArrayResource(id = R.array.brainy_quote_type_entries),
            entryValues = stringArrayResource(id = R.array.brainy_quote_type_values),
            selectedItemIndex = selectedItemIndex,
            onItemSelected = { index, item ->
                onItemSelected(index, item)
            }
        )
    }
}

@Preview(
    name = "Brainy Quote Screen Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(name = "Brainy Quote Screen Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun BrainyQuoteScreenPreview() {
    QuoteLockTheme {
        Surface {
            BrainyQuoteScreen()
        }
    }
}