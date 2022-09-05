package com.crossbowffs.quotelock.ui.components

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.*
import android.os.Build
import android.text.*
import androidx.annotation.ColorInt
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.compose.ui.unit.LayoutDirection.Ltr
import androidx.compose.ui.unit.LayoutDirection.Rtl
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import java.util.*
import kotlin.math.roundToInt

class Snapshotables : MutableIterable<Snapshotable> {
    private val list = mutableListOf<Snapshotable>()

    override fun iterator(): MutableIterator<Snapshotable> = list.iterator()

    operator fun plusAssign(element: Snapshotable) {
        val overrideIndex = list.indexOfFirst { it.snapshotKey == element.snapshotKey }
        if (overrideIndex >= 0) list[overrideIndex] = element else list += element
    }

    val bounds: Size?
        get() = list.filter { it.snapshotRoot }.map { it.bounds }.maxByOrNull { it.maxDimension }
}

@Composable
fun rememberSnapshotState(
    snapshotKey: String = UUID.randomUUID().toString(),
    snapshotRoot: Boolean = true,
) = remember {
    Snapshotable(snapshotKey = snapshotKey, snapshotRoot = snapshotRoot)
}

class Snapshotable internal constructor(
    internal val snapshotKey: String = UUID.randomUUID().toString(),
    internal val snapshotRoot: Boolean = true,
    internal var snapshotCallback: ((Canvas) -> Unit)? = null,
    internal var boundsCallback: (() -> Size)? = null,
) {
    val bounds: Size
        get() = boundsCallback?.invoke() ?: Size.Unspecified

    fun snapshot(canvas: Canvas) {
        snapshotCallback?.invoke(canvas)
    }
}

@Composable
fun SnapshotText(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = 16.sp,
    fontStyle: FontStyle = FontStyle.Normal,
    fontWeight: FontWeight = FontWeight.Normal,
    fontFamily: Typeface? = null,
    lineHeight: TextUnit = 1.em,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    snapshotable: Snapshotable? = null,
) {
    var textLayout: Layout? by remember {
        mutableStateOf(null)
    }
    var textPicture: Picture? by remember {
        mutableStateOf(null)
    }
    var textRegion by remember {
        mutableStateOf(Rect.Zero)
    }

    DisposableEffect(Unit) {
        snapshotable?.let { snapshot ->
            snapshot.snapshotCallback = { canvas ->
                textPicture?.let { picture ->
                    canvas.save()
                    if (!snapshot.snapshotRoot) {
                        canvas.translate(textRegion.left, textRegion.top)
                    }
                    picture.draw(canvas)
                    canvas.restore()
                }
            }
            snapshot.boundsCallback = { Size(textRegion.width, textRegion.height) }
        }

        onDispose {
            snapshotable?.snapshotCallback = null
            snapshotable?.boundsCallback = null
        }
    }

    val layoutDirection = LocalLayoutDirection.current
    val textColor: Color = if (color.isUnspecified) LocalContentColor.current else color
    androidx.compose.ui.layout.Layout(modifier = modifier
        .onGloballyPositioned {
            textRegion = it.boundsInParent()
        },
        content = {
            Canvas(modifier = Modifier) {
                drawIntoCanvas { canvas ->
                    textLayout?.let { layout ->
                        textPicture = Picture().apply {
                            layout.draw(beginRecording(layout.width, layout.height))
                            endRecording()
                            draw(canvas.nativeCanvas)
                        }
                    }
                }
            }
        }
    ) { measurables, constraints ->
        require(measurables.size == 1)
        val placeable = measurables.first().measure(constraints)
        val layout = buildTextLayout(
            text = text,
            width = constraints.maxWidth,
            textColor = textColor.toArgb(),
            fontSize = fontSize.toPx(),
            fontStyle = when {
                fontStyle == FontStyle.Normal && fontWeight <= FontWeight.Normal -> Typeface.NORMAL
                fontStyle == FontStyle.Normal && fontWeight > FontWeight.Normal -> Typeface.BOLD
                fontStyle == FontStyle.Italic && fontWeight <= FontWeight.Normal -> Typeface.ITALIC
                fontStyle == FontStyle.Italic && fontWeight > FontWeight.Normal -> Typeface.BOLD_ITALIC
                else -> Typeface.NORMAL
            },
            fontFamily = fontFamily,
            alignment = when (textAlign) {
                TextAlign.Left -> if (layoutDirection == Ltr) {
                    Layout.Alignment.ALIGN_NORMAL
                } else Layout.Alignment.ALIGN_OPPOSITE
                TextAlign.Start -> Layout.Alignment.ALIGN_NORMAL
                TextAlign.Center -> Layout.Alignment.ALIGN_CENTER
                TextAlign.End -> Layout.Alignment.ALIGN_OPPOSITE
                TextAlign.Right -> if (layoutDirection == Ltr) {
                    Layout.Alignment.ALIGN_OPPOSITE
                } else Layout.Alignment.ALIGN_NORMAL
                else -> Layout.Alignment.ALIGN_NORMAL
            },
            direction = when (layoutDirection) {
                Ltr -> TextDirectionHeuristics.FIRSTSTRONG_LTR
                Rtl -> TextDirectionHeuristics.FIRSTSTRONG_RTL
            },
            lineHeightMult = lineHeight.value,
            ellipsize = when (overflow) {
                TextOverflow.Ellipsis -> TextUtils.TruncateAt.END
                TextOverflow.Clip, TextOverflow.Visible -> null
                else -> null
            },
            ellipsizeWidth = constraints.maxWidth,
            maxLines = maxLines,
            includePad = false,
        ).also {
            textLayout = it
        }
        layout(
            if (constraints.hasFixedWidth) {
                constraints.maxWidth
            } else {
                (0 until layout.lineCount).map(layout::getLineWidth).max().roundToInt()
            },
            if (constraints.hasFixedHeight) {
                constraints.maxHeight
            } else {
                layout.height
            }
        ) {
            placeable.placeRelative(0, 0)
        }
    }
}

