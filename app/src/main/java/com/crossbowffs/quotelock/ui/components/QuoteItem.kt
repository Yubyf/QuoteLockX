@file:OptIn(ExperimentalFoundationApi::class)

package com.crossbowffs.quotelock.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.crossbowffs.quotelock.data.api.QuoteEntity
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import com.yubyf.quotelockx.R

@ExperimentalFoundationApi
@Composable
fun QuoteItem(
    entity: QuoteEntity,
    modifier: Modifier = Modifier,
    onClick: (String, String) -> Unit = { _, _ -> },
    onDelete: (() -> Unit)? = null,
) {
    val quote = entity.text
    val source = entity.source.run {
        if (!entity.author.isNullOrBlank()) {
            "${entity.author}${if (this.isBlank()) "" else " $this"}"
        } else {
            this
        }
    }
    val height = 72.dp
    var contextMenuExpanded by remember {
        mutableStateOf(false)
    }
    var contextMenuOffset by remember {
        mutableStateOf(Offset(0F, 0F))
    }
    val interactionSource = remember {
        MutableInteractionSource()
    }
    val haptic = LocalHapticFeedback.current
    Box(contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .height(height)
            .indication(interactionSource = interactionSource, LocalIndication.current)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        val press = PressInteraction.Press(it)
                        interactionSource.emit(press)
                        tryAwaitRelease()
                        interactionSource.emit(PressInteraction.Release(press))
                    },
                    onLongPress = {
                        contextMenuExpanded = true
                        contextMenuOffset = it
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onTap = {
                        onClick.invoke(quote, source)
                    }
                )
            }
    ) {
        Column(Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .wrapContentHeight()
        ) {
            Text(text = quote,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Start,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth())
            if (source.isNotBlank()) {
                Text(text = source,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Start,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth())
            }
        }
        DropdownMenu(
            expanded = contextMenuExpanded,
            offset = DpOffset(with(LocalDensity.current) { contextMenuOffset.x.toDp() },
                with(LocalDensity.current) { contextMenuOffset.y.toDp() } - height),
            onDismissRequest = { contextMenuExpanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(text = stringResource(id = R.string.delete)) },
                onClick = {
                    contextMenuExpanded = false
                    onDelete?.invoke()
                }
            )
        }
    }
}

private val PREVIEW_FULL_ENTITY = object : QuoteEntity {
    override val id: Int
        get() = 0
    override val md5: String
        get() = ""
    override val text: String
        get() = "落霞与孤鹜齐飞，秋水共长天一色"
    override val source: String
        get() = "《滕王阁序》"
    override val author: String
        get() = "王勃"

    override fun equals(other: Any?): Boolean = false
}

private val PREVIEW_SINGLE_LINE_ENTITY = object : QuoteEntity {
    override val id: Int
        get() = 0
    override val md5: String
        get() = ""
    override val text: String
        get() = "落霞与孤鹜齐飞，秋水共长天一色"
    override val source: String
        get() = ""
    override val author: String?
        get() = null

    override fun equals(other: Any?): Boolean = false
}

@Preview(name = "Quote Item Light Full",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Quote Item Dark Full",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HistoryTopAppBarLightPreview() {
    QuoteLockTheme {
        Surface {
            QuoteItem(PREVIEW_FULL_ENTITY)
        }
    }
}

@Preview(name = "Quote Item Light Single Line", showBackground = true)
@Composable
private fun HistoryTopAppBarSingleLinePreview() {
    QuoteLockTheme {
        Surface {
            QuoteItem(PREVIEW_SINGLE_LINE_ENTITY)
        }
    }
}