package com.crossbowffs.quotelock.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun <T> MultiSelectItemList(
    modifier: Modifier = Modifier,
    entries: Array<String>,
    entryValues: Array<T>,
    selectedItems: Set<T>? = null,
    onItemsSelected: (Set<T>) -> Unit,
) {
    val minHeight = 56.dp
    val minHeightPx = with(LocalDensity.current) { minHeight.toPx() }
    var selectedItemValues by remember {
        mutableStateOf(selectedItems)
    }
    var containerHeight by remember {
        mutableStateOf(0)
    }
    var columnHeight by remember {
        mutableStateOf(0F)
    }
    var scrollable by remember {
        mutableStateOf(false)
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState(), enabled = scrollable)
            .onGloballyPositioned { coordinates ->
                (coordinates.parentCoordinates?.size?.height ?: 0).let {
                    if (containerHeight != it) {
                        containerHeight = coordinates.parentCoordinates?.size?.height ?: 0
                        scrollable = containerHeight < columnHeight
                    }
                }
            }
    ) {
        entries.zip(entryValues).forEachIndexed { index, (entry, value) ->
            var checked by remember {
                mutableStateOf(selectedItemValues?.contains(value) ?: false)
            }
            Box(modifier = Modifier
                .heightIn(min = minHeight)
                .fillMaxWidth()
                .let { if (!scrollable) it.weight(1F) else it }
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
                    onItemsSelected(selectedItemValues.orEmpty())
                },
                contentAlignment = Alignment.CenterStart
            ) {
                Row(modifier = Modifier
                    .wrapContentHeight()
                    .onSizeChanged {
                        columnHeight += maxOf(it.height.toFloat(), minHeightPx)
                    }
                ) {
                    Checkbox(
                        checked = checked,
                        modifier = Modifier.padding(start = 24.dp),
                        onCheckedChange = null,
                    )
                    Spacer(modifier = Modifier.width(24.dp))
                    Text(
                        text = entry,
                        color = AlertDialogDefaults.textContentColor,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize
                    )
                }
            }
            if (index != entries.lastIndex) {
                Divider(thickness = Dp.Hairline)
            }
        }
    }
}