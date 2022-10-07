@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalAnimationGraphicsApi::class)

package com.crossbowffs.quotelock.app.detail

import android.content.res.Configuration
import android.graphics.Typeface
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumedWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.crossbowffs.quotelock.app.detail.style.CardStylePopup
import com.crossbowffs.quotelock.app.detail.style.CardStyleViewModel
import com.crossbowffs.quotelock.consts.PREF_QUOTE_CARD_ELEVATION_DP
import com.crossbowffs.quotelock.data.api.CardStyle
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.data.api.QuoteDataWithCollectState
import com.crossbowffs.quotelock.data.api.isQuoteGeneratedByApp
import com.crossbowffs.quotelock.data.api.withCollectState
import com.crossbowffs.quotelock.ui.components.DetailAppBar
import com.crossbowffs.quotelock.ui.components.SnapshotCard
import com.crossbowffs.quotelock.ui.components.SnapshotText
import com.crossbowffs.quotelock.ui.components.Snapshotables
import com.crossbowffs.quotelock.ui.components.rememberCardSnapshotState
import com.crossbowffs.quotelock.ui.components.rememberContainerSnapshotState
import com.crossbowffs.quotelock.ui.components.rememberTextSnapshotState
import com.crossbowffs.quotelock.ui.components.verticalFadingEdge
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import com.yubyf.quotelockx.R
import kotlin.math.roundToInt


@Composable
fun QuoteDetailRoute(
    modifier: Modifier = Modifier,
    quote: String,
    source: String?,
    author: String?,
    initialCollectState: Boolean? = null,
    detailViewModel: QuoteDetailViewModel = hiltViewModel(),
    cardStyleViewModel: CardStyleViewModel = hiltViewModel(),
    onFontCustomize: () -> Unit,
    onShare: () -> Unit,
    onBack: () -> Unit,
) {
    val quoteData = QuoteData(quote, source.orEmpty(), author.orEmpty())
    detailViewModel.quoteData = quoteData
    val uiState by detailViewModel.uiState
    val cardStyleUiState by cardStyleViewModel.uiState
    if (initialCollectState == null && uiState.collectState == null) {
        detailViewModel.queryQuoteCollectState()
    }
    QuoteDetailScreen(modifier,
        quoteData.withCollectState(uiState.collectState ?: initialCollectState),
        uiState,
        onCollectClick = detailViewModel::switchCollectionState,
        onStyle = cardStyleViewModel::showStylePopup,
        onShare = { detailViewModel.setSnapshotables(it); onShare() },
        onBack = onBack
    ) {
        CardStylePopup(
            popped = cardStyleUiState.show,
            fonts = cardStyleUiState.fonts,
            cardStyle = cardStyleUiState.cardStyle,
            onFontSelected = cardStyleViewModel::selectFontFamily,
            onFontAdd = onFontCustomize,
            onQuoteSizeChange = cardStyleViewModel::setQuoteSize,
            onSourceSizeChange = cardStyleViewModel::setSourceSize,
            onLineSpacingChange = cardStyleViewModel::setLineSpacing,
            onCardPaddingChange = cardStyleViewModel::setCardPadding,
            onQuoteWeightChange = cardStyleViewModel::setQuoteWeight,
            onQuoteItalicChange = cardStyleViewModel::setQuoteItalic,
            onSourceWeightChange = cardStyleViewModel::setSourceWeight,
            onSourceItalicChange = cardStyleViewModel::setSourceItalic,
            onDismiss = cardStyleViewModel::dismissStylePopup
        )
    }
}

@Composable
fun QuoteDetailScreen(
    modifier: Modifier = Modifier,
    quoteData: QuoteDataWithCollectState,
    uiState: QuoteDetailUiState,
    onCollectClick: (QuoteDataWithCollectState) -> Unit,
    onStyle: () -> Unit,
    onShare: (Snapshotables) -> Unit = {},
    onBack: () -> Unit,
    popupContent: @Composable () -> Unit = {},
) {
    val snapshotStates = Snapshotables()
    Scaffold(
        topBar = { DetailAppBar(onStyle = onStyle, onBackPressed = onBack) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(text = stringResource(id = R.string.quote_image_share)) },
                icon = {
                    Icon(Icons.Rounded.Share,
                        contentDescription = stringResource(id = R.string.quote_image_share_description))
                },
                shape = FloatingActionButtonDefaults.largeShape,
                onClick = { onShare(snapshotStates) }
            )
        },
        floatingActionButtonPosition = FabPosition.End
    ) { internalPadding ->
        Box(modifier = modifier
            .fillMaxSize()
            .padding(internalPadding)
            .consumedWindowInsets(internalPadding)
        ) {
            QuoteDetailPage(
                quoteData = quoteData,
                cardStyle = uiState.cardStyle,
                snapshotStates = snapshotStates,
                onCollectClick = onCollectClick,
            )
            popupContent()
        }
    }
}