@SuppressLint("WrongConstant")
private fun buildTextLayout(
    text: String,
    width: Int,
    @ColorInt textColor: Int,
    fontSize: Float,
    fontStyle: Int = Typeface.NORMAL,
    fontFamily: Typeface? = null,
    alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
    direction: TextDirectionHeuristic = TextDirectionHeuristics.FIRSTSTRONG_LTR,
    lineHeightMult: Float = 1F,
    ellipsize: TextUtils.TruncateAt? = null,
    ellipsizeWidth: Int = 0,
    maxLines: Int = Int.MAX_VALUE,
    includePad: Boolean = true,
): Layout {
    val paint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG).apply {
        textSize = fontSize
        color = textColor
        // Set font typeface and style
        // Copied from [TextView#setTypeface(Typeface, int)]
        if (fontStyle > 0) {
            typeface = if (fontFamily == null) {
                Typeface.defaultFromStyle(fontStyle)
            } else {
                Typeface.create(fontFamily, fontStyle)
            }
            // now compute what (if any) algorithmic styling is needed
            val typefaceStyle = typeface?.style ?: 0
            val need: Int = fontStyle and typefaceStyle.inv()
            isFakeBoldText = need and Typeface.BOLD != 0
            textSkewX = if (need and Typeface.ITALIC != 0) -0.25f else 0F
        } else {
            isFakeBoldText = false
            textSkewX = 0f
            typeface = fontFamily
        }
    }
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        StaticLayout.Builder.obtain(text, 0, text.length,
            paint, width)
            .setAlignment(alignment)
            .setTextDirection(direction)
            .setLineSpacing(0F, lineHeightMult)
            .setIncludePad(includePad)
            .setBreakStrategy(Layout.BREAK_STRATEGY_SIMPLE)
            .setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NONE)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    setJustificationMode(Layout.JUSTIFICATION_MODE_NONE)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    setUseLineSpacingFromFallbacks(false)
                }
            }
            .setEllipsize(ellipsize)
            .setEllipsizedWidth(ellipsizeWidth)
            .setMaxLines(maxLines)
            .build()
    } else {
        @Suppress("DEPRECATION")
        StaticLayout(text, 0, text.length,
            paint, width,
            alignment,
            lineHeightMult, 0F,
            includePad,
            ellipsize, ellipsizeWidth)
    }
}

