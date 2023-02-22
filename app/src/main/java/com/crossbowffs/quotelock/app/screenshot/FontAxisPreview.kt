package com.crossbowffs.quotelock.app.screenshot

import android.content.res.Configuration
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crossbowffs.quotelock.app.font.FontManager
import com.crossbowffs.quotelock.app.quote.QuoteCard
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.data.api.QuoteDataWithCollectState
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import kotlin.math.abs
import kotlin.math.roundToInt


@Preview(
    name = "Animate Variable Font Axis Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
private fun AnimateVariableFontAxisPreview() {
    val quote = QuoteDataWithCollectState(
        quote = QuoteData(
            quoteText = "Knowledge is power.",
            quoteSource = "Francis Bacon",
            quoteAuthor = "",
        ),
        collectState = false
    )
    val currentState = remember { MutableTransitionState(false) }
    currentState.targetState = true
    val transition = updateTransition(currentState, label = "transition")
    val value by transition.animateFloat(label = "value",
        transitionSpec = {
            repeatable(
                iterations = 10,
                animation = tween(durationMillis = 10000, easing = LinearEasing)
            )
        }
    ) { state ->
        if (state) 1F else -1F
    }
    val typeface =
        FontManager.loadTypeface(
            "/system/fonts/custom/Inter-VariableFont_slnt,wght.ttf",
            FontWeight(100 + (800 * (1 - abs(value))).roundToInt()),
            FontStyle.Italic.value.toFloat(),
            -(10 * (1 - abs(value)))
        )
    QuoteLockTheme {
        Surface {
            QuoteCard(
                modifier = Modifier.padding(8.dp),
                quote = quote.quoteText,
                source = quote.readableSourceWithPrefix,
                quoteSize = 32.sp,
                sourceSize = 18.sp,
                lineSpacing = 16.dp,
                cardPadding = 16.dp,
                minHeight = 240.dp,
                quoteTypeface = typeface,
                sourceTypeface = typeface,
                quoteWeight = FontWeight.Normal,
                quoteStyle = FontStyle.Normal,
                sourceWeight = FontWeight.Normal,
                sourceStyle = FontStyle.Normal,
            )
        }
    }
}