@file:OptIn(ExperimentalLayoutApi::class)

package com.crossbowffs.quotelock.app.configs.wikiquote

import android.content.res.Configuration
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.crossbowffs.quotelock.app.configs.wikiquote.WikiquotePrefKeys.PREF_WIKIQUOTE_LANGUAGE_DEFAULT
import com.crossbowffs.quotelock.ui.components.ConfigsAppBar
import com.crossbowffs.quotelock.ui.components.RadioButtonItemList
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import com.yubyf.quotelockx.R

@Composable
fun WikiquoteRoute(
    modifier: Modifier = Modifier,
    viewModel: WikiquoteViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val language by viewModel.language
    WikiquoteScreen(
        modifier = modifier,
        selectedItem = language,
        onItemSelected = { _, item ->
            viewModel.selectLanguage(item)
        },
        onBack = onBack
    )
}

@Composable
fun WikiquoteScreen(
    modifier: Modifier = Modifier,
    selectedItem: String = PREF_WIKIQUOTE_LANGUAGE_DEFAULT,
    onItemSelected: (Int, String) -> Unit = { _, _ -> },
    onBack: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            ConfigsAppBar(titleRes = R.string.module_wiki_quote_config_label, onBack = onBack)
        }
    ) { padding ->
        val supportedLangs = stringArrayResource(id = R.array.wikiquote_langs)
        RadioButtonItemList(
            modifier = modifier
                .padding(padding)
                .consumeWindowInsets(padding),
            stretchToFill = true,
            entries = supportedLangs,
            entryValues = supportedLangs,
            selectedItemIndex = supportedLangs.indexOf(selectedItem).coerceAtLeast(0),
            onItemSelected = { index, item ->
                onItemSelected(index, item)
            }
        )
    }
}

@Preview(
    name = "Wikiquote Screen Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Wikiquote Screen Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun WikiquoteScreenPreview() {
    QuoteLockTheme {
        Surface {
            WikiquoteScreen()
        }
    }
}