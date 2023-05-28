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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.os.LocaleListCompat
import com.crossbowffs.quotelock.ui.components.ConfigsAppBar
import com.crossbowffs.quotelock.ui.components.RadioButtonItemList
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import com.yubyf.quotelockx.R

@Composable
fun LanguageRoute(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    LanguageScreen(
        modifier = modifier,
        selectedItem = AppCompatDelegate.getApplicationLocales()[0]?.toLanguageTag(),
        onBack = onBack
    )
}

@Composable
fun LanguageScreen(
    modifier: Modifier = Modifier,
    selectedItem: String? = null,
    onBack: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            ConfigsAppBar(titleRes = R.string.pref_language_title, onBack = onBack)
        }
    ) { padding ->
        val values = arrayOf<String?>(null) + stringArrayResource(id = R.array.lang_values)
        RadioButtonItemList(
            modifier = modifier
                .padding(padding)
                .consumeWindowInsets(padding)
                .fillMaxWidth(),
            entries = arrayOf(stringResource(id = R.string.pref_language_system))
                    + stringArrayResource(id = R.array.lang_entries),
            entryValues = values,
            selectedItemIndex = values.indexOf(selectedItem).coerceAtLeast(0),
            onItemSelected = { _, item ->
                val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(item)
                AppCompatDelegate.setApplicationLocales(appLocale)
            }
        )
    }
}

@Preview(
    name = "Language Screen Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Language Screen Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun LanguageScreenPreview() {
    QuoteLockTheme {
        Surface {
            LanguageScreen()
        }
    }
}