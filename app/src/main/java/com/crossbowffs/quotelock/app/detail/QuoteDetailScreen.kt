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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.crossbowffs.quotelock.app.detail.style.CardStylePopup
import com.crossbowffs.quotelock.app.detail.style.CardStyleViewModel
import com.crossbowffs.quotelock.consts.PREF_QUOTE_CARD_ELEVATION_DP
import com.crossbowffs.quotelock.data.api.*
import com.crossbowffs.quotelock.ui.components.*
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import com.yubyf.quotelockx.R


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
                .padding(top = 16.dp,
                    bottom = 16.dp + if (includeExtraPadding) extraPadding else 0.dp)
                .onSizeChanged { contentSize = it },
            quote = quoteData.quoteText,
            source = if (quoteGeneratedByApp) quoteData.readableSource
            else quoteData.readableSourceWithPrefix,
            quoteSize = cardStyle.quoteSize.sp,
            sourceSize = cardStyle.sourceSize.sp,
            lineSpacing = cardStyle.lineSpacing.dp,
            cardPadding = cardStyle.cardPadding.dp,
            typeface = cardStyle.typeface,
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
        if (LocalInspectionMode.current || !LocalInspectionMode.current && !includeExtraPadding) {
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
    typeface: Typeface? = Typeface.DEFAULT,
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
                .animateContentSize()
                .padding(horizontal = cardPadding, vertical = cardPadding + 24.dp)
                .align(alignment = Alignment.Center)
                .onGloballyPositioned { textContainerSnapshotable.region = it.boundsInParent() },
            horizontalAlignment = Alignment.End,
        ) {
            SnapshotText(text = quote,
                fontSize = quoteSize,
                fontFamily = typeface,
                lineHeight = 1.3F.em,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
                snapshotable = rememberTextSnapshotState("quote", false)
                    .also { snapshotStates += it }
            )
            if (!source.isNullOrBlank()) {
                SnapshotText(text = source,
                    fontSize = sourceSize,
                    fontFamily = typeface,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(top = lineSpacing),
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