@Composable
fun QuoteDetailPage(
    modifier: Modifier = Modifier,
    quoteData: QuoteDataWithCollectState,
    cardStyle: CardStyle = CardStyle(),
    snapshotStates: Snapshotables = Snapshotables(),
    onCollectClick: (QuoteDataWithCollectState) -> Unit,
    onShareCard: ((Snapshotables) -> Unit)? = null,
) {
    val extraPadding = 64.dp
    var containerHeight by remember {
        mutableStateOf(0)
    }
    var contentSize by remember {
        mutableStateOf(IntSize.Zero)
    }
    val includeExtraPadding =
        contentSize.height + with(LocalDensity.current) { extraPadding.toPx() } >= containerHeight
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalFadingEdge(scrollState = scrollState, length = 72.dp)
            .verticalScroll(scrollState)
            .onGloballyPositioned { containerHeight = it.size.height }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.Center
    ) {
        val quoteGeneratedByApp =
            if (LocalInspectionMode.current) false else LocalContext.current.isQuoteGeneratedByApp(
                quoteData.quoteText,
                quoteData.quoteSource,
                quoteData.quoteAuthor)
        QuoteCard(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(
                    top = 16.dp,
                    bottom = 16.dp + if (includeExtraPadding) extraPadding else 0.dp
                )
                .onSizeChanged { contentSize = it },
            quote = quoteData.quoteText,
            source = if (quoteGeneratedByApp) quoteData.readableSource
            else quoteData.readableSourceWithPrefix,
            quoteSize = cardStyle.quoteSize.sp,
            sourceSize = cardStyle.sourceSize.sp,
            lineSpacing = cardStyle.lineSpacing.dp,
            cardPadding = cardStyle.cardPadding.dp,
            quoteTypeface = cardStyle.quoteFontStyle.typeface,
            sourceTypeface = cardStyle.sourceFontStyle.typeface,
            quoteWeight = cardStyle.quoteFontStyle.weight,
            quoteStyle = if (cardStyle.quoteFontStyle.italic.roundToInt() == FontStyle.Italic.value)
                FontStyle.Italic else FontStyle.Normal,
            sourceWeight = cardStyle.sourceFontStyle.weight,
            sourceStyle = if (cardStyle.sourceFontStyle.italic.roundToInt() == FontStyle.Italic.value)
                FontStyle.Italic else FontStyle.Normal,
            minHeight = if (!LocalInspectionMode.current) {
                with(LocalDensity.current) { max(containerHeight.toDp(), 320.dp) * 0.6F }
            } else 320.dp,
            snapshotStates = snapshotStates,
            currentCollectState = quoteData.collectState ?: false,
            onCollectClick = if (!quoteGeneratedByApp) {
                { onCollectClick(quoteData) }
            } else null,
            onShareCard = onShareCard
        )
        if (LocalInspectionMode.current || !includeExtraPadding) {
            Spacer(modifier = Modifier.height(extraPadding))
        }
    }
}

