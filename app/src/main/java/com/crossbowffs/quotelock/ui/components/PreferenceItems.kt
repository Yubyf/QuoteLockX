package com.crossbowffs.quotelock.ui.components

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme

private val PREFERENCE_ITEM_HORIZONTAL_PADDING = 24.dp
private val PREFERENCE_TITLE_ITEM_HEIGHT = 48.dp
private val PREFERENCE_DUAL_LINE_ITEM_HEIGHT = 72.dp
private val PREFERENCE_SINGLE_LINE_ITEM_HEIGHT = 56.dp

@Composable
fun PreferenceTitle(@StringRes titleRes: Int) {
    PreferenceTitle(title = stringResource(id = titleRes))
}

@Composable
fun PreferenceTitle(title: String) {
    Box(
        modifier = Modifier
            .heightIn(min = PREFERENCE_TITLE_ITEM_HEIGHT)
            .padding(horizontal = PREFERENCE_ITEM_HORIZONTAL_PADDING)
            .padding(bottom = 8.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier.align(Alignment.BottomStart),
            style = MaterialTheme.typography.bodyMedium,
            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
fun BasePreferenceItem(
    modifier: Modifier = Modifier,
    @StringRes titleRes: Int,
    @StringRes summaryRes: Int? = null,
    checked: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
    onSwitchChange: ((Boolean) -> Unit)? = null,
) {
    BasePreferenceItem(
        modifier = modifier,
        title = stringResource(id = titleRes),
        summary = summaryRes?.let { stringResource(id = summaryRes) },
        checked = checked,
        enabled = enabled,
        onClick = onClick,
        onSwitchChange = onSwitchChange,
    )
}

@Composable
fun BasePreferenceItem(
    modifier: Modifier = Modifier,
    title: String,
    summary: String? = null,
    info: @Composable (RowScope.() -> Unit)? = null,
    checked: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
    onSwitchChange: ((Boolean) -> Unit)? = null,
) {
    var checkedState by remember { mutableStateOf(checked) }
    Box(contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .heightIn(
                min = if (summary.isNullOrBlank()) {
                    PREFERENCE_SINGLE_LINE_ITEM_HEIGHT
                } else {
                    PREFERENCE_DUAL_LINE_ITEM_HEIGHT
                }
            )
            .clickable(enabled = enabled) {
                if (onSwitchChange != null) {
                    checkedState = !checkedState
                    onSwitchChange.invoke(checkedState)
                } else {
                    onClick()
                }
            }
            .alpha(if (enabled) 1F else ContentAlpha.disabled)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PREFERENCE_ITEM_HORIZONTAL_PADDING, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .weight(1f, true),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Start,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
                if (!summary.isNullOrBlank()) {
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Start,
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp)
                            .alpha(if (enabled) 0.6F else ContentAlpha.medium)
                    )
                }
            }
            if (info == null) {
                onSwitchChange?.let {
                    Switch(
                        checked = checkedState,
                        // React switch changes by onClick in parent component
                        onCheckedChange = null,
                    )
                }
            } else {
                info.invoke(this)
            }
        }
    }
}

@Composable
fun PreferenceItem(
    modifier: Modifier = Modifier,
    @StringRes titleRes: Int,
    @StringRes summaryRes: Int? = null,
    info: @Composable (RowScope.() -> Unit)? = null,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    PreferenceItem(
        modifier = modifier,
        title = stringResource(id = titleRes),
        summary = summaryRes?.let { stringResource(id = summaryRes) },
        info = info,
        enabled = enabled,
        onClick = onClick,
    )
}

@Composable
fun PreferenceItem(
    modifier: Modifier = Modifier,
    title: String,
    summary: String? = null,
    info: @Composable (RowScope.() -> Unit)? = null,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    BasePreferenceItem(
        modifier = modifier,
        title = title,
        summary = summary,
        info = info,
        enabled = enabled,
        onClick = onClick,
    )
}

@Composable
fun SwitchablePreferenceItem(
    modifier: Modifier = Modifier,
    @StringRes titleRes: Int,
    @StringRes summaryRes: Int? = null,
    checked: Boolean = false,
    enabled: Boolean = true,
    onSwitchChange: (Boolean) -> Unit,
) {
    SwitchablePreferenceItem(
        modifier = modifier,
        title = stringResource(id = titleRes),
        summary = summaryRes?.let { stringResource(id = summaryRes) },
        enabled = enabled,
        checked = checked,
        onSwitchChange = onSwitchChange,
    )
}

@Composable
fun SwitchablePreferenceItem(
    modifier: Modifier = Modifier,
    title: String,
    summary: String? = null,
    checked: Boolean = false,
    enabled: Boolean = true,
    onSwitchChange: (Boolean) -> Unit,
) {
    BasePreferenceItem(
        modifier = modifier,
        title = title,
        summary = summary,
        enabled = enabled,
        checked = checked,
        onSwitchChange = onSwitchChange
    )
}

@Preview(
    name = "Preference Item Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Preference Item Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreferenceItemPreview() {
    QuoteLockTheme {
        Surface {
            Column {
                PreferenceTitle(title = "Title")
                PreferenceItem(
                    title = "Standard Item",
                    summary = "Summary here",
                    info = {
                        Text(
                            text = "Info here",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            modifier = Modifier.alpha(0.6F)
                        )
                    }
                ) {}
                PreferenceItem(title = "Single Line Item") {}
                SwitchablePreferenceItem(title = "Switchable Item", summary = "Summary here") {}
            }
        }
    }
}