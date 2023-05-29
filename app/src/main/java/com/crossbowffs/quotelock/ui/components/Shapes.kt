package com.crossbowffs.quotelock.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme

class BubbleShape(
    private val cornerSize: Dp,
    private val arrowSize: Dp,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val cornerRadius = CornerRadius(with(density) { cornerSize.toPx() }
            .coerceAtMost(size.minDimension / 2))
        val arrowSize = with(density) { arrowSize.toPx().coerceAtMost(8.dp.toPx()) }
        val path = Path().apply {
            addRoundRect(RoundRect(0F,
                0F,
                size.width - arrowSize,
                size.height,
                cornerRadius,
                cornerRadius,
                cornerRadius,
                cornerRadius)
            )
            moveTo(size.width - arrowSize, size.height / 2 - arrowSize)
            lineTo(size.width - arrowSize * 0.2F, size.height / 2 - arrowSize * 0.2F)
            quadraticBezierTo(size.width, size.height / 2,
                size.width - arrowSize * 0.2F, size.height / 2 + arrowSize * 0.2F)
            lineTo(size.width - arrowSize, size.height / 2 + arrowSize)
            close()
        }
        return Outline.Generic(path)
    }
}

@Preview
@Composable
fun BubbleShapePreview() {
    QuoteLockTheme {
        Surface(shape = BubbleShape(4.dp, 6.dp), modifier = Modifier
            .width(200.dp)
            .height(30.dp)) {
        }
    }
}