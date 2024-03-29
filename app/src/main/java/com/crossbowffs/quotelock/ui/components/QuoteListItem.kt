package com.crossbowffs.quotelock.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.data.api.QuoteEntity
import com.crossbowffs.quotelock.data.api.readableSource
import com.crossbowffs.quotelock.data.api.toQuoteData
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import com.yubyf.quotelockx.R
import kotlinx.coroutines.launch

@Composable
fun EditableQuoteListItem(
    modifier: Modifier = Modifier,
    entity: QuoteEntity,
    onClick: (QuoteData) -> Unit = {},
    onEdit: (QuoteEntity) -> Unit = {},
    onDelete: (Int?) -> Unit = {},
) {
    QuoteListItem(entity = entity, modifier = modifier, onClick = onClick) { closeMenu ->
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.edit)) },
            onClick = {
                closeMenu()
                onEdit.invoke(entity)
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.delete)) },
            onClick = {
                closeMenu()
                onDelete.invoke(entity.id)
            }
        )
    }
}

@Composable
fun DeletableQuoteListItem(
    modifier: Modifier = Modifier,
    entity: QuoteEntity,
    onClick: (QuoteData) -> Unit = {},
    onDelete: (Int?) -> Unit = {},
) {
    QuoteListItem(entity = entity, modifier = modifier, onClick = onClick) { closeMenu ->
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.delete)) },
            onClick = {
                closeMenu()
                onDelete.invoke(entity.id)
            }
        )
    }
}

@Composable
fun QuoteListItem(
    modifier: Modifier = Modifier,
    entity: QuoteEntity,
    onClick: (QuoteData) -> Unit = { },
    content: @Composable ColumnScope.(() -> Unit) -> Unit,
) {
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
    val scope = rememberCoroutineScope()
    var press: PressInteraction.Press? by remember {
        mutableStateOf(null)
    }
    Box(contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .height(height)
            .indication(interactionSource = interactionSource, LocalIndication.current)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        tryAwaitRelease()
                        press?.let { interactionSource.emit(PressInteraction.Release(it)) }
                    },
                    onLongPress = { offset ->
                        contextMenuExpanded = true
                        contextMenuOffset = offset
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        scope.launch {
                            press = PressInteraction
                                .Press(offset)
                                .also {
                                    interactionSource.emit(it)
                                }
                        }
                    },
                    onTap = { offset ->
                        scope.launch {
                            PressInteraction
                                .Press(offset)
                                .apply {
                                    interactionSource.emit(this)
                                    interactionSource.emit(PressInteraction.Release(this))
                                }
                        }
                        onClick(entity.toQuoteData())
                    }
                )
            }
    ) {
        val readableSource = entity.readableSource
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .wrapContentHeight()
        ) {
            Text(
                text = entity.text,
                fontSize = 20.sp,
                textAlign = TextAlign.Start,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            if (readableSource.isNotBlank()) {
                Text(
                    text = readableSource,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Start,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(ContentAlpha.high)
                        .padding(top = 4.dp)
                )
            }
        }
        DropdownMenu(
            expanded = contextMenuExpanded,
            offset = DpOffset(with(LocalDensity.current) { contextMenuOffset.x.toDp() },
                with(LocalDensity.current) { contextMenuOffset.y.toDp() } - height),
            onDismissRequest = { contextMenuExpanded = false },
        ) {
            content { contextMenuExpanded = false }
        }
    }
}

class QuoteListItemPreviewParameterProvider : PreviewParameterProvider<QuoteEntity> {
    override val values: Sequence<QuoteEntity> = sequenceOf(
        object : QuoteEntity {
            override val id: Int = 0
            override val text: String = "落霞与孤鹜齐飞，秋水共长天一色"
            override val source: String = "《滕王阁序》"
            override val author: String = "王勃"
            override val uid: String = ""
            override val provider: String = ""
            override val extra: ByteArray? = null

            override fun equals(other: Any?): Boolean = false
        },
        object : QuoteEntity {
            override val id: Int = 0
            override val text: String = "落霞与孤鹜齐飞，秋水共长天一色"
            override val source: String = ""
            override val author: String? = null
            override val uid: String = ""
            override val provider: String = ""
            override val extra: ByteArray? = null

            override fun equals(other: Any?): Boolean = false
        }
    )
}

@Preview(name = "Quote Item Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Quote Item Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HistoryTopAppBarPreview(
    @PreviewParameter(QuoteListItemPreviewParameterProvider::class) entity: QuoteEntity,
) {
    QuoteLockTheme {
        Surface {
            DeletableQuoteListItem(entity = entity)
        }
    }
}