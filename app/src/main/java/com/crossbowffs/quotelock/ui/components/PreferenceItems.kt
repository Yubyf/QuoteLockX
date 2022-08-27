@file:OptIn(ExperimentalMaterial3Api::class)

package com.crossbowffs.quotelock.ui.components

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
            style = QuoteLockTheme.typography.bodyMedium,
            fontSize = QuoteLockTheme.typography.bodyMedium.fontSize,
            color = QuoteLockTheme.materialColors.primary,
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
    checked: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
    onSwitchChange: ((Boolean) -> Unit)? = null,
) {
    var checkedState by remember { mutableStateOf(checked) }
    Box(contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .heightIn(min = if (summary.isNullOrBlank()) {
                PREFERENCE_SINGLE_LINE_ITEM_HEIGHT
            } else {
                PREFERENCE_DUAL_LINE_ITEM_HEIGHT
            })
            .clickable(enabled = enabled) {
                if (onSwitchChange != null) {
                    checkedState = !checkedState
                    onSwitchChange.invoke(checkedState)
                } else {
                    onClick()
                }
            }
            // FIXME: Use 38% alpha for disabled content since ContentAlpha.disabled is not able in the Material3 library 1.0.0-alpha16.
            // See https://developer.android.com/reference/kotlin/androidx/compose/material/package-summary#LocalContentAlpha()
            .alpha(if (enabled) 1F else 0.38F)
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
                Text(text = title,
                    style = QuoteLockTheme.typography.titleMedium,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Start,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
                if (!summary.isNullOrBlank()) {
                    Text(text = summary,
                        style = QuoteLockTheme.typography.labelLarge,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Start,
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp)
                            // FIXME: Use 60% alpha for subtitle content since ContentAlpha.medium is not able in the Material3 library 1.0.0-alpha16.
                            // See https://developer.android.com/reference/kotlin/androidx/compose/material/package-summary#LocalContentAlpha()
                            .alpha(if (enabled) 0.6F else 1F)
                    )
                }
            }
            onSwitchChange?.let {
                Switch(
                    checked = checkedState,
                    // React switch changes by onClick in parent component
                    onCheckedChange = null,
                )
            }
        }
    }
}

@Composable
fun PreferenceItem(
    modifier: Modifier = Modifier,
    @StringRes titleRes: Int,
    @StringRes summaryRes: Int? = null,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    PreferenceItem(
        modifier = modifier,
        title = stringResource(id = titleRes),
        summary = summaryRes?.let { stringResource(id = summaryRes) },
        enabled = enabled,
        onClick = onClick,
    )
}

@Composable
fun PreferenceItem(
    modifier: Modifier = Modifier,
    title: String,
    summary: String? = null,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    BasePreferenceItem(
        modifier = modifier,
        title = title,
        summary = summary,
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

@Preview(name = "Preference Item Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Preference Item Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreferenceItemPreview() {
    QuoteLockTheme {
        Surface {
            Column {
                PreferenceTitle(title = "Title")
                PreferenceItem(title = "Standard Item", summary = "Summary here") {}
                PreferenceItem(title = "Single Line Item") {}
                SwitchablePreferenceItem(title = "Switchable Item", summary = "Summary here") {}
            }
        }
    }
}