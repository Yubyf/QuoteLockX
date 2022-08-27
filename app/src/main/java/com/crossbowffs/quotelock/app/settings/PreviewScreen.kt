@file:OptIn(ExperimentalUnitApi::class)

package com.crossbowffs.quotelock.app.settings

import android.content.res.Configuration
import android.graphics.Typeface
import androidx.compose.foundation.layout.*
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.crossbowffs.quotelock.consts.PREF_COMMON_FONT_SIZE_SOURCE_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_COMMON_FONT_SIZE_TEXT_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_QUOTE_SOURCE_PREFIX
import com.crossbowffs.quotelock.ui.components.PreferenceTitle
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import com.yubyf.quotelockx.R


@Composable
fun PreviewRoute(
    modifier: Modifier = Modifier,
    viewModel: PreviewViewModel = hiltViewModel(),
) {
    val uiState: PreviewUiState by viewModel.uiState.collectAsState()
    PreviewScreen(modifier, uiState)
}

@Composable
fun PreviewScreen(
    modifier: Modifier = Modifier,
    uiState: PreviewUiState,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        PreferenceTitle(titleRes = R.string.preview)
        QuoteLayout(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            quote = uiState.quoteViewData.text,
            quoteSize = uiState.quoteStyle.quoteSize.toFloat(),
            quoteTypeface = uiState.quoteStyle.quoteTypeface,
            source = uiState.quoteViewData.source,
            sourceSize = uiState.quoteStyle.sourceSize.toFloat(),
            sourceTypeface = uiState.quoteStyle.sourceTypeface,
            quoteSpacing = uiState.quoteStyle.quoteSpacing.dp,
            paddingTop = uiState.quoteStyle.paddingTop.dp,
            paddingBottom = uiState.quoteStyle.paddingBottom.dp
        )
    }
}

@Composable
fun QuoteLayout(
    modifier: Modifier = Modifier,
    quote: String?,
    quoteSize: Float = PREF_COMMON_FONT_SIZE_TEXT_DEFAULT.toFloat(),
    quoteTypeface: Typeface? = null,
    source: String? = null,
    sourceSize: Float = PREF_COMMON_FONT_SIZE_SOURCE_DEFAULT.toFloat(),
    sourceTypeface: Typeface? = null,
    quoteSpacing: Dp = 0.dp,
    paddingTop: Dp = 0.dp,
    paddingBottom: Dp = 0.dp,
) {
    OutlinedCard(
        modifier = modifier.padding(start = 24.dp, end = 24.dp),
        shape = ShapeDefaults.Small
    ) {
        Column(modifier = Modifier.padding(start = 8.dp,
            top = paddingTop,
            end = 8.dp,
            bottom = paddingBottom),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = quote ?: "",
                modifier = Modifier.fillMaxWidth(),
                fontSize = TextUnit(quoteSize, TextUnitType.Sp),
                fontFamily = quoteTypeface?.let { FontFamily(it) },
            )
            if (!source.isNullOrBlank()) {
                Text(
                    text = source,
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(top = quoteSpacing),
                    fontSize = TextUnit(sourceSize, TextUnitType.Sp),
                    fontFamily = sourceTypeface?.let { FontFamily(it) },
                )
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