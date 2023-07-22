package com.crossbowffs.quotelock.ui.components

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
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
        get() = list.filter { it.snapshotRoot }.map { Size(it.bounds.width, it.bounds.height) }
            .maxByOrNull { it.maxDimension }

    fun snapshot(
        canvas: Canvas,
        containerColor: Color = Color.Unspecified,
        contentColor: Color = Color.Unspecified,
    ) {
        list.forEach { it.snapshot(canvas, containerColor, contentColor) }
    }
}

@Composable
fun rememberContainerSnapshotState(
    snapshotKey: String = UUID.randomUUID().toString(),
    snapshotRoot: Boolean = true,
) = remember {
    ContainerSnapshotable(snapshotKey = snapshotKey, snapshotRoot = snapshotRoot)
}

@Composable
fun rememberTextSnapshotState(
    snapshotKey: String = UUID.randomUUID().toString(),
    snapshotRoot: Boolean = true,
) = remember {
    TextSnapshotable(snapshotKey = snapshotKey, snapshotRoot = snapshotRoot)
}

@Composable
fun rememberCardSnapshotState(
    snapshotKey: String = UUID.randomUUID().toString(),
    snapshotRoot: Boolean = true,
) = remember {
    CardSnapshotable(snapshotKey = snapshotKey, snapshotRoot = snapshotRoot)
}

abstract class Snapshotable internal constructor(
    internal val snapshotKey: String = UUID.randomUUID().toString(),
    internal val snapshotRoot: Boolean = true,
) {
    abstract val bounds: Rect

    abstract fun snapshot(
        canvas: Canvas,
        containerColor: Color = Color.Unspecified,
        contentColor: Color = Color.Unspecified,
    )
}

class ContainerSnapshotable(
    snapshotKey: String = UUID.randomUUID().toString(),
    snapshotRoot: Boolean = true,
    internal var region: Rect = Rect.Zero,
) : Snapshotable(snapshotKey, snapshotRoot) {
    override val bounds: Rect
        get() = region

    override fun snapshot(canvas: Canvas, containerColor: Color, contentColor: Color) {
        canvas.translate(bounds.left, bounds.top)
    }
}