@Composable
fun SnapshotCard(
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    elevation: Dp = 0.dp,
    cornerSize: Dp = 0.dp,
    contentAlignment: Alignment = Alignment.TopStart,
    snapshotable: Snapshotable? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    var backgroundPicture: Picture? by remember {
        mutableStateOf(null)
    }
    var cardRegion by remember {
        mutableStateOf(Rect.Zero)
    }
    val elevationPx = with(LocalDensity.current) { elevation.toPx() }
    DisposableEffect(Unit) {
        snapshotable?.let { snapshot ->
            snapshot.snapshotCallback = { canvas ->
                backgroundPicture?.let { picture ->
                    canvas.translate(elevationPx, elevationPx)
                    canvas.save()
                    if (!snapshot.snapshotRoot) {
                        canvas.translate(cardRegion.left, cardRegion.top)
                    }
                    picture.draw(canvas)
                    canvas.restore()
                }
            }
            snapshot.boundsCallback = {
                Size(cardRegion.width + elevationPx * 2,
                    cardRegion.height + elevationPx * 2)
            }
        }

        onDispose {
            snapshotable?.snapshotCallback = null
            snapshotable?.boundsCallback = null
        }
    }
    CompositionLocalProvider(
        LocalContentColor provides contentColor,
    ) {
        Box(
            modifier = modifier
                .padding(elevation)
                .drawWithContent {
                    drawIntoCanvas { canvas ->
                        backgroundPicture = Picture().apply {
                            val pictureCanvas = beginRecording(size.width.roundToInt(),
                                size.height.roundToInt())
                            val rect = RectF(0F, 0F, size.width, size.height)
                            val shadowPaint = Paint().apply {
                                color = Color.Black
                                    .copy(alpha = 0.2F)
                                    .toArgb()
                                style = Paint.Style.FILL
                                maskFilter = BlurMaskFilter(elevation.toPx() * 2,
                                    BlurMaskFilter.Blur.OUTER)
                            }
                            val backgroundPaint = Paint().apply {
                                color = containerColor.toArgb()
                                style = Paint.Style.FILL
                            }
                            pictureCanvas.drawRoundRect(rect,
                                cornerSize.toPx(),
                                cornerSize.toPx(),
                                shadowPaint)
                            pictureCanvas.drawRoundRect(rect,
                                cornerSize.toPx(),
                                cornerSize.toPx(),
                                backgroundPaint
                            )
                            endRecording()
                            draw(canvas.nativeCanvas)
                        }
                    }
                    drawContent()
                }
                .onGloballyPositioned { cardRegion = it.boundsInParent() },
            contentAlignment = contentAlignment,
            content = content
        )
    }
}

@Preview(name = "Snapshot Text Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Snapshot Text Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SnapshotTextPreview() {
    QuoteLockTheme {
        Surface {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = QuoteLockTheme.quotelockColors.quoteCardSurface,
                    contentColor = QuoteLockTheme.quotelockColors.quoteCardOnSurface,
                ),
                shape = RoundedCornerShape(2.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    horizontalAlignment = Alignment.End,
                ) {
                    SnapshotText(
                        modifier = Modifier.fillMaxWidth(),
                        text = "落霞与孤鹜齐飞，秋水共长天一色",
                        fontSize = 36.sp,
                        lineHeight = 1.3F.em,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                    SnapshotText(
                        modifier = Modifier.wrapContentWidth(),
                        text = "王勃",
                        fontSize = 16.sp,
                        lineHeight = 1.3F.em,
                    )
                }
            }
        }
    }
}