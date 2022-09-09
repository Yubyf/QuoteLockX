@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.crossbowffs.quotelock.app.detail

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Typeface
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.*
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.crossbowffs.quotelock.app.detail.style.CardStylePopup
import com.crossbowffs.quotelock.app.detail.style.CardStyleViewModel
import com.crossbowffs.quotelock.consts.PREF_QUOTE_CARD_ELEVATION_DP
import com.crossbowffs.quotelock.consts.PREF_QUOTE_SOURCE_PREFIX
import com.crossbowffs.quotelock.consts.PREF_SHARE_FILE_AUTHORITY
import com.crossbowffs.quotelock.consts.PREF_SHARE_IMAGE_MIME_TYPE
import com.crossbowffs.quotelock.data.api.CardStyle
import com.crossbowffs.quotelock.ui.components.*
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import com.yubyf.quotelockx.R
import java.io.File


@Composable
fun QuoteDetailRoute(
    modifier: Modifier = Modifier,
    quote: String,
    source: String?,
    detailViewModel: QuoteDetailViewModel = hiltViewModel(),
    cardStyleViewModel: CardStyleViewModel = hiltViewModel(),
    onFontCustomize: () -> Unit,
    onBack: () -> Unit,
) {
    val uiState by detailViewModel.uiState
    val uiEvent by detailViewModel.uiEvent.collectAsState(initial = null)
    val cardStyleUiState by cardStyleViewModel.uiState
    uiEvent?.shareFile?.let { file ->
        LocalContext.current.shareImage(file)
    }
    QuoteDetailScreen(modifier,
        quote,
        source,
        uiState.cardStyle,
        onStyle = cardStyleViewModel::showStylePopup,
        onShareQuote = detailViewModel::shareQuote,
        onBack = onBack
    ) {
        cardStyleUiState.takeIf { cardStyleUiState.show }?.let {
            CardStylePopup(
                fonts = it.fonts,
                cardStyle = it.cardStyle,
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
}

@Composable
fun QuoteDetailScreen(
    modifier: Modifier = Modifier,
    quote: String,
    source: String?,
    cardStyle: CardStyle = CardStyle(),
    onStyle: () -> Unit,
    onShareQuote: (size: Size, block: ((Canvas) -> Unit)) -> Unit = { _, _ -> },
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
                        contentDescription = stringResource(id = R.string.quote_image_share))
                },
                onClick = {
                    snapshotStates.bounds?.let {
                        onShareQuote.invoke(it) { canvas ->
                            snapshotStates.forEach { snapshot ->
                                snapshot.snapshot(canvas)
                            }
                        }
                    }
                }
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
                quote = quote,
                source = source,
                cardStyle = cardStyle,
                snapshotStates = snapshotStates
            )
            popupContent()
        }
    }
}

@Composable
fun QuoteDetailPage(
    modifier: Modifier = Modifier,
    quote: String,
    source: String?,
    cardStyle: CardStyle = CardStyle(),
    snapshotStates: Snapshotables = Snapshotables(),
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .onGloballyPositioned { containerHeight = it.size.height }
            .verticalScroll(state = rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        QuoteCard(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(top = 16.dp,
                    bottom = 16.dp + if (includeExtraPadding) extraPadding else 0.dp)
                .onSizeChanged { contentSize = it },
            quote = quote,
            source = source,
            quoteSize = cardStyle.quoteSize.sp,
            sourceSize = cardStyle.sourceSize.sp,
            lineSpacing = cardStyle.lineSpacing.dp,
            cardPadding = cardStyle.cardPadding.dp,
            typeface = cardStyle.typeface,
            minHeight = if (!LocalInspectionMode.current) {
                with(LocalDensity.current) { max(containerHeight.toDp(), 320.dp) * 0.6F }
            } else 320.dp,
            snapshotStates = snapshotStates
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
        rememberSnapshotState("card").also { snapshotStates += it }
    ) {
        var columnBounds: Rect by remember { mutableStateOf(Rect.Zero) }
        // Text container position snapshot
        snapshotStates += rememberSnapshotState("text_container", false).apply {
            snapshotCallback = { canvas -> canvas.translate(columnBounds.left, columnBounds.top) }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(cardPadding)
                .align(alignment = Alignment.Center)
                .onGloballyPositioned { columnBounds = it.boundsInParent() },
            horizontalAlignment = Alignment.End,
        ) {
            SnapshotText(text = quote,
                fontSize = quoteSize,
                fontFamily = typeface,
                lineHeight = 1.3F.em,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
                snapshotable = rememberSnapshotState("quote", false)
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
                    snapshotable = rememberSnapshotState("source", false)
                        .also { snapshotStates += it }
                )
            }
        }
    }
}

internal fun Context.shareImage(file: File) {
    val imageFileUri: Uri =
        FileProvider.getUriForFile(this, PREF_SHARE_FILE_AUTHORITY, file)
    Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, imageFileUri)
        type = PREF_SHARE_IMAGE_MIME_TYPE
        clipData = ClipData.newRawUri("", imageFileUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }.let { intent ->
        startActivity(Intent.createChooser(intent, "Share Quote"))
    }
}

class QuotePreviewParameterProvider : PreviewParameterProvider<Pair<String, String>> {
    override val values: Sequence<Pair<String, String>> = sequenceOf(
        Pair("落霞与孤鹜齐飞，秋水共长天一色", "${PREF_QUOTE_SOURCE_PREFIX}王勃 《滕王阁序》"),
        Pair("Knowledge is power.", "${PREF_QUOTE_SOURCE_PREFIX}Francis Bacon"),
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
    @PreviewParameter(QuotePreviewParameterProvider::class) quote: Pair<String, String>,
) {
    QuoteLockTheme {
        Surface {
            QuoteCard(
                quote = quote.first,
                source = quote.second,
                quoteSize = 36.sp,
                sourceSize = 36.sp,
                lineSpacing = 36.dp,
                cardPadding = 36.dp,
                minHeight = 240.dp
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
private fun DetailScreenPreview(
    @PreviewParameter(QuotePreviewParameterProvider::class) quote: Pair<String, String>,
) {
    QuoteLockTheme {
        Surface {
            QuoteDetailScreen(quote = quote.first, source = quote.second, onStyle = {}, onBack = {})
        }
    }
}