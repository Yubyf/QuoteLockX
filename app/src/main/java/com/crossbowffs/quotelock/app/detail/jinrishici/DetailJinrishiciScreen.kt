@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.crossbowffs.quotelock.app.detail.jinrishici

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumedWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.crossbowffs.quotelock.data.api.CardStyle
import com.crossbowffs.quotelock.data.api.composeFontFamily
import com.crossbowffs.quotelock.data.modules.jinrishici.detail.JinrishiciDetailData
import com.crossbowffs.quotelock.ui.components.DetailTopBar
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import com.yubyf.quotelockx.R

@Composable
fun DetailJinrishiciRoute(
    modifier: Modifier = Modifier,
    detailData: JinrishiciDetailData?,
    viewModel: DetailJinrishiciViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val cardStyle = viewModel.uiState.value.cardStyle
    DetailJinrishiciScreen(
        modifier = modifier,
        cardStyle = cardStyle,
        detailData = detailData,
        onBack = onBack,
    )
}

@Composable
fun DetailJinrishiciScreen(
    modifier: Modifier = Modifier,
    cardStyle: CardStyle,
    detailData: JinrishiciDetailData?,
    onBack: () -> Unit = {},
) {
    Scaffold(
        topBar = { DetailTopBar(onBack) },
    ) { internalPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(internalPadding)
                .consumedWindowInsets(internalPadding)
        ) {
            detailData?.let {
                DetailJinrishiciContent(
                    modifier = modifier,
                    cardStyle = cardStyle,
                    detailData = it,
                )
            } ?: run {
                BlankContent()
            }
        }
    }
}

@Composable
fun BlankContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        Text(text = stringResource(R.string.no_data), modifier = modifier.align(Alignment.Center))
    }
}

@Composable
fun DetailJinrishiciContent(
    modifier: Modifier = Modifier,
    cardStyle: CardStyle,
    detailData: JinrishiciDetailData,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = detailData.title,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontFamily = cardStyle.quoteFontStyle.composeFontFamily,
            fontSize = MaterialTheme.typography.headlineMedium.fontSize,
            fontWeight = FontWeight.Bold,
            lineHeight = MaterialTheme.typography.headlineMedium.lineHeight,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .wrapContentSize()
        ) {
            Text(
                text = "〔${detailData.dynasty}〕",
                modifier = Modifier.align(Alignment.CenterVertically),
                fontFamily = cardStyle.quoteFontStyle.composeFontFamily,
                fontSize = MaterialTheme.typography.bodySmall.fontSize
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = detailData.author,
                modifier = Modifier.align(Alignment.CenterVertically),
                fontFamily = cardStyle.quoteFontStyle.composeFontFamily,
                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        detailData.content.forEachIndexed { index, string ->
            Text(
                text = string,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                fontFamily = cardStyle.quoteFontStyle.composeFontFamily,
                fontSize = 18.sp,
                lineHeight = 28.sp
            )
            if (index != detailData.content.lastIndex) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(
    name = "Blank Detail Jinrishici Screen Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Blank Detail Jinrishici Screen Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun BlankQuoteScreenPreview() {
    QuoteLockTheme {
        Surface {
            DetailJinrishiciScreen(
                cardStyle = CardStyle(),
                detailData = null
            )
        }
    }
}

@Preview(
    name = "Detail Jinrishici Screen Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Detail Jinrishici Screen Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun QuoteScreenPreview() {
    QuoteLockTheme {
        Surface {
            DetailJinrishiciScreen(
                cardStyle = CardStyle(),
                detailData = JinrishiciDetailData(
                    title = "梅花",
                    author = "王安石",
                    dynasty = "宋代",
                    content = listOf(
                        "墙角数枝梅，凌寒独自开。",
                        "遥知不是雪，为有暗香来。"
                    ),
                    translate = listOf(
                        "那墙角的几枝梅花，冒着严寒独自盛开。",
                        "为什么远望就知道洁白的梅花不是雪呢？因为梅花隐隐传来阵阵的香气。"
                    ),
                    tags = listOf("冬")
                )
            )
        }
    }
}