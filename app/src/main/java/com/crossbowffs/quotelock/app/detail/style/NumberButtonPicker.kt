package com.crossbowffs.quotelock.app.detail.style

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.crossbowffs.quotelock.ui.components.ContentAlpha

@Composable
internal fun NumberButtonPicker(
    modifier: Modifier = Modifier,
    value: Int,
    onValueChange: (Int) -> Unit,
    valueRange: ClosedRange<Int> = 0..1,
    step: Int = 1,
    decreaseIcon: @Composable RowScope.() -> Unit,
    increaseIcon: @Composable RowScope.() -> Unit,
) {
    require(step > 0) { "step should be > 0" }

    var currentValue by remember {
        mutableStateOf(value)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedButton(
            onClick = {
                if (currentValue - step >= valueRange.start) {
                    currentValue -= step
                    onValueChange(currentValue)
                }
            },
            enabled = currentValue - step >= valueRange.start,
            modifier = Modifier
                .width(48.dp)
                .height(36.dp),
            shape = MaterialTheme.shapes.extraSmall,
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = ContentAlpha.disabled)
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            decreaseIcon()
        }
        Text(
            text = currentValue.toString(),
            color = AlertDialogDefaults.textContentColor.copy(alpha = ContentAlpha.high),
            fontSize = MaterialTheme.typography.labelSmall.fontSize
        )
        OutlinedButton(
            onClick = {
                if (currentValue + step <= valueRange.endInclusive) {
                    currentValue += step
                    onValueChange(currentValue)
                }
            },
            enabled = currentValue + step <= valueRange.endInclusive,
            modifier = Modifier
                .width(48.dp)
                .height(36.dp),
            shape = MaterialTheme.shapes.extraSmall,
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = ContentAlpha.disabled)
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            increaseIcon()
        }
    }
}