package com.crossbowffs.quotelock.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.crossbowffs.quotelock.utils.px2dp

private val contentPaddingHorizontal = 12.dp
private val contentPaddingVertical = 8.dp
private val iconSize = 18.dp
private val iconPadding = 8.dp

/**
 * [Segmented buttons](https://m3.material.io/components/segmented-buttons/guidelines) with M3 Style
 * modified from [Reference](https://medium.com/@manojbhadane/hello-everyone-558290eb632e)
 *
 * @param items list of items to be render
 * @param selectedItemIndex to highlight item by default (Optional)
 * @param autoSize set true if you want to set auto width to item (Optional)
 * @param cornerRadius To make control as rounded (Optional)
 * @param color to set color to control (Optional)
 */
@Composable
fun SegmentedControl(
    modifier: Modifier = Modifier,
    items: List<SegmentedItem>,
    selectedItemIndex: Int = 0,
    autoSize: Boolean = true,
    cornerRadius: Int = 50,
    color: Color = Color.Unspecified,
    onItemSelection: (selectedItemIndex: Int) -> Unit,
) {
    var selectedIndex by remember { mutableStateOf(selectedItemIndex) }
    val selectContainerColor =
        if (color.isSpecified) color else MaterialTheme.colorScheme.secondaryContainer
    val selectContentColor =
        if (color.isSpecified) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSecondaryContainer
    val hasIcon = items.any { it is SegmentedItemWithIcon<*> }
    Row(
        modifier = modifier
    ) {
        var labelWidth by remember { mutableStateOf(0) }
        val itemWidth = if (autoSize && labelWidth != 0)
            labelWidth.px2dp() + if (hasIcon) iconSize + iconPadding else 0.dp
        else 0.dp
        items.forEachIndexed { index, item ->
            val selected = selectedIndex == index
            OutlinedButton(
                modifier = when (index) {
                    0 -> {
                        Modifier
                            .offset(0.dp, 0.dp)
                            .zIndex(if (selected) 1f else 0f)
                    }

                    else -> {
                        Modifier
                            .offset((-1 * index).dp, 0.dp)
                            .zIndex(if (selected) 1f else 0f)
                    }
                },
                onClick = {
                    selectedIndex = index
                    onItemSelection(selectedIndex)
                },
                shape = when (index) {
                    /**
                     * left outer button
                     */
                    0 -> RoundedCornerShape(
                        topStartPercent = cornerRadius,
                        topEndPercent = 0,
                        bottomStartPercent = cornerRadius,
                        bottomEndPercent = 0
                    )
                    /**
                     * right outer button
                     */
                    items.size - 1 -> RoundedCornerShape(
                        topStartPercent = 0,
                        topEndPercent = cornerRadius,
                        bottomStartPercent = 0,
                        bottomEndPercent = cornerRadius
                    )
                    /**
                     * middle button
                     */
                    else -> RoundedCornerShape(
                        topStartPercent = 0,
                        topEndPercent = 0,
                        bottomStartPercent = 0,
                        bottomEndPercent = 0
                    )
                },
                colors = if (selected) {
                    /**
                     * selected colors
                     */
                    ButtonDefaults.outlinedButtonColors(
                        containerColor = selectContainerColor
                    )
                } else {
                    /**
                     * not selected colors
                     */
                    ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent)
                },
                contentPadding = PaddingValues(
                    start = contentPaddingHorizontal,
                    end = contentPaddingHorizontal,
                    top = contentPaddingVertical,
                    bottom = contentPaddingVertical
                )
            ) {
                Row(
                    modifier = if (itemWidth > 0.dp) Modifier.width(itemWidth)
                    else Modifier.wrapContentSize(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (item is SegmentedItemWithIcon<*>
                        && (!item.onlyShowWhenSelected || item.onlyShowWhenSelected && selected)
                    ) {
                        when (item) {
                            is SegmentedItemWithIcon.IconTypeVector -> {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(iconSize),
                                    tint = selectContentColor
                                )
                            }

                            is SegmentedItemWithIcon.IconTypeRes -> {
                                Icon(
                                    painter = painterResource(id = item.icon),
                                    contentDescription = null,
                                    modifier = Modifier.size(iconSize),
                                    tint = selectContentColor
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(iconPadding))
                    }
                    Text(
                        text = item.label,
                        modifier = Modifier.onSizeChanged {
                            labelWidth = maxOf(labelWidth, it.width)
                        },
                        color = selectContentColor,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

sealed interface SegmentedItem {
    val label: String
}

data class SegmentedLabelItem(override val label: String) : SegmentedItem

sealed class SegmentedItemWithIcon<T>(open val icon: T, open val onlyShowWhenSelected: Boolean) :
    SegmentedItem {
    data class IconTypeRes(
        override val label: String,
        @DrawableRes override val icon: Int,
        override val onlyShowWhenSelected: Boolean = true,
    ) : SegmentedItemWithIcon<Int>(icon, onlyShowWhenSelected)

    data class IconTypeVector(
        override val label: String,
        override val icon: ImageVector,
        override val onlyShowWhenSelected: Boolean = true,
    ) : SegmentedItemWithIcon<ImageVector>(icon, onlyShowWhenSelected)
}
