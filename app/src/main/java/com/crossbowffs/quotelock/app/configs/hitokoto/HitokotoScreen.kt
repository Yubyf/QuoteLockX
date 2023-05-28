@file:OptIn(ExperimentalLayoutApi::class)

package com.crossbowffs.quotelock.app.configs.hitokoto

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
import com.crossbowffs.quotelock.ui.components.MultiSelectItemList
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import com.yubyf.quotelockx.R


@Composable
fun HitokotoRoute(
    modifier: Modifier = Modifier,
    viewModel: ConfigsViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val entryValues = stringArrayResource(id = R.array.hitokoto_type_values)
    val selectedItems = viewModel.loadHitokotoTypesString() ?: run {
        viewModel.loadHitokotoTypeIndex().takeIf { it > 0 }?.let { index ->
            setOf(entryValues[index])
        } ?: emptySet()
    }
    HitokotoScreen(
        modifier = modifier,
        selectedItems = selectedItems,
        onItemsSelected = { items ->
            viewModel.selectHitokotoTypes(items)
        },
        onBack = onBack
    )
}

@Composable
fun HitokotoScreen(
    modifier: Modifier = Modifier,
    selectedItems: Set<String> = emptySet(),
    onItemsSelected: (Set<String>) -> Unit = {},
    onBack: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            ConfigsAppBar(titleRes = R.string.module_hitokoto_config_label, onBack = onBack)
        }
    ) { padding ->
        val entries = stringArrayResource(id = R.array.hitokoto_type_entries)
        val entryValues = stringArrayResource(id = R.array.hitokoto_type_values)
        MultiSelectItemList(
            modifier = modifier
                .padding(padding)
                .consumeWindowInsets(padding),
            entries = entries,
            entryValues = entryValues,
            selectedItems = selectedItems.takeIf { it.isNotEmpty() } ?: setOf(entryValues.first()),
            onItemsSelected = { items ->
                onItemsSelected(items)
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