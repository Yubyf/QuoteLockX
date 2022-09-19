@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)

package com.crossbowffs.quotelock.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.crossbowffs.quotelock.app.font.FontInfo
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import com.crossbowffs.quotelock.utils.loadComposeFontWithSystem
import com.yubyf.quotelockx.R

private val LIST_DIALOG_ITEM_HEIGHT = 48.dp

@Composable
fun LoadingDialog(
    message: String?,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    onDismiss: () -> Unit = {},
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = dismissOnBackPress,
            dismissOnClickOutside = dismissOnClickOutside
        )
    ) {
        LoadingLayout(Modifier, message)
    }
}

@Composable
fun LoadingLayout(modifier: Modifier = Modifier, message: String? = null) {
    Surface(
        modifier = modifier,
        shape = AlertDialogDefaults.shape,
        color = AlertDialogDefaults.containerColor,
        tonalElevation = AlertDialogDefaults.TonalElevation,
        shadowElevation = AlertDialogDefaults.TonalElevation,
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator()
            if (message != null) {
                Text(
                    text = message,
                    modifier = Modifier
                        .padding(start = 24.dp)
                        .fillMaxWidth(),
                    color = AlertDialogDefaults.textContentColor,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    fontStyle = MaterialTheme.typography.bodyMedium.fontStyle,
                )
            }
        }
    }
}

@Composable
fun CustomQuoteEditDialog(
    quoteId: Long = -1,
    quote: QuoteData? = null,
    onConfirm: (Long, String, String) -> Unit = { _, _, _ -> },
    onDismiss: () -> Unit = {},
) {
    var text by rememberSaveable { mutableStateOf(quote?.quoteText.orEmpty()) }
    var source by rememberSaveable { mutableStateOf(quote?.quoteSource.orEmpty()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        ),
        title = { Text(text = stringResource(id = R.string.module_custom_enter_quote)) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(text = stringResource(id = R.string.module_custom_text)) },
                    maxLines = 2,
                )
                OutlinedTextField(
                    value = source,
                    onValueChange = { source = it },
                    modifier = Modifier.padding(top = 8.dp),
                    label = { Text(text = stringResource(id = R.string.module_custom_source)) },
                    maxLines = 2,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(quoteId, text, source)
                    onDismiss()
                },
                enabled = text.isNotBlank()
            ) {
                Text(text = stringResource(id = R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
    )
}

@Composable
fun <T> ListPreferenceDialog(
    title: String,
    entries: Array<String>,
    entryValues: Array<T>,
    selectedItem: T? = null,
    onItemSelected: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedItemIndex by remember {
        mutableStateOf(entryValues.indexOfFirst { it == selectedItem }
            .coerceIn(minimumValue = 0, maximumValue = entries.lastIndex))
    }
    var containerWidth by remember {
        mutableStateOf(0)
    }
    AlertDialog(
        onDismissRequest = { onDismiss() },
        modifier = Modifier.onGloballyPositioned { coordinates ->
            containerWidth = coordinates.size.width
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        ),
        title = { Text(text = title) },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                itemsIndexed(entries.zip(entryValues)) { index, (entry, value) ->
                    Row(
                        modifier = Modifier
                            .height(LIST_DIALOG_ITEM_HEIGHT)
                            // Make the item fill the max width in the Dialog
                            // to ensure the ripple effect can be fully rendered
                            .requiredWidth(with(LocalDensity.current) { containerWidth.toDp() })
                            .clickable {
                                selectedItemIndex = index
                                onItemSelected(value)
                                onDismiss()
                            },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selectedItemIndex == index,
                            // Leave 24dp space from the start of Dialog
                            // since the item was fill the max width in the Dialog
                            modifier = Modifier.padding(start = 24.dp),
                            onClick = null,
                        )
                        Spacer(modifier = Modifier.width(24.dp))
                        Text(text = entry,
                            color = AlertDialogDefaults.textContentColor,
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.cancel))
            }
        }
    )
}