class TextSnapshotable(
    snapshotKey: String = UUID.randomUUID().toString(),
    snapshotRoot: Boolean = true,
    internal var textLayout: Layout? = null,
    internal var textRegion: Rect = Rect.Zero,
) : Snapshotable(snapshotKey, snapshotRoot) {

    override val bounds: Rect
        get() = textRegion

    override fun snapshot(
        canvas: Canvas,
        containerColor: Color,
        contentColor: Color,
    ) {
        textLayout?.let { layout ->
            canvas.save()
            layout.paint.color = contentColor.toArgb()
            if (!snapshotRoot) {
                canvas.translate(textRegion.left, textRegion.top)
            }
            layout.draw(canvas)
            canvas.restore()
        }
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
    snapshotable: TextSnapshotable? = null,
) {
    var textLayout: SnapshotTextLayout? by remember {
        mutableStateOf(null)
    }
    var textRegion by remember {
        mutableStateOf(Rect.Zero)
    }
    LaunchedEffect(textLayout, textRegion) {
        snapshotable?.apply {
            this.textLayout = textLayout?.makeNewTextLayout()
            this.textRegion = textRegion
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
                    textLayout?.layout?.draw(canvas.nativeCanvas)
                }
            }
        }
    ) { measurables, constraints ->
        require(measurables.size == 1)
        val placeable = measurables.first().measure(constraints)
        val newTextLayout = SnapshotTextLayout(
            text = text,
            width = constraints.maxWidth,
            fontSize = fontSize.toPx(),
            textColor = textColor,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            lineHeight = lineHeight,
            layoutDirection = layoutDirection,
            textAlign = textAlign ?: TextAlign.Start,
            overflow = overflow,
            maxLines = maxLines,
        )
        val layout: Layout = textLayout?.let {
            if (newTextLayout == it) {
                it.layout
            } else {
                newTextLayout.updateTextLayout().also {
                    textLayout = newTextLayout
                }
            }
        } ?: newTextLayout.updateTextLayout().also {
            textLayout = newTextLayout
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

private data class SnapshotTextLayout(
    private val text: String,
    private val width: Int,
    private val fontSize: Float,
    private val textColor: Color = Color.Unspecified,
    private val fontStyle: FontStyle = FontStyle.Normal,
    private val fontWeight: FontWeight = FontWeight.Normal,
    private val fontFamily: Typeface? = null,
    private val lineHeight: TextUnit = 1.em,
    private val layoutDirection: LayoutDirection = Ltr,
    private val textAlign: TextAlign? = null,
    private val overflow: TextOverflow = TextOverflow.Clip,
    private val maxLines: Int = Int.MAX_VALUE,
) {

    lateinit var layout: Layout
        private set

    fun updateTextLayout(): Layout = makeNewTextLayout().also {
        layout = it
    }

    fun makeNewTextLayout(): Layout = makeNewTextLayout(
        text = text,
        width = width,
        textColor = textColor.toArgb(),
        fontSize = fontSize,
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
        ellipsizeWidth = width,
        maxLines = maxLines,
        includePad = false,
    )

    override fun hashCode(): Int {
        var result = text.hashCode()
        result = 31 * result + width
        result = 31 * result + fontSize.hashCode()
        result = 31 * result + textColor.hashCode()
        result = 31 * result + fontStyle.hashCode()
        result = 31 * result + fontWeight.hashCode()
        result = 31 * result + (fontFamily?.hashCode() ?: 0)
        result = 31 * result + lineHeight.hashCode()
        result = 31 * result + layoutDirection.hashCode()
        result = 31 * result + (textAlign?.hashCode() ?: 0)
        result = 31 * result + overflow.hashCode()
        result = 31 * result + maxLines
        result = 31 * result + layout.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        return other is SnapshotTextLayout &&
                text == other.text &&
                width == other.width &&
                fontSize == other.fontSize &&
                textColor == other.textColor &&
                fontStyle == other.fontStyle &&
                fontWeight == other.fontWeight &&
                fontFamily == other.fontFamily &&
                lineHeight == other.lineHeight &&
                layoutDirection == other.layoutDirection &&
                textAlign == other.textAlign &&
                overflow == other.overflow &&
                maxLines == other.maxLines
    }
}

@SuppressLint("WrongConstant")
fun makeNewTextLayout(
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
        StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
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
        StaticLayout(
            text, 0, text.length,
            paint, width,
            alignment,
            lineHeightMult, 0F,
            includePad,
            ellipsize, ellipsizeWidth
        )
    }
}

private const val CARD_SHADOW_ALPHA = 0.1F

class CardSnapshotable(
    snapshotKey: String = UUID.randomUUID().toString(),
    snapshotRoot: Boolean = true,
    internal var cornerSizePx: Float = 0F,
    internal var cardRegion: Rect = Rect.Zero,
    internal var insideRect: Rect = Rect.Zero,
) : Snapshotable(snapshotKey, snapshotRoot) {

    private val backgroundPaint = Paint().apply {
        style = Paint.Style.FILL
    }

    private val shadowPaint = Paint().apply {
        color = Color.Black
            .copy(alpha = CARD_SHADOW_ALPHA)
            .toArgb()
        style = Paint.Style.FILL
    }

    internal var elevationPx: Float = 0F
        set(value) {
            field = value
            shadowPaint.maskFilter = BlurMaskFilter(
                value * 2,
                BlurMaskFilter.Blur.OUTER
            )
        }

    override val bounds: Rect
        get() = cardRegion.copy(
            left = cardRegion.left - elevationPx,
            top = cardRegion.top - elevationPx,
            right = cardRegion.right + elevationPx,
            bottom = cardRegion.bottom + elevationPx
        )

    override fun snapshot(
        canvas: Canvas,
        containerColor: Color,
        contentColor: Color,
    ) {
        backgroundPaint.color = containerColor.toArgb()
        canvas.translate(elevationPx, elevationPx)
        canvas.save()
        if (!snapshotRoot) {
            canvas.translate(cardRegion.left, cardRegion.top)
        }
        canvas.drawRoundRect(insideRect.toAndroidRectF(), cornerSizePx, cornerSizePx, shadowPaint)
        canvas.drawRoundRect(
            insideRect.toAndroidRectF(),
            cornerSizePx,
            cornerSizePx,
            backgroundPaint
        )
        canvas.restore()
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
    snapshotable: CardSnapshotable? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    var cardRegion by remember {
        mutableStateOf(Rect.Zero)
    }
    val elevationPx = with(LocalDensity.current) { elevation.toPx() }
    val cornerSizePx = with(LocalDensity.current) { cornerSize.toPx() }
    var insideRect by remember {
        mutableStateOf(Rect.Zero)
    }
    val shadowPaint by remember {
        mutableStateOf(Paint().apply {
            color = Color.Black
                .copy(alpha = CARD_SHADOW_ALPHA)
                .toArgb()
            style = Paint.Style.FILL
            maskFilter = BlurMaskFilter(
                elevationPx * 2,
                BlurMaskFilter.Blur.OUTER
            )
        })
    }
    val backgroundPaint by remember(containerColor) {
        mutableStateOf(Paint().apply {
            color = containerColor.toArgb()
            style = Paint.Style.FILL
        })
    }
    LaunchedEffect(elevationPx, cornerSizePx, cardRegion, insideRect) {
        snapshotable?.apply {
            this.elevationPx = elevationPx
            this.cornerSizePx = cornerSizePx
            this.cardRegion = cardRegion
            this.insideRect = insideRect
        }
    }
    CompositionLocalProvider(
        LocalContentColor provides contentColor,
    ) {
        Box(
            modifier = modifier
                .padding(elevation * 1.6F)
                .drawWithContent {
                    drawIntoCanvas { canvas ->
                        insideRect = Rect(0F, 0F, size.width, size.height)
                        canvas.nativeCanvas.drawRoundRect(
                            insideRect.toAndroidRectF(),
                            cornerSizePx,
                            cornerSizePx,
                            shadowPaint
                        )
                        canvas.nativeCanvas.drawRoundRect(
                            insideRect.toAndroidRectF(),
                            cornerSizePx,
                            cornerSizePx,
                            backgroundPaint
                        )
                    }
                    drawContent()
                }
                .onGloballyPositioned { cardRegion = it.boundsInParent() },
            contentAlignment = contentAlignment,
            content = content
        )
    }
}

@Preview(
    name = "Snapshot Text Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Snapshot Text Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
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