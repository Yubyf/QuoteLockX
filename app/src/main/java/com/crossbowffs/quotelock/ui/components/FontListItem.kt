package com.crossbowffs.quotelock.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.crossbowffs.quotelock.app.font.FontInfo
import com.crossbowffs.quotelock.app.font.FontInfoWithState
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import com.yubyf.quotelockx.R
import kotlinx.coroutines.launch

@Composable
fun DeletableFontListItem(
    modifier: Modifier = Modifier,
    fontInfoWithState: FontInfoWithState,
    onClick: (FontInfoWithState) -> Unit = {},
    onDelete: (FontInfoWithState) -> Unit = {},
) {
    FontListItem(
        fontInfoWithState = fontInfoWithState,
        modifier = modifier,
        onClick = onClick,
    ) { closeMenu ->
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.delete)) },
            onClick = {
                closeMenu()
                onDelete.invoke(fontInfoWithState)
            }
        )
    }
}

@Composable
fun FontListItem(
    modifier: Modifier = Modifier,
    fontInfoWithState: FontInfoWithState,
    onClick: (FontInfoWithState) -> Unit = {},
    content: @Composable ColumnScope.(() -> Unit) -> Unit,
) {
    val minHeight = 112.dp
    val density = LocalDensity.current
    var height by remember { mutableStateOf(with(density) { minHeight.roundToPx() }) }
    var contextMenuExpanded by remember {
        mutableStateOf(false)
    }
    var contextMenuOffset by remember {
        mutableStateOf(Offset(0F, 0F))
    }
    val interactionSource = remember {
        MutableInteractionSource()
    }
    val scope = rememberCoroutineScope()
    var press: PressInteraction.Press? by remember {
        mutableStateOf(null)
    }
    val haptic = LocalHapticFeedback.current
    Box(contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .heightIn(min = minHeight)
            .fillMaxWidth()
            .onSizeChanged { height = it.height }
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
                        onClick.invoke(fontInfoWithState)
                    }
                )
            }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (!fontInfoWithState.active) {
            Text(
                text = stringResource(id = R.string.quote_fonts_management_inactive).uppercase(),
                modifier = Modifier
                    .alpha(0.05F)
                    .fillMaxWidth(),
                fontSize = 56.sp,
                letterSpacing = 0.05.em,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
        Row(Modifier
            .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier
                .weight(1F)
                .wrapContentHeight()
                .alpha(if (fontInfoWithState.active) 1F else ContentAlpha.disabled)
            ) {
                val fontInfo = fontInfoWithState.fontInfo
                val font by remember {
                    mutableStateOf(fontInfo.composeFontInStyle())
                }
                Text(text = with(fontInfo) { LocalConfiguration.current.localeName },
                    fontFamily = font,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Start,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth())
                Text(text = fontInfo.descriptionLatin,
                    fontFamily = font,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Start,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp))
                if (fontInfo.descriptionLocale.isNotBlank()) {
                    Text(text = fontInfo.descriptionLocale,
                        fontFamily = font,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Start,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp))
                }
            }
            if (!fontInfoWithState.active && !fontInfoWithState.systemFont) {
                var hintMenuExpanded by remember {
                    mutableStateOf(false)
                }
                var hintMenuOffset by remember {
                    mutableStateOf(Offset(0F, 0F))
                }
                IconButton(onClick = { hintMenuExpanded = true },
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        hintMenuOffset = Offset(coordinates.positionInParent().x,
                            coordinates.boundsInParent().center.y)
                    }) {
                    Icon(Icons.Rounded.Info, contentDescription = "Font active hint")
                }
                AnchorPopup(
                    expanded = hintMenuExpanded,
                    onDismissRequest = { hintMenuExpanded = false },
                    anchor = DpOffset(with(LocalDensity.current) { hintMenuOffset.x.toDp() },
                        with(LocalDensity.current) { (hintMenuOffset.y).toDp() }),
                    alignment = Alignment.CenterStart,
                    content = {
                        Surface(
                            modifier = Modifier
                                .width(IntrinsicSize.Max)
                                .widthIn(max = 300.dp)
                                .wrapContentHeight(),
                            shape = BubbleShape(cornerSize = 4.dp, arrowSize = 6.dp),
                            color = MaterialTheme.colorScheme.inverseSurface,
                            tonalElevation = 4.dp,
                            shadowElevation = 4.dp
                        ) {
                            Text(text = stringResource(id = R.string.quote_fonts_management_activate_tips),
                                modifier = Modifier
                                    .padding(start = 8.dp, top = 8.dp, end = 14.dp, bottom = 8.dp)
                                    .alpha(ContentAlpha.high),
                                fontSize = MaterialTheme.typography.labelLarge.fontSize)
                        }
                    }
                )
            }
        }
        DropdownMenu(
            expanded = contextMenuExpanded,
            offset = DpOffset(with(LocalDensity.current) { contextMenuOffset.x.toDp() },
                with(LocalDensity.current) { (contextMenuOffset.y - height).toDp() }),
            onDismissRequest = { contextMenuExpanded = false },
        ) {
            content { contextMenuExpanded = false }
        }
    }
}

class FontListItemPreviewParameterProvider : PreviewParameterProvider<FontInfoWithState> {
    override val values: Sequence<FontInfoWithState> = sequenceOf(
        FontInfoWithState(FontInfo(names = mapOf("en-US" to "Roboto Regular"),
            descriptionLatin = "Lorem ipsum dolor sit amet",
            descriptionLocale = " Lorem"
        ), systemFont = false, active = false),
        FontInfoWithState(FontInfo(names = mapOf("en-US" to "方正新书宋"),
            descriptionLatin = "Lorem ipsum dolor sit amet",
            descriptionLocale = "落霞与孤鹜齐飞，秋水共长天一色"
        ), systemFont = true, active = true)
    )
}

@Preview(name = "Font List Item Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Font List Item Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FontListItemPreview(
    @PreviewParameter(FontListItemPreviewParameterProvider::class) entity: FontInfoWithState,
) {
    QuoteLockTheme {
        Surface {
            FontListItem(fontInfoWithState = entity) {}
        }
    }
}