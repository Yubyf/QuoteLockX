package com.crossbowffs.quotelock.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme

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

@Preview(name = "Loading Layout Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Loading Layout Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LoadingLayoutPreview() {
    QuoteLockTheme {
        Surface {
            LoadingLayout(message = "Loading...")
        }
    }
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