@Composable
fun QuoteCard(
    modifier: Modifier = Modifier,
    quote: String,
    source: String?,
    quoteSize: TextUnit,
    sourceSize: TextUnit,
    lineSpacing: Dp,
    cardPadding: Dp,
    quoteTypeface: Typeface? = Typeface.DEFAULT,
    sourceTypeface: Typeface? = Typeface.DEFAULT,
    quoteWeight: FontWeight = FontWeight.Normal,
    quoteStyle: FontStyle = FontStyle.Normal,
    sourceWeight: FontWeight = FontWeight.Normal,
    sourceStyle: FontStyle = FontStyle.Normal,
    minHeight: Dp = 0.dp,
    snapshotStates: Snapshotables = Snapshotables(),
    currentCollectState: Boolean = false,
    onCollectClick: (() -> Unit)? = null,
    onShareCard: ((Snapshotables) -> Unit)? = null,
) {
    val containerColor = QuoteLockTheme.quotelockColors.quoteCardSurface
    val contentColor = QuoteLockTheme.quotelockColors.quoteCardOnSurface
    SnapshotCard(
        modifier = modifier
            .heightIn(min = minHeight),
        containerColor = containerColor,
        contentColor = contentColor,
        elevation = PREF_QUOTE_CARD_ELEVATION_DP.dp,
        cornerSize = 8.dp,
        contentAlignment = Alignment.Center,
        rememberCardSnapshotState("card").also { snapshotStates += it }
    ) {
        // Text container position snapshot
        val textContainerSnapshotable = rememberContainerSnapshotState("text_container", false)
        snapshotStates += textContainerSnapshotable
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = cardPadding, vertical = cardPadding + 24.dp)
                .align(alignment = Alignment.Center)
                .onGloballyPositioned { textContainerSnapshotable.region = it.boundsInParent() },
            horizontalAlignment = Alignment.End,
        ) {
            SnapshotText(text = quote,
                fontSize = quoteSize,
                fontFamily = quoteTypeface,
                fontWeight = quoteWeight,
                fontStyle = quoteStyle,
                lineHeight = 1.3F.em,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                snapshotable = rememberTextSnapshotState("quote", false)
                    .also { snapshotStates += it }
            )
            if (!source.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(lineSpacing))
                SnapshotText(text = source,
                    fontSize = sourceSize,
                    fontFamily = sourceTypeface,
                    fontWeight = sourceWeight,
                    fontStyle = sourceStyle,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .wrapContentWidth()
                        .animateContentSize(),
                    snapshotable = rememberTextSnapshotState("source", false)
                        .also { snapshotStates += it }
                )
            }
        }
        Row(modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(top = 8.dp, end = 8.dp)) {
            val animStar =
                AnimatedImageVector.animatedVectorResource(id = R.drawable.avd_star_unselected_to_selected)
            onCollectClick?.let {
                val haptic = LocalHapticFeedback.current
                IconButton(
                    modifier = Modifier.size(36.dp),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        it()
                    }) {
                    Icon(painter = rememberAnimatedVectorPainter(animStar, currentCollectState),
                        contentDescription = "Collect")
                }
            }
            onShareCard?.let {
                IconButton(modifier = Modifier.size(36.dp),
                    onClick = { onShareCard(snapshotStates) }) {
                    Icon(Icons.Rounded.Share,
                        contentDescription = stringResource(id = R.string.quote_image_share_description),
                        modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

class QuotePreviewParameterProvider : PreviewParameterProvider<QuoteDataWithCollectState> {
    override val values: Sequence<QuoteDataWithCollectState> = sequenceOf(
        QuoteDataWithCollectState(
            "落霞与孤鹜齐飞，秋水共长天一色",
            "《滕王阁序》",
            "王勃",
            true
        ),
        QuoteDataWithCollectState(
            "Knowledge is power.",
            "Francis Bacon",
            "",
            false
        ),
    )
}

@Preview(name = "Quote Card Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Quote Card Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun QuoteCardPreview(
    @PreviewParameter(QuotePreviewParameterProvider::class) quote: QuoteDataWithCollectState,
) {
    QuoteLockTheme {
        Surface {
            QuoteCard(
                quote = quote.quoteText,
                source = quote.readableSourceWithPrefix,
                quoteSize = 36.sp,
                sourceSize = 36.sp,
                lineSpacing = 36.dp,
                cardPadding = 36.dp,
                minHeight = 240.dp,
                onCollectClick = {},
                onShareCard = {}
            )
        }
    }
}

@Preview(name = "Quote Page Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun QuotePagePreview() {
    QuoteLockTheme {
        Surface {
            QuoteDetailPage(
                quoteData = QuoteDataWithCollectState(
                    "落霞与孤鹜齐飞，秋水共长天一色",
                    "《滕王阁序》",
                    "王勃",
                    true
                ),
                onCollectClick = {},
                onShareCard = {}
            )
        }
    }
}

@Preview(name = "Detail Screen Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Detail Screen Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DetailScreenPreview() {
    QuoteLockTheme {
        Surface {
            QuoteDetailScreen(
                quoteData = QuoteDataWithCollectState(
                    "落霞与孤鹜齐飞，秋水共长天一色",
                    "《滕王阁序》",
                    "王勃",
                    true
                ),
                uiState = QuoteDetailUiState(CardStyle()),
                onCollectClick = {},
                onStyle = {},
                onBack = {}
            )
        }
    }
}