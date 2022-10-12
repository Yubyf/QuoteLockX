package com.crossbowffs.quotelock.app.detail.style

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.crossbowffs.quotelock.app.font.FontInfo
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_CARD_PADDING_MAX
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_CARD_PADDING_MIN
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_CARD_PADDING_STEP
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_LINE_SPACING_MAX
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_LINE_SPACING_MIN
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_LINE_SPACING_STEP
import com.crossbowffs.quotelock.data.api.CardStyle
import com.crossbowffs.quotelock.ui.components.AnchorPopup
import com.crossbowffs.quotelock.ui.components.ContentAlpha
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import com.crossbowffs.quotelock.utils.loadComposeFontWithSystem
import com.yubyf.quotelockx.R


@Composable
fun CardStylePopup(
    popped: Boolean,
    fonts: List<FontInfo>,
    cardStyle: CardStyle,
    onFontSelected: (FontInfo) -> Unit,
    onFontAdd: () -> Unit,
    onQuoteSizeChange: (Int) -> Unit,
    onSourceSizeChange: (Int) -> Unit,
    onLineSpacingChange: (Int) -> Unit,
    onCardPaddingChange: (Int) -> Unit,
    onQuoteWeightChange: (Int) -> Unit,
    onQuoteItalicChange: (Float) -> Unit,
    onSourceWeightChange: (Int) -> Unit,
    onSourceItalicChange: (Float) -> Unit,
    onDismiss: () -> Unit,
) {
    AnchorPopup(
        popped = popped,
        onDismissRequest = onDismiss,
        anchor = DpOffset(0.dp, 0.dp),
        alignment = Alignment.BottomCenter,
    ) {
        CardStyleContent(
            fonts = fonts,
            cardStyle = cardStyle,
            onFontSelected = onFontSelected,
            onFontAdd = onFontAdd,
            onQuoteSizeChange = onQuoteSizeChange,
            onSourceSizeChange = onSourceSizeChange,
            onLineSpacingChange = onLineSpacingChange,
            onCardPaddingChange = onCardPaddingChange,
            onQuoteWeightChange = onQuoteWeightChange,
            onQuoteItalicChange = onQuoteItalicChange,
            onSourceWeightChange = onSourceWeightChange,
            onSourceItalicChange = onSourceItalicChange,
            onDismiss = onDismiss
        )
    }
}

@Composable
fun CardStyleContent(
    fonts: List<FontInfo>,
    cardStyle: CardStyle,
    onFontSelected: (FontInfo) -> Unit,
    onFontAdd: () -> Unit,
    onQuoteSizeChange: (Int) -> Unit,
    onSourceSizeChange: (Int) -> Unit,
    onLineSpacingChange: (Int) -> Unit,
    onCardPaddingChange: (Int) -> Unit,
    onQuoteWeightChange: (Int) -> Unit,
    onQuoteItalicChange: (Float) -> Unit,
    onSourceWeightChange: (Int) -> Unit,
    onSourceItalicChange: (Float) -> Unit,
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
    Surface(
        modifier = Modifier.padding(horizontal = 24.dp),
        shape = MaterialTheme.shapes.small,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = 16.dp)
        ) {
            LazyRow(modifier = Modifier.fillMaxWidth()) {
                itemsIndexed(allFonts) { index, fontInfo ->
                    Spacer(modifier = Modifier.width(16.dp))
                    PopupFontIndicator(
                        fontInfo = fontInfo, width = fontIndicatorWidth,
                        selected = selectedItemIndex == index
                    ) {
                        selectedItemIndex = index
                        performHapticFeedback()
                        onFontSelected(fontInfo)
                    }
                    if (index == presetFonts.lastIndex && fonts.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(20.dp))
                        Divider(
                            modifier = Modifier
                                .height(56.dp)
                                .padding(top = 4.dp)
                                .width(0.5.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = ContentAlpha.disabled)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    if (index == allFonts.lastIndex) {
                        Spacer(modifier = Modifier.width(16.dp))
                        OutlinedButton(
                            onClick = { onDismiss(); onFontAdd() },
                            shape = CircleShape,
                            border = BorderStroke(
                                Dp.Hairline,
                                MaterialTheme.colorScheme.outline
                            ),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(fontIndicatorWidth)
                        ) {
                            Icon(
                                Icons.Rounded.Add,
                                contentDescription = "Add font",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                PopupLayoutRow(
                    lineSpacing = cardStyle.lineSpacing,
                    cardPadding = cardStyle.cardPadding,
                    onLineSpacingChange = { performHapticFeedback(); onLineSpacingChange(it) },
                    onCardPaddingChange = { performHapticFeedback(); onCardPaddingChange(it) },
                )
                Spacer(modifier = Modifier.height(16.dp))
                PopupFontStyleRow(
                    cardStyle = cardStyle,
                    onQuoteSizeChange = { performHapticFeedback(); onQuoteSizeChange(it) },
                    onSourceSizeChange = { performHapticFeedback(); onSourceSizeChange(it) },
                    onQuoteWeightChange = onQuoteWeightChange,
                    onQuoteItalicChange = onQuoteItalicChange,
                    onSourceWeightChange = onSourceWeightChange,
                    onSourceItalicChange = onSourceItalicChange
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .alpha(ContentAlpha.disabled),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.ErrorOutline,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
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

@Composable
private fun PopupFontIndicator(
    fontInfo: FontInfo,
    width: Dp,
    selected: Boolean = false,
    onClick: (FontInfo) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val composeFontFamily = loadComposeFontWithSystem(fontInfo.path)
        TextButton(
            onClick = { onClick(fontInfo) },
            colors = if (selected) {
                ButtonDefaults.textButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            } else ButtonDefaults.textButtonColors(),
            shape = CircleShape,
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = ContentAlpha.disabled)
            ),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.size(width)
        ) {
            Text(
                text = if (fontInfo.cjk) "文" else "A",
                fontSize = if (fontInfo.cjk) 28.sp else 32.sp,
                letterSpacing = 0.sp,
                lineHeight = 0.em,
                fontFamily = composeFontFamily
            )
        }
        Text(
            text = with(fontInfo) {
                LocalConfiguration.current.localeName.takeIf { it.isNotBlank() }
                    ?: fileName
            },
            color = AlertDialogDefaults.textContentColor.copy(alpha = ContentAlpha.high),
            fontSize = MaterialTheme.typography.labelSmall.fontSize,
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
    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
    ) {
        Column(
            modifier = Modifier
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
                    Icon(
                        painter = painterResource(id = R.drawable.ic_format_decrease_segment_spacing_24_dp),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                increaseIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_format_increase_segment_spacing_24_dp),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            )
        }
        Column(
            modifier = Modifier
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
                    Icon(
                        painter = painterResource(id = R.drawable.ic_format_page_padding_24_dp),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                increaseIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_format_page_large_padding_24_dp),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            )
        }
    }
}

@Preview(
    name = "Card Style Popup Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Card Style Popup Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun CardStyleContentPreview() {
    QuoteLockTheme {
        Surface {
            CardStyleContent(
                fonts = emptyList(),
                cardStyle = CardStyle(),
                onFontSelected = { },
                onFontAdd = {},
                onQuoteSizeChange = {},
                onSourceSizeChange = {},
                onLineSpacingChange = {},
                onCardPaddingChange = {},
                onQuoteWeightChange = {},
                onQuoteItalicChange = {},
                onSourceWeightChange = {},
                onSourceItalicChange = {},
                onDismiss = {}
            )
        }
    }
}