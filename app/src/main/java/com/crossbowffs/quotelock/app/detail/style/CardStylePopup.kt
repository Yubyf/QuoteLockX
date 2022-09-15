package com.crossbowffs.quotelock.app.detail.style

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.crossbowffs.quotelock.app.font.FontInfo
import com.crossbowffs.quotelock.consts.*
import com.crossbowffs.quotelock.data.api.CardStyle
import com.crossbowffs.quotelock.ui.components.ContentAlpha
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import com.crossbowffs.quotelock.utils.loadComposeFontWithSystem
import com.yubyf.quotelockx.R


@Composable
fun CardStylePopup(
    fonts: List<FontInfo>,
    cardStyle: CardStyle,
    onFontSelected: (String) -> Unit,
    onFontAdd: () -> Unit,
    onQuoteSizeChange: (Int) -> Unit,
    onSourceSizeChange: (Int) -> Unit,
    onLineSpacingChange: (Int) -> Unit,
    onCardPaddingChange: (Int) -> Unit,
    onShareWatermarkChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val names = stringArrayResource(id = R.array.default_font_family_entries)
    val paths = stringArrayResource(id = R.array.default_font_family_values)
    val presetFonts = names.zip(paths).map { (name, path) ->
        FontInfo(fileName = name, path = path)
    }
    val allFonts = presetFonts + fonts
    var selectedItemIndex by remember {
        mutableStateOf(allFonts.indexOfFirst { it.path == cardStyle.fontFamily }
            .coerceIn(minimumValue = 0, maximumValue = allFonts.lastIndex))
    }
    val fontIndicatorWidth = 64.dp
    val haptic = LocalHapticFeedback.current
    fun performHapticFeedback() = haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    Popup(alignment = Alignment.TopCenter,
        onDismissRequest = onDismiss,
        properties = PopupProperties()
    ) {
        Surface(
            modifier = Modifier.padding(horizontal = 24.dp),
            shape = MaterialTheme.shapes.small,
            tonalElevation = 8.dp,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = 16.dp)
            ) {
                LazyRow(modifier = Modifier.fillMaxWidth()) {
                    itemsIndexed(allFonts) { index, fontInfo ->
                        Spacer(modifier = Modifier.width(16.dp))
                        PopupFontIndicator(fontInfo = fontInfo, width = fontIndicatorWidth,
                            selected = selectedItemIndex == index) {
                            selectedItemIndex = index
                            performHapticFeedback()
                            onFontSelected(it)
                        }
                        if (index == presetFonts.lastIndex && fonts.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(20.dp))
                            Divider(modifier = Modifier
                                .height(56.dp)
                                .padding(top = 4.dp)
                                .width(0.5.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        if (index == allFonts.lastIndex) {
                            Spacer(modifier = Modifier.width(16.dp))
                            OutlinedButton(
                                onClick = { onDismiss(); onFontAdd() },
                                shape = CircleShape,
                                border = BorderStroke(Dp.Hairline,
                                    MaterialTheme.colorScheme.outline),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.size(fontIndicatorWidth)
                            ) {
                                Icon(Icons.Rounded.Add,
                                    contentDescription = "Add font",
                                    modifier = Modifier.size(32.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                    }
                }
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)) {
                    PopupLayoutRow(
                        lineSpacing = cardStyle.lineSpacing,
                        cardPadding = cardStyle.cardPadding,
                        onLineSpacingChange = { performHapticFeedback(); onLineSpacingChange(it) },
                        onCardPaddingChange = { performHapticFeedback(); onCardPaddingChange(it) },
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    PopupFontSizeRow(
                        quoteSize = cardStyle.quoteSize,
                        sourceSize = cardStyle.sourceSize,
                        onQuoteSizeChange = { performHapticFeedback(); onQuoteSizeChange(it) },
                        onSourceSizeChange = { performHapticFeedback(); onSourceSizeChange(it) },
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(id = R.string.quote_card_style_share_watermark),
                            fontSize = MaterialTheme.typography.labelLarge.fontSize,
                            modifier = Modifier
                                .weight(1f)
                                .alpha(ContentAlpha.medium),
                        )
                        var shareWatermark by remember {
                            mutableStateOf(cardStyle.shareWatermark)
                        }
                        Switch(checked = shareWatermark,
                            onCheckedChange = {
                                shareWatermark = it; onShareWatermarkChange(it)
                            })
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier
                        .alpha(ContentAlpha.disabled),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.quote_card_style_popup_hint),
                            fontSize = MaterialTheme.typography.labelSmall.fontSize,
                            lineHeight = 1.em,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PopupFontIndicator(
    fontInfo: FontInfo,
    width: Dp,
    selected: Boolean = false,
    onClick: (String) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val composeFontFamily = loadComposeFontWithSystem(fontInfo.path)
        TextButton(
            onClick = { onClick(fontInfo.path) },
            colors = if (selected) {
                ButtonDefaults.textButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            } else ButtonDefaults.textButtonColors(),
            shape = CircleShape,
            border = BorderStroke(Dp.Hairline,
                MaterialTheme.colorScheme.outline),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.size(width)
        ) {
            Text(text = if (fontInfo.cjk) "文" else "A",
                fontSize = if (fontInfo.cjk) 28.sp else 32.sp,
                letterSpacing = 0.sp,
                lineHeight = 0.em,
                fontFamily = composeFontFamily)
        }
        Text(
            text = with(fontInfo) {
                LocalConfiguration.current.localeName.takeIf { it.isNotBlank() }
                    ?: fileName
            },
            color = AlertDialogDefaults.textContentColor,
            fontSize = MaterialTheme.typography.bodySmall.fontSize,
            letterSpacing = 0.sp,
            lineHeight = 1.em,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier
                .widthIn(max = width)
                .padding(top = 4.dp)
        )
    }
}

@Composable
private fun PopupLayoutRow(
    modifier: Modifier = Modifier,
    lineSpacing: Int,
    cardPadding: Int,
    onLineSpacingChange: (Int) -> Unit,
    onCardPaddingChange: (Int) -> Unit,
) {
    Row(modifier = modifier
        .height(IntrinsicSize.Min)
    ) {
        Column(modifier = Modifier
            .fillMaxWidth(0.5F)
            .padding(end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NumberButtonPicker(
                modifier = Modifier
                    .fillMaxWidth(),
                value = lineSpacing,
                onValueChange = onLineSpacingChange,
                valueRange = PREF_CARD_STYLE_LINE_SPACING_MIN..PREF_CARD_STYLE_LINE_SPACING_MAX,
                step = PREF_CARD_STYLE_LINE_SPACING_STEP,
                decreaseIcon = {
                    Icon(painter = painterResource(id = R.drawable.ic_format_decrease_line_spacing_24_dp),
                        contentDescription = "",
                        modifier = Modifier.size(24.dp))
                },
                increaseIcon = {
                    Icon(painter = painterResource(id = R.drawable.ic_format_increase_line_spacing_24_dp),
                        contentDescription = "",
                        modifier = Modifier.size(24.dp))
                }
            )
        }
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NumberButtonPicker(
                modifier = Modifier
                    .fillMaxWidth(),
                value = cardPadding,
                onValueChange = onCardPaddingChange,
                valueRange = PREF_CARD_STYLE_CARD_PADDING_MIN..PREF_CARD_STYLE_CARD_PADDING_MAX,
                step = PREF_CARD_STYLE_CARD_PADDING_STEP,
                decreaseIcon = {
                    Icon(painter = painterResource(id = R.drawable.ic_format_page_padding_24_dp),
                        contentDescription = "",
                        modifier = Modifier.size(24.dp))
                },
                increaseIcon = {
                    Icon(painter = painterResource(id = R.drawable.ic_format_page_large_padding_24_dp),
                        contentDescription = "",
                        modifier = Modifier.size(24.dp))
                }
            )
        }
    }
}

@Composable
private fun PopupFontSizeRow(
    modifier: Modifier = Modifier,
    quoteSize: Int,
    sourceSize: Int,
    onQuoteSizeChange: (Int) -> Unit,
    onSourceSizeChange: (Int) -> Unit,
) {
    Row(modifier = modifier
        .height(IntrinsicSize.Min)
    ) {
        Column(modifier = Modifier
            .fillMaxWidth(0.5F)
            .padding(end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(modifier = Modifier.alpha(ContentAlpha.medium),
                text = stringResource(R.string.quote_card_style_text_title),
                color = AlertDialogDefaults.textContentColor,
                fontSize = MaterialTheme.typography.bodySmall.fontSize)
            NumberButtonPicker(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                value = quoteSize,
                onValueChange = onQuoteSizeChange,
                valueRange = PREF_CARD_STYLE_FONT_SIZE_TEXT_MIN..PREF_CARD_STYLE_FONT_SIZE_TEXT_MAX,
                step = PREF_CARD_STYLE_FONT_SIZE_TEXT_STEP,
                decreaseIcon = {
                    Icon(Icons.Rounded.Title,
                        contentDescription = "",
                        modifier = Modifier.size(16.dp))
                },
                increaseIcon = {
                    Icon(Icons.Rounded.Title,
                        contentDescription = "",
                        modifier = Modifier.size(24.dp))
                }
            )
        }
        Divider(modifier = Modifier
            .fillMaxHeight()
            .padding(vertical = 8.dp)
            .width(0.5.dp)
        )
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(modifier = Modifier.alpha(ContentAlpha.medium),
                text = stringResource(R.string.quote_card_style_source_title),
                color = AlertDialogDefaults.textContentColor,
                fontSize = MaterialTheme.typography.bodySmall.fontSize)
            NumberButtonPicker(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                value = sourceSize,
                onValueChange = onSourceSizeChange,
                valueRange = PREF_CARD_STYLE_FONT_SIZE_SOURCE_MIN..PREF_CARD_STYLE_FONT_SIZE_SOURCE_MAX,
                step = PREF_CARD_STYLE_FONT_SIZE_SOURCE_STEP,
                decreaseIcon = {
                    Icon(Icons.Rounded.Title,
                        contentDescription = "",
                        modifier = Modifier.size(16.dp))
                },
                increaseIcon = {
                    Icon(Icons.Rounded.Title,
                        contentDescription = "",
                        modifier = Modifier.size(24.dp))
                }
            )
        }
    }
}

@Composable
private fun NumberButtonPicker(
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

    Row(modifier = modifier,
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
            border = BorderStroke(Dp.Hairline,
                MaterialTheme.colorScheme.outline),
            contentPadding = PaddingValues(0.dp)
        ) {
            decreaseIcon()
        }
        Text(text = currentValue.toString(),
            color = AlertDialogDefaults.textContentColor,
            fontSize = MaterialTheme.typography.bodySmall.fontSize)
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
            border = BorderStroke(Dp.Hairline,
                MaterialTheme.colorScheme.outline),
            contentPadding = PaddingValues(0.dp)
        ) {
            increaseIcon()
        }
    }
}

@Preview(name = "Card Style Popup Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Card Style Popup Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CardStylePopupPreview() {
    QuoteLockTheme {
        Surface {
            CardStylePopup(
                fonts = emptyList(),
                cardStyle = CardStyle(),
                onFontSelected = {},
                onFontAdd = {},
                onQuoteSizeChange = {},
                onSourceSizeChange = {},
                onLineSpacingChange = {},
                onCardPaddingChange = {},
                onShareWatermarkChange = {},
                onDismiss = {}
            )
        }
    }
}