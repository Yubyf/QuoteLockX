@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.crossbowffs.quotelock.app.configs.hitokoto

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
fun HitokotoRoute(
    modifier: Modifier = Modifier,
    viewModel: ConfigsViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    HitokotoScreen(
        modifier = modifier,
        selectedItemIndex = viewModel.loadHitokotoTypeIndex(),
        onItemSelected = { index, item ->
            viewModel.selectHitokotoType(index, item)
        },
        onBack = onBack
    )
}

@Composable
fun HitokotoScreen(
    modifier: Modifier = Modifier,
    selectedItemIndex: Int = 0,
    onItemSelected: (Int, String) -> Unit = { _, _ -> },
    onBack: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            ConfigsAppBar(titleRes = R.string.module_hitokoto_config_label, onBack = onBack)
        }
    ) { padding ->
        RadioButtonItemList(
            modifier = modifier
                .padding(padding)
                .consumedWindowInsets(padding),
            entries = stringArrayResource(id = R.array.hitokoto_type_entries),
            entryValues = stringArrayResource(id = R.array.hitokoto_type_values),
            selectedItemIndex = selectedItemIndex,
            onItemSelected = { index, item ->
                onItemSelected(index, item)
            }
        )
    }
}

@Preview(
    name = "Hitokoto Screen Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(name = "Hitokoto Screen Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HitokotoScreenPreview() {
    QuoteLockTheme {
        Surface {
            HitokotoScreen()
        }
    }
}