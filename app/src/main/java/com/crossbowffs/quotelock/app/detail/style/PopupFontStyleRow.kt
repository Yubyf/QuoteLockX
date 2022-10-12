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
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_FONT_SIZE_SOURCE_MAX
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_FONT_SIZE_SOURCE_MIN
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_FONT_SIZE_SOURCE_STEP
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_FONT_SIZE_TEXT_MAX
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_FONT_SIZE_TEXT_MIN
import com.crossbowffs.quotelock.consts.PREF_CARD_STYLE_FONT_SIZE_TEXT_STEP
import com.crossbowffs.quotelock.data.api.CardStyle
import com.crossbowffs.quotelock.ui.components.ContentAlpha
import com.yubyf.quotelockx.R
import kotlin.math.roundToInt

private enum class VariableCheckedType {
    QUOTE_WEIGHT,
    QUOTE_SLANT,
    SOURCE_WEIGHT,
    SOURCE_SLANT,
    NONE
}

@Composable
internal fun PopupFontStyleRow(
    modifier: Modifier = Modifier,
    cardStyle: CardStyle,
    onQuoteSizeChange: (Int) -> Unit,
    onSourceSizeChange: (Int) -> Unit,
    onQuoteWeightChange: (Int) -> Unit,
    onQuoteItalicChange: (Float) -> Unit,
    onSourceWeightChange: (Int) -> Unit,
    onSourceItalicChange: (Float) -> Unit,
) {
    Column(modifier = modifier) {
        var variableCheckedType by remember { mutableStateOf(VariableCheckedType.NONE) }
        if ((variableCheckedType == VariableCheckedType.QUOTE_WEIGHT
                    && !cardStyle.quoteFontStyle.supportVariableWeight)
            || (variableCheckedType == VariableCheckedType.QUOTE_SLANT
                    && !cardStyle.quoteFontStyle.supportVariableSlant)
            || (variableCheckedType == VariableCheckedType.SOURCE_WEIGHT
                    && !cardStyle.sourceFontStyle.supportVariableWeight)
            || (variableCheckedType == VariableCheckedType.SOURCE_SLANT
                    && !cardStyle.sourceFontStyle.supportVariableSlant)
        ) {
            variableCheckedType = VariableCheckedType.NONE
        }
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
                val supportVariableWeight = cardStyle.quoteFontStyle.supportVariableWeight
                val supportVariableSlant = cardStyle.quoteFontStyle.supportVariableSlant
                FontStyleRow(
                    supportVariableWeight = supportVariableWeight,
                    supportVariableSlant = supportVariableSlant,
                    boldChecked = variableCheckedType == VariableCheckedType.QUOTE_WEIGHT
                            || !supportVariableWeight && cardStyle.quoteFontStyle.weight == FontWeight.Bold,
                    italicChecked = variableCheckedType == VariableCheckedType.QUOTE_SLANT
                            || cardStyle.quoteFontStyle.italic.roundToInt() == FontStyle.Italic.value,
                    onWeightChange = {
                        if (it && supportVariableWeight) {
                            variableCheckedType = VariableCheckedType.QUOTE_WEIGHT
                        } else if (!supportVariableWeight) {
                            onQuoteWeightChange(
                                if (it) FontWeight.Bold.weight
                                else FontWeight.Normal.weight
                            )
                        } else {
                            variableCheckedType = VariableCheckedType.NONE
                        }
                    },
                    onItalicChange = {
                        if (it && supportVariableSlant) {
                            variableCheckedType = VariableCheckedType.QUOTE_SLANT
                        } else if (!supportVariableSlant) {
                            onQuoteItalicChange(
                                if (it) FontStyle.Italic.value.toFloat()
                                else FontStyle.Normal.value.toFloat()
                            )
                        } else {
                            variableCheckedType = VariableCheckedType.NONE
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
                val supportVariableWeight = cardStyle.sourceFontStyle.supportVariableWeight
                val supportVariableSlant = cardStyle.sourceFontStyle.supportVariableSlant
                FontStyleRow(
                    supportVariableWeight = supportVariableWeight,
                    supportVariableSlant = supportVariableSlant,
                    boldChecked = variableCheckedType == VariableCheckedType.SOURCE_WEIGHT
                            || !supportVariableWeight && cardStyle.sourceFontStyle.weight == FontWeight.Bold,
                    italicChecked = variableCheckedType == VariableCheckedType.SOURCE_SLANT
                            || cardStyle.sourceFontStyle.italic.roundToInt() == FontStyle.Italic.value,
                    onWeightChange = {
                        if (it && supportVariableWeight) {
                            variableCheckedType = VariableCheckedType.SOURCE_WEIGHT
                        } else if (!supportVariableWeight) {
                            onSourceWeightChange(
                                if (it) FontWeight.Bold.weight
                                else FontWeight.Normal.weight
                            )
                        } else {
                            variableCheckedType = VariableCheckedType.NONE
                        }
                    },
                    onItalicChange = {
                        if (it && supportVariableSlant) {
                            variableCheckedType = VariableCheckedType.SOURCE_SLANT
                        } else if (!supportVariableSlant) {
                            onSourceItalicChange(
                                if (it) FontStyle.Italic.value.toFloat()
                                else FontStyle.Normal.value.toFloat()
                            )
                        } else {
                            variableCheckedType = VariableCheckedType.NONE
                        }
                    })
            }
        }
        if (variableCheckedType != VariableCheckedType.NONE) {
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
                    val variableItalic = variableCheckedType == VariableCheckedType.QUOTE_SLANT
                            || variableCheckedType == VariableCheckedType.SOURCE_SLANT
                    var quoteWeightVariableValue by remember(cardStyle.quoteFontStyle.weight) {
                        mutableStateOf(cardStyle.quoteFontStyle.weight.weight.toFloat())
                    }
                    var sourceWeightVariableValue by remember(cardStyle.sourceFontStyle.weight) {
                        mutableStateOf(cardStyle.sourceFontStyle.weight.weight.toFloat())
                    }
                    var quoteSlantVariableValue by remember(cardStyle.quoteFontStyle.italic) {
                        mutableStateOf(cardStyle.quoteFontStyle.italic)
                    }
                    var sourceSlantVariableValue by remember(cardStyle.sourceFontStyle.italic) {
                        mutableStateOf(cardStyle.sourceFontStyle.italic)
                    }
                    val variableValue = when (variableCheckedType) {
                        VariableCheckedType.QUOTE_WEIGHT -> quoteWeightVariableValue
                        VariableCheckedType.QUOTE_SLANT -> quoteSlantVariableValue
                        VariableCheckedType.SOURCE_WEIGHT -> sourceWeightVariableValue
                        VariableCheckedType.SOURCE_SLANT -> sourceSlantVariableValue
                        VariableCheckedType.NONE -> 0f
                    }
                    val variableValueRange = when (variableCheckedType) {
                        VariableCheckedType.QUOTE_WEIGHT ->
                            cardStyle.quoteFontStyle.minWeight.weight.toFloat()..
                                    cardStyle.quoteFontStyle.maxWeight.weight.toFloat()

                        VariableCheckedType.QUOTE_SLANT ->
                            cardStyle.quoteFontStyle.minSlant..cardStyle.quoteFontStyle.maxSlant

                        VariableCheckedType.SOURCE_WEIGHT ->
                            cardStyle.sourceFontStyle.minWeight.weight.toFloat()..
                                    cardStyle.sourceFontStyle.maxWeight.weight.toFloat()

                        VariableCheckedType.SOURCE_SLANT ->
                            cardStyle.sourceFontStyle.minSlant..cardStyle.sourceFontStyle.maxSlant

                        VariableCheckedType.NONE -> 0f..0f
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
                            when (variableCheckedType) {
                                VariableCheckedType.QUOTE_WEIGHT -> quoteWeightVariableValue =
                                    it.roundToInt().toFloat()

                                VariableCheckedType.QUOTE_SLANT -> quoteSlantVariableValue = it
                                VariableCheckedType.SOURCE_WEIGHT -> sourceWeightVariableValue =
                                    it.roundToInt().toFloat()

                                VariableCheckedType.SOURCE_SLANT -> sourceSlantVariableValue = it
                                VariableCheckedType.NONE -> {}
                            }
                        },
                        onValueChangeFinished = {
                            when (variableCheckedType) {
                                VariableCheckedType.QUOTE_WEIGHT -> onQuoteWeightChange(
                                    quoteWeightVariableValue.roundToInt()
                                )

                                VariableCheckedType.QUOTE_SLANT -> onQuoteItalicChange(
                                    quoteSlantVariableValue
                                )

                                VariableCheckedType.SOURCE_WEIGHT -> onSourceWeightChange(
                                    sourceWeightVariableValue.roundToInt()
                                )

                                VariableCheckedType.SOURCE_SLANT -> onSourceItalicChange(
                                    sourceSlantVariableValue
                                )

                                VariableCheckedType.NONE -> {}
                            }
                        })
                }
            }
        }
    }
}

@Composable
fun FontStyleRow(
    supportVariableWeight: Boolean = false,
    supportVariableSlant: Boolean = false,
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
            if (supportVariableWeight) {
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
            if (supportVariableSlant) {
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