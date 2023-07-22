package com.crossbowffs.quotelock.data

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Environment
import android.text.TextPaint
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.crossbowffs.quotelock.app.App
import com.crossbowffs.quotelock.consts.PREF_PUBLIC_RELATIVE_PATH
import com.crossbowffs.quotelock.consts.PREF_QUOTE_CARD_ELEVATION_DP
import com.crossbowffs.quotelock.consts.PREF_SHARE_IMAGE_CHILD_PATH
import com.crossbowffs.quotelock.consts.PREF_SHARE_IMAGE_EXTENSION
import com.crossbowffs.quotelock.consts.PREF_SHARE_IMAGE_FRAME_WIDTH
import com.crossbowffs.quotelock.consts.PREF_SHARE_IMAGE_NAME_PREFIX
import com.crossbowffs.quotelock.consts.PREF_SHARE_IMAGE_WATERMARK_PADDING
import com.crossbowffs.quotelock.consts.PREF_SHARE_IMAGE_WATERMARK_TEXT_SIZE_PX
import com.crossbowffs.quotelock.di.DISPATCHER_IO
import com.crossbowffs.quotelock.ui.components.Snapshotables
import com.crossbowffs.quotelock.utils.dp2px
import com.crossbowffs.quotelock.utils.toFile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt

@Single
class ShareRepository(
    @Named(DISPATCHER_IO) private val dispatcher: CoroutineDispatcher,
) {
    private val legacyShareFileDir =
        File(App.instance.getExternalFilesDir(null), PREF_SHARE_IMAGE_CHILD_PATH)
    private val shareFileDir =
        File(App.instance.externalCacheDir, PREF_SHARE_IMAGE_CHILD_PATH)
    private val savePublicDir =
        File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            PREF_PUBLIC_RELATIVE_PATH
        )

    private val _cacheSizeFlow = MutableStateFlow(-1L)
    val cacheSizeFlow = _cacheSizeFlow.asStateFlow()

    var currentSnapshotables: Snapshotables? = Snapshotables()

    suspend fun saveBitmapPublic(bitmap: Bitmap): File = File(
        savePublicDir,
        PREF_SHARE_IMAGE_NAME_PREFIX + DATE_FORMATTER.format(Date(System.currentTimeMillis()))
                + PREF_SHARE_IMAGE_EXTENSION
    ).also {
        bitmap.toFile(it, dispatcher)
    }

    suspend fun saveBitmapInternal(bitmap: Bitmap): File = File(
        shareFileDir,
        UUID.randomUUID().toString() + PREF_SHARE_IMAGE_EXTENSION
    ).also {
        bitmap.toFile(it, dispatcher)
    }

    suspend fun clearCache() = withContext(dispatcher) {
        legacyShareFileDir.takeIf { it.exists() }?.apply(File::deleteRecursively)
        shareFileDir.deleteRecursively()
        shareFileDir.mkdirs()
        _cacheSizeFlow.value = 0
    }

    suspend fun calcCacheSizeBytes() = withContext(dispatcher) {
        _cacheSizeFlow.value = sequenceOf(shareFileDir, legacyShareFileDir).sumOf { dir ->
            dir.listFiles()?.sumOf { it.length() } ?: 0
        }
    }

    suspend fun generateShareBitmap(
        containerColor: Color,
        contentColor: Color,
        watermark: Pair<String, Drawable?>? = null,
    ): Bitmap? =
        withContext(dispatcher) {
            currentSnapshotables?.let { snapshotables ->
                snapshotables.shareBounds?.let { size ->
                    Bitmap.createBitmap(
                        size.width.roundToInt(),
                        (size.height
                                + if (watermark != null) PREF_SHARE_IMAGE_WATERMARK_PADDING else 0F).roundToInt(),
                        Bitmap.Config.ARGB_8888
                    )
                        .also { bitmap ->
                            val canvas = Canvas(bitmap)
                            snapshotables.drawShareCard(
                                canvas,
                                containerColor,
                                contentColor,
                                true,
                                watermark
                            )
                        }
                }
            }
        }

    companion object {
        private val DATE_FORMATTER = SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
    }
}

val Snapshotables.shareBounds: Size?
    get() = bounds?.let { size ->
        Size(
            size.width + PREF_SHARE_IMAGE_FRAME_WIDTH * 2,
            size.height + PREF_SHARE_IMAGE_FRAME_WIDTH * 2
        )
    }

fun Canvas.drawWatermark(icon: Drawable?, watermark: String) {
    save()
    val watermarkAlpha = 0.3F
    val watermarkPaint = TextPaint().apply {
        color = Color.Black.copy(alpha = watermarkAlpha).toArgb()
        textSize = PREF_SHARE_IMAGE_WATERMARK_TEXT_SIZE_PX
    }
    val watermarkIconSize = PREF_SHARE_IMAGE_WATERMARK_TEXT_SIZE_PX * 0.9F
    icon?.apply {
        setBounds(
            0,
            -(watermarkIconSize / 2).roundToInt(),
            watermarkIconSize.roundToInt(),
            (watermarkIconSize / 2).roundToInt()
        )
        setTint(Color.Black.toArgb())
        alpha = (255 * watermarkAlpha).roundToInt()
        translate(watermarkIconSize / 2F, 0F)
        draw(this@drawWatermark)
        translate(watermarkIconSize * 1.5F, 0F)
    }
    val textBounds = Rect()
    watermarkPaint.getTextBounds(watermark, 0, watermark.length, textBounds)
    // Draw watermark text vertically centered.
    translate(0F, -textBounds.exactCenterY())
    drawText(watermark, 0F, 0F, watermarkPaint)
    restore()
}

fun Snapshotables.drawShareCard(
    canvas: Canvas,
    containerColor: Color,
    contentColor: Color,
    drawBackground: Boolean,
    watermark: Pair<String, Drawable?>? = null,
) {
    shareBounds?.let {
        if (drawBackground) {
            canvas.drawColor(Color.White.toArgb())
        }
        canvas.save()
        canvas.translate(
            PREF_SHARE_IMAGE_FRAME_WIDTH,
            PREF_SHARE_IMAGE_FRAME_WIDTH
        )
        snapshot(
            canvas,
            containerColor,
            contentColor
        )
        canvas.restore()
        watermark?.let { (text, icon) ->
            // Watermark
            canvas.translate(
                PREF_QUOTE_CARD_ELEVATION_DP.dp2px()
                        + PREF_SHARE_IMAGE_FRAME_WIDTH,
                it.height + PREF_SHARE_IMAGE_WATERMARK_PADDING / 4
            )
            canvas.drawWatermark(icon, text)
        }
    }
}
