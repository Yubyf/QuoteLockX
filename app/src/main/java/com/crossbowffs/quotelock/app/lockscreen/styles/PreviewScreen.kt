@file:OptIn(ExperimentalMaterial3Api::class)

package com.crossbowffs.quotelock.app.lockscreen.styles

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.crossbowffs.quotelock.consts.PREF_COMMON_FONT_SIZE_SOURCE_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_COMMON_FONT_SIZE_TEXT_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_QUOTE_SOURCE_PREFIX
import com.crossbowffs.quotelock.data.api.QuoteDataWithCollectState
import com.crossbowffs.quotelock.data.api.QuoteViewData
import com.crossbowffs.quotelock.data.api.composeFontFamily
import com.crossbowffs.quotelock.data.api.isQuoteGeneratedByConfiguration
import com.crossbowffs.quotelock.ui.components.PreferenceTitle
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import com.yubyf.quotelockx.R


@Composable
fun PreviewRoute(
    modifier: Modifier = Modifier,
    viewModel: PreviewViewModel = hiltViewModel(),
    onPreviewClick: (QuoteDataWithCollectState) -> Unit,
) {
    val uiState: PreviewUiState by viewModel.uiState.collectAsState()
    PreviewScreen(modifier, uiState, onPreviewClick)
}

@Composable
fun PreviewScreen(
    modifier: Modifier = Modifier,
    uiState: PreviewUiState,
    onPreviewClick: (QuoteDataWithCollectState) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        val quoteGeneratedByApp =
            LocalContext.current.isQuoteGeneratedByConfiguration(
                uiState.quoteData.quoteText,
                uiState.quoteData.quoteSource,
                uiState.quoteData.quoteAuthor
            )
        PreferenceTitle(titleRes = R.string.preview)
        QuoteLayout(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            quote = uiState.quoteData.quoteText,
            quoteSize = uiState.quoteStyle.quoteSize.toFloat(),
            quoteFamily = uiState.quoteStyle.quoteFontStyle.composeFontFamily,
            quoteFontWeight = uiState.quoteStyle.quoteFontStyle.weight,
            quoteFontStyle = uiState.quoteStyle.quoteFontStyle.composeFontStyle,
            source = if (quoteGeneratedByApp) uiState.quoteData.readableSource
            else uiState.quoteData.readableSourceWithPrefix,
            sourceSize = uiState.quoteStyle.sourceSize.toFloat(),
            sourceFamily = uiState.quoteStyle.sourceFontStyle.composeFontFamily,
            sourceFontWeight = uiState.quoteStyle.sourceFontStyle.weight,
            sourceFontStyle = uiState.quoteStyle.sourceFontStyle.composeFontStyle,
            quoteSpacing = uiState.quoteStyle.quoteSpacing.dp,
            paddingTop = uiState.quoteStyle.paddingTop.dp,
            paddingBottom = uiState.quoteStyle.paddingBottom.dp,
            enabled = !quoteGeneratedByApp,
            onClick = { onPreviewClick(uiState.quoteData) }
        )
    }
}

@Composable
fun QuoteLayout(
    modifier: Modifier = Modifier,
    quote: String?,
    quoteSize: Float = PREF_COMMON_FONT_SIZE_TEXT_DEFAULT.toFloat(),
    quoteFamily: FontFamily = FontFamily.Default,
    quoteFontWeight: FontWeight = FontWeight.Normal,
    quoteFontStyle: FontStyle = FontStyle.Normal,
    source: String? = null,
    sourceSize: Float = PREF_COMMON_FONT_SIZE_SOURCE_DEFAULT.toFloat(),
    sourceFamily: FontFamily = FontFamily.Default,
    sourceFontWeight: FontWeight = FontWeight.Normal,
    sourceFontStyle: FontStyle = FontStyle.Normal,
    quoteSpacing: Dp = 0.dp,
    paddingTop: Dp = 0.dp,
    paddingBottom: Dp = 0.dp,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    ElevatedCard(
        modifier = modifier.padding(start = 24.dp, end = 24.dp),
        shape = ShapeDefaults.ExtraSmall,
        colors = CardDefaults.cardColors(),
        elevation = CardDefaults.cardElevation(),
        enabled = enabled,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(start = 8.dp,
            top = paddingTop,
            end = 8.dp,
            bottom = paddingBottom),
            horizontalAlignment = Alignment.End
        ) {
            val localTextStyle = LocalTextStyle.current
            val previewTextStyle by remember {
                derivedStateOf { localTextStyle.copy(fontSynthesis = FontSynthesis.All) }
            }
            CompositionLocalProvider(LocalTextStyle provides previewTextStyle) {
                Text(
                    text = quote ?: "",
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = TextUnit(quoteSize, TextUnitType.Sp),
                    fontFamily = quoteFamily,
                    fontWeight = quoteFontWeight,
                    fontStyle = quoteFontStyle,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!source.isNullOrBlank()) {
                    Text(
                        text = source,
                        modifier = Modifier
                            .wrapContentWidth()
                            .padding(top = quoteSpacing),
                        fontSize = TextUnit(sourceSize, TextUnitType.Sp),
                        fontFamily = sourceFamily,
                        fontWeight = sourceFontWeight,
                        fontStyle = sourceFontStyle
                    )
                }
            }
        }
    }
}

class QuotePreviewParameterProvider : PreviewParameterProvider<QuoteViewData> {
    override val values: Sequence<QuoteViewData> = sequenceOf(
        QuoteViewData("落霞与孤鹜齐飞，秋水共长天一色", "${PREF_QUOTE_SOURCE_PREFIX}王勃 《滕王阁序》"),
        QuoteViewData("Knowledge is power.", "${PREF_QUOTE_SOURCE_PREFIX}Francis Bacon"),
    )
}

@Preview(name = "Quote Layout Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Quote Layout Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun QuoteLayoutPreview(
    @PreviewParameter(QuotePreviewParameterProvider::class) quote: QuoteViewData,
) {
    QuoteLockTheme {
        Surface {
            QuoteLayout(quote = quote.text, source = quote.source)
        }
    }
}