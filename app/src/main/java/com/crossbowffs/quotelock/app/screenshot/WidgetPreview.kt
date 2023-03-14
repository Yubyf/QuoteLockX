package com.crossbowffs.quotelock.app.screenshot

import android.graphics.Typeface
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.crossbowffs.quotelock.ui.components.SnapshotText
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import org.jetbrains.annotations.TestOnly

@TestOnly
@Composable
fun WidgetPreview(text: String, source: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = QuoteLockTheme.quotelockColors.quoteCardOnSurface,
            ),
        contentAlignment = Alignment.Center
    ) {
        var size by remember { mutableStateOf(IntSize.Zero) }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = QuoteLockTheme.quotelockColors.quoteCardSurface,
                )
                .onSizeChanged { size = it },
            contentAlignment = Alignment.Center
        ) {
            with(LocalDensity.current) {
                (size.width / 14F).toDp()
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = with(LocalDensity.current) {
                        (size.width * 0.08F).toDp()
                    })
            ) {
                val textSize = with(LocalDensity.current) {
                    (size.width / 14F).toSp()
                }
                SnapshotText(
                    text = text,
                    fontSize = textSize,
                    fontFamily = Typeface.SERIF,
                    textAlign = TextAlign.Start,
                    color = QuoteLockTheme.quotelockColors.quoteCardOnSurface,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(textSize.value.dp * 0.7F))
                SnapshotText(
                    text = source,
                    fontSize = textSize / 2F,
                    fontFamily = Typeface.SERIF,
                    textAlign = TextAlign.End,
                    color = QuoteLockTheme.quotelockColors.quoteCardOnSurface,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Preview
@Composable
fun WidgetPreviewPreview() {
    QuoteLockTheme {
        Box(modifier = Modifier.size(308.dp, 187.dp)) {
            WidgetPreview(
                text = "Knowledge is power.",
                source = "―Francis Bacon"
            )
        }
    }
}