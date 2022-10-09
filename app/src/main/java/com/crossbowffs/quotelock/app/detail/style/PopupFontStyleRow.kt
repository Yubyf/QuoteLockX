package com.crossbowffs.quotelock.app.detail.style

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FormatBold
import androidx.compose.material.icons.rounded.FormatItalic
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconToggleButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.crossbowffs.quotelock.app.font.FontInfo
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_FONT_ITALIC_SOURCE_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_FONT_ITALIC_TEXT_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_FONT_SIZE_SOURCE_MAX
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_FONT_SIZE_SOURCE_MIN
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_FONT_SIZE_SOURCE_STEP
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_FONT_SIZE_TEXT_MAX
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_FONT_SIZE_TEXT_MIN
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_FONT_SIZE_TEXT_STEP
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_FONT_WEIGHT_SOURCE_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_FONT_WEIGHT_TEXT_DEFAULT
import com.crossbowffs.quotelock.data.api.CardStyle
import com.crossbowffs.quotelock.ui.components.ContentAlpha
import com.yubyf.quotelockx.R
import kotlin.math.roundToInt


@Composable
internal fun PopupFontStyleRow(
    modifier: Modifier = Modifier,
    selectedFont: FontInfo,
    cardStyle: CardStyle,
    onQuoteSizeChange: (Int) -> Unit,
    onSourceSizeChange: (Int) -> Unit,
    onQuoteWeightChange: (Int) -> Unit,
    onQuoteItalicChange: (Float) -> Unit,
    onSourceWeightChange: (Int) -> Unit,
    onSourceItalicChange: (Float) -> Unit,
) {
    Column(modifier = modifier) {
        val fontChanged by remember(selectedFont) {
            derivedStateOf {
                selectedFont.path != cardStyle.fontFamily
            }
        }
        var quoteBoldChecked by remember(selectedFont) {
            mutableStateOf(
                !fontChanged && !selectedFont.supportVariableWeight
                        && cardStyle.quoteFontStyle.weight == FontWeight.Bold
            )
        }
        var quoteItalicChecked by remember(selectedFont) {
            mutableStateOf(
                !fontChanged && !selectedFont.supportVariableSlant
                        && cardStyle.quoteFontStyle.italic.roundToInt() == FontStyle.Italic.value
            )
        }
        var sourceBoldChecked by remember(selectedFont) {
            mutableStateOf(
                !fontChanged && !selectedFont.supportVariableWeight
                        && cardStyle.sourceFontStyle.weight == FontWeight.Bold
            )
        }
        var sourceItalicChecked by remember(selectedFont) {
            mutableStateOf(
                !fontChanged && !selectedFont.supportVariableSlant
                        && cardStyle.sourceFontStyle.italic.roundToInt() == FontStyle.Italic.value
            )
        }
        var textWeightVariableValue by remember(selectedFont, cardStyle.quoteFontStyle.weight) {
            mutableStateOf(
                if (fontChanged) PREF_CARD_STYLE_FONT_WEIGHT_TEXT_DEFAULT.weight.toFloat()
                else cardStyle.quoteFontStyle.weight.weight.toFloat()
            )
        }
        var sourceWeightVariableValue by remember(selectedFont, cardStyle.sourceFontStyle.weight) {
            mutableStateOf(
                if (fontChanged) PREF_CARD_STYLE_FONT_WEIGHT_SOURCE_DEFAULT.weight.toFloat()
                else cardStyle.sourceFontStyle.weight.weight.toFloat()
            )
        }
        var textItalicVariableValue by remember(selectedFont, cardStyle.quoteFontStyle.italic) {
            mutableStateOf(
                if (fontChanged) PREF_CARD_STYLE_FONT_ITALIC_TEXT_DEFAULT
                else cardStyle.quoteFontStyle.italic
            )
        }
        var sourceItalicVariableValue by remember(selectedFont, cardStyle.sourceFontStyle.italic) {
            mutableStateOf(
                if (fontChanged) PREF_CARD_STYLE_FONT_ITALIC_SOURCE_DEFAULT
                else cardStyle.sourceFontStyle.italic
            )
        }
        var variableValueRange by remember { mutableStateOf(0F..1F) }
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.5F)
                    .padding(end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.alpha(ContentAlpha.medium),
                    text = stringResource(R.string.quote_card_style_text_title),
                    color = AlertDialogDefaults.textContentColor,
                    fontSize = MaterialTheme.typography.bodySmall.fontSize
                )
                Spacer(modifier = Modifier.height(8.dp))
                NumberButtonPicker(
                    modifier = Modifier.fillMaxWidth(),
                    value = cardStyle.quoteSize,
                    onValueChange = onQuoteSizeChange,
                    valueRange = PREF_CARD_STYLE_FONT_SIZE_TEXT_MIN..PREF_CARD_STYLE_FONT_SIZE_TEXT_MAX,
                    step = PREF_CARD_STYLE_FONT_SIZE_TEXT_STEP,
                    decreaseIcon = {
                        Icon(
                            Icons.Rounded.Title,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    increaseIcon = {
                        Icon(
                            Icons.Rounded.Title,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                FontStyleRow(fontInfo = selectedFont,
                    boldChecked = quoteBoldChecked,
                    italicChecked = quoteItalicChecked,
                    onWeightChange = {
                        quoteBoldChecked = it
                        if (it && selectedFont.supportVariableWeight) {
                            sourceBoldChecked = false
                            if (selectedFont.supportVariableSlant) {
                                quoteItalicChecked = false
                                sourceItalicChecked = false
                            }
                            variableValueRange =
                                (selectedFont.variableWeight?.range?.start?.toFloat()
                                    ?: FontWeight.Normal.weight.toFloat())..
                                        (selectedFont.variableWeight?.range?.endInclusive?.toFloat()
                                            ?: FontWeight.Bold.weight.toFloat())
                        } else {
                            onQuoteWeightChange(
                                if (it) FontWeight.Bold.weight
                                else FontWeight.Normal.weight
                            )
                        }
                    },
                    onItalicChange = {
                        quoteItalicChecked = it
                        if (it && selectedFont.supportVariableSlant) {
                            sourceItalicChecked = false
                            if (selectedFont.supportVariableWeight) {
                                quoteBoldChecked = false
                                sourceBoldChecked = false
                            }
                            variableValueRange =
                                (selectedFont.variableSlant?.range?.start
                                    ?: FontStyle.Normal.value.toFloat())..
                                        (selectedFont.variableSlant?.range?.endInclusive
                                            ?: FontStyle.Italic.value.toFloat())
                        } else {
                            onQuoteItalicChange(
                                if (it) FontStyle.Italic.value.toFloat()
                                else FontStyle.Normal.value.toFloat()
                            )
                        }
                    })
            }
            Divider(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 8.dp)
                    .width(0.5.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = ContentAlpha.disabled)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.alpha(ContentAlpha.medium),
                    text = stringResource(R.string.quote_card_style_source_title),
                    color = AlertDialogDefaults.textContentColor,
                    fontSize = MaterialTheme.typography.bodySmall.fontSize
                )
                NumberButtonPicker(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    value = cardStyle.sourceSize,
                    onValueChange = onSourceSizeChange,
                    valueRange = PREF_CARD_STYLE_FONT_SIZE_SOURCE_MIN..PREF_CARD_STYLE_FONT_SIZE_SOURCE_MAX,
                    step = PREF_CARD_STYLE_FONT_SIZE_SOURCE_STEP,
                    decreaseIcon = {
                        Icon(
                            Icons.Rounded.Title,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    increaseIcon = {
                        Icon(
                            Icons.Rounded.Title,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                FontStyleRow(fontInfo = selectedFont,
                    boldChecked = sourceBoldChecked,
                    italicChecked = sourceItalicChecked,
                    onWeightChange = {
                        sourceBoldChecked = it
                        if (it && selectedFont.supportVariableWeight) {
                            quoteBoldChecked = false
                            if (selectedFont.supportVariableSlant) {
                                quoteItalicChecked = false
                                sourceItalicChecked = false
                            }
                            variableValueRange =
                                (selectedFont.variableWeight?.range?.start?.toFloat()
                                    ?: FontWeight.Normal.weight.toFloat())..
                                        (selectedFont.variableWeight?.range?.endInclusive?.toFloat()
                                            ?: FontWeight.Bold.weight.toFloat())
                        } else {
                            onSourceWeightChange(
                                if (it) FontWeight.Bold.weight
                                else FontWeight.Normal.weight
                            )
                        }
                    },
                    onItalicChange = {
                        sourceItalicChecked = it
                        if (it && selectedFont.supportVariableSlant) {
                            quoteItalicChecked = false
                            if (selectedFont.supportVariableWeight) {
                                quoteBoldChecked = false
                                sourceBoldChecked = false
                            }
                            variableValueRange =
                                (selectedFont.variableSlant?.range?.start
                                    ?: FontStyle.Normal.value.toFloat())..
                                        (selectedFont.variableSlant?.range?.endInclusive
                                            ?: FontStyle.Italic.value.toFloat())
                        } else {
                            onSourceItalicChange(
                                if (it) FontStyle.Italic.value.toFloat()
                                else FontStyle.Normal.value.toFloat()
                            )
                        }
                    })
            }
        }
        val textVariableWeightChecked = selectedFont.supportVariableWeight && quoteBoldChecked
        val textVariableItalicChecked = selectedFont.supportVariableSlant && quoteItalicChecked
        val sourceVariableWeightChecked = selectedFont.supportVariableWeight && sourceBoldChecked
        val sourceVariableItalicChecked = selectedFont.supportVariableSlant && sourceItalicChecked
        if (selectedFont.supportVariableWeight && (quoteBoldChecked || sourceBoldChecked)
            || selectedFont.supportVariableSlant && (quoteItalicChecked || sourceItalicChecked)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                shape = MaterialTheme.shapes.extraSmall, border = BorderStroke(
                    Dp.Hairline,
                    MaterialTheme.colorScheme.outline.copy(alpha = ContentAlpha.disabled)
                )
            ) {
                Column(
                    modifier = Modifier
                        .wrapContentHeight()
                        .padding(start = 16.dp, top = 8.dp, end = 16.dp)
                ) {
                    val variableItalic = textVariableItalicChecked || sourceVariableItalicChecked
                    val variableValue = when {
                        textVariableWeightChecked -> textWeightVariableValue
                        textVariableItalicChecked -> textItalicVariableValue
                        sourceVariableWeightChecked -> sourceWeightVariableValue
                        sourceVariableItalicChecked -> sourceItalicVariableValue
                        else -> 0f
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(
                                if (variableItalic) R.string.quote_card_style_variable_font_italic
                                else R.string.quote_card_style_variable_font_weight
                            ),
                            color = AlertDialogDefaults.textContentColor,
                            fontSize = MaterialTheme.typography.bodySmall.fontSize
                        )

                        Text(
                            text = (if (variableItalic) "%.2f" else "%.0f").format(variableValue),
                            color = AlertDialogDefaults.textContentColor,
                            fontSize = MaterialTheme.typography.bodySmall.fontSize
                        )
                    }
                    Slider(value = variableValue,
                        valueRange = variableValueRange,
                        onValueChange = {
                            when {
                                textVariableWeightChecked -> textWeightVariableValue =
                                    it.roundToInt().toFloat()

                                textVariableItalicChecked -> textItalicVariableValue = it
                                sourceVariableWeightChecked -> sourceWeightVariableValue =
                                    it.roundToInt().toFloat()

                                sourceVariableItalicChecked -> sourceItalicVariableValue = it
                            }
                        },
                        onValueChangeFinished = {
                            when {
                                textVariableWeightChecked -> onQuoteWeightChange(
                                    textWeightVariableValue.roundToInt()
                                )

                                textVariableItalicChecked -> onQuoteItalicChange(
                                    textItalicVariableValue
                                )

                                sourceVariableWeightChecked -> onSourceWeightChange(
                                    sourceWeightVariableValue.roundToInt()
                                )

                                sourceVariableItalicChecked -> onSourceItalicChange(
                                    sourceItalicVariableValue
                                )
                            }
                        })
                }
            }
        }
    }
}

@Composable
fun FontStyleRow(
    fontInfo: FontInfo,
    boldChecked: Boolean = false,
    italicChecked: Boolean = false,
    onWeightChange: (Boolean) -> Unit,
    onItalicChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        var internalBoldChecked by remember(boldChecked) { mutableStateOf(boldChecked) }
        OutlinedIconToggleButton(
            checked = internalBoldChecked,
            onCheckedChange = { internalBoldChecked = it; onWeightChange(it) },
            modifier = Modifier
                .height(36.dp)
                .weight(1F),
            shape = MaterialTheme.shapes.extraSmall,
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = ContentAlpha.disabled)
            ),
            colors = IconButtonDefaults.outlinedIconToggleButtonColors(
                contentColor = MaterialTheme.colorScheme.primary,
                checkedContainerColor = MaterialTheme.colorScheme.primary,
                checkedContentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            if (fontInfo.supportVariableWeight) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_variable_weight_24dp),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    Icons.Rounded.FormatBold,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        var internalItalicChecked by remember(italicChecked) { mutableStateOf(italicChecked) }
        OutlinedIconToggleButton(
            checked = internalItalicChecked,
            onCheckedChange = { internalItalicChecked = it; onItalicChange(it) },
            modifier = Modifier
                .height(36.dp)
                .weight(1F),
            shape = MaterialTheme.shapes.extraSmall,
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = ContentAlpha.disabled)
            ),
            colors = IconButtonDefaults.outlinedIconToggleButtonColors(
                contentColor = MaterialTheme.colorScheme.primary,
                checkedContainerColor = MaterialTheme.colorScheme.primary,
                checkedContentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            if (fontInfo.supportVariableSlant) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_variable_italic_24dp),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    Icons.Rounded.FormatItalic,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}