@Composable
fun <T> MultiSelectListPreferenceDialog(
    title: String,
    entries: Array<String>,
    entryValues: Array<T>,
    selectedItems: Set<T>? = null,
    onItemsSelected: (Set<T>?) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedItemValues by remember {
        mutableStateOf(selectedItems)
    }
    var containerWidth by remember {
        mutableStateOf(0)
    }
    AlertDialog(
        onDismissRequest = { onDismiss() },
        modifier = Modifier.onGloballyPositioned { coordinates ->
            containerWidth = coordinates.size.width
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
        title = { Text(text = title) },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(entries.zip(entryValues)) { (entry, value) ->
                    var checked by remember {
                        mutableStateOf(selectedItemValues?.contains(value) ?: false)
                    }
                    Row(
                        modifier = Modifier
                            .height(LIST_DIALOG_ITEM_HEIGHT)
                            // Make the item fill the max width in the Dialog
                            // to ensure the ripple effect can be fully rendered
                            .requiredWidth(with(LocalDensity.current) { containerWidth.toDp() })
                            .clickable {
                                checked = checked.not()
                                selectedItemValues = if (checked) {
                                    (selectedItemValues?.toMutableSet() ?: mutableSetOf()).apply {
                                        add(value)
                                    }
                                } else {
                                    selectedItemValues
                                        ?.toMutableSet()
                                        ?.apply {
                                            remove(value)
                                        }
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = checked,
                            // Leave 24dp space from the start of Dialog
                            // since the item was fill the max width in the Dialog
                            modifier = Modifier.padding(start = 24.dp),
                            onCheckedChange = null,
                        )
                        Spacer(modifier = Modifier.width(24.dp))
                        Text(text = entry,
                            color = AlertDialogDefaults.textContentColor,
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onItemsSelected(selectedItemValues); onDismiss() }) {
                Text(text = stringResource(id = R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.cancel))
            }
        }
    )
}

@Composable
fun FontListPreferenceDialog(
    title: String,
    fonts: List<FontInfo>,
    selectedItem: String? = null,
    onItemSelected: (String) -> Unit,
    onCustomize: () -> Unit,
    onDismiss: () -> Unit,
) {
    val names = stringArrayResource(id = R.array.default_font_family_entries)
    val paths = stringArrayResource(id = R.array.default_font_family_values)
    val presetFonts = names.zip(paths).map { (name, path) ->
        FontInfo(fileName = name, path = path)
    }
    val allFonts = presetFonts + fonts
    var selectedItemIndex by remember {
        mutableStateOf(allFonts.indexOfFirst { it.path == selectedItem }
            .coerceIn(minimumValue = 0, maximumValue = allFonts.lastIndex))
    }
    var containerWidth by remember {
        mutableStateOf(0)
    }
    AlertDialog(
        onDismissRequest = { onDismiss() },
        modifier = Modifier.onGloballyPositioned { coordinates ->
            containerWidth = coordinates.size.width
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        ),
        title = { Text(text = title) },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                itemsIndexed(allFonts) { index, fontInfo ->
                    Row(
                        modifier = Modifier
                            .height(LIST_DIALOG_ITEM_HEIGHT)
                            // Make the item fill the max width in the Dialog
                            // to ensure the ripple effect can be fully rendered
                            .requiredWidth(with(LocalDensity.current) { containerWidth.toDp() })
                            .clickable {
                                selectedItemIndex = index
                                onItemSelected(fontInfo.path)
                                onDismiss()
                            },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selectedItemIndex == index,
                            // Leave 24dp space from the start of Dialog
                            // since the item was fill the max width in the Dialog
                            modifier = Modifier.padding(start = 24.dp),
                            onClick = null,
                        )
                        Spacer(modifier = Modifier.width(24.dp))
                        val composeFontFamily = loadComposeFontWithSystem(fontInfo.path)
                        val displayName = with(fontInfo) {
                            LocalConfiguration.current.localeName.takeIf { it.isNotBlank() }
                                ?: fileName
                        }
                        Text(text = displayName,
                            color = AlertDialogDefaults.textContentColor,
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                            fontFamily = composeFontFamily
                        )
                    }
                    if (index == presetFonts.lastIndex && fonts.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(thickness = Dp.Hairline)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        },
        confirmButton = {},
        neutralButton = {
            TextButton(onClick = { onCustomize(); onDismiss(); }) {
                Text(text = stringResource(id = R.string.pref_font_family_custom))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.padding(start = 8.dp)) {
                Text(text = stringResource(id = R.string.cancel))
            }
        }
    )
}

/**
 * AlertDialog with neutral button not defined in MaterialDesign3.
 */
@Composable
fun AlertDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    neutralButton: @Composable (() -> Unit),
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    shape: Shape = AlertDialogDefaults.shape,
    containerColor: Color = AlertDialogDefaults.containerColor,
    iconContentColor: Color = AlertDialogDefaults.iconContentColor,
    titleContentColor: Color = AlertDialogDefaults.titleContentColor,
    textContentColor: Color = AlertDialogDefaults.textContentColor,
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
    properties: DialogProperties = DialogProperties(),
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        icon = icon,
        title = title,
        text = text,
        shape = shape,
        containerColor = containerColor,
        iconContentColor = iconContentColor,
        titleContentColor = titleContentColor,
        textContentColor = textContentColor,
        tonalElevation = tonalElevation,
        properties = properties,
        confirmButton = {
            Row {
                neutralButton()
                Spacer(modifier = Modifier.weight(1F))
                dismissButton?.invoke()
                confirmButton()
            }
        },
        dismissButton = null,
    )
}

@Preview(name = "Loading Dialog Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Loading Dialog Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LoadingDialogPreview() {
    QuoteLockTheme {
        Surface {
            LoadingDialog(message = "Loading...") {}
        }
    }
}

@Preview(name = "Custom Quote Dialog Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Custom Quote Dialog Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CustomQuoteDialogPreview() {
    QuoteLockTheme {
        Surface {
            CustomQuoteEditDialog {}
        }
    }
}

@Preview(name = "List Preference Quote Dialog Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "List Preference Quote Dialog Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ListPreferenceDialogPreview() {
    QuoteLockTheme {
        val items = arrayOf(
            "Item 1" to "item1",
            "Item 2" to "item2",
            "Item 3" to "item3",
            "Item 4" to "item4",
            "Item 5" to "item5",
        )
        val entries: Array<String> = items.map { it.first }.toTypedArray()
        val entryValues = items.map { it.second }.toTypedArray()
        Surface {
            Column {
                ListPreferenceDialog(
                    title = "Title",
                    entries = entries,
                    entryValues = entryValues,
                    selectedItem = items[2].second,
                    onItemSelected = {}
                ) {}
            }
        }
    }
}

@Preview(name = "Multi-Select List Preference Quote Dialog Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Multi-Select List Preference Quote Dialog Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MultiSelectListPreferenceDialogPreview() {
    QuoteLockTheme {
        val items = arrayOf(
            "Item 1" to "item1",
            "Item 2" to "item2",
            "Item 3" to "item3",
            "Item 4" to "item4",
            "Item 5" to "item5",
        )
        val entries: Array<String> = items.map { it.first }.toTypedArray()
        val entryValues = items.map { it.second }.toTypedArray()
        Surface {
            Column {
                MultiSelectListPreferenceDialog(
                    title = "Title",
                    entries = entries,
                    entryValues = entryValues,
                    selectedItems = setOf(items[2].second, items[3].second),
                    onItemsSelected = {}
                ) {}
            }
        }
    }
}