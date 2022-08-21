@file:OptIn(ExperimentalMaterial3Api::class)

package com.crossbowffs.quotelock.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import com.yubyf.quotelockx.R

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
                    fontSize = QuoteLockTheme.typography.bodyMedium.fontSize,
                    fontStyle = QuoteLockTheme.typography.bodyMedium.fontStyle,
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