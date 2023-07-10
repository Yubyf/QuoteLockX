package com.crossbowffs.quotelock.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Typeface
import android.text.Layout
import android.text.TextUtils
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import androidx.core.graphics.withTranslation
import androidx.datastore.preferences.core.MutablePreferences
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import com.crossbowffs.quotelock.app.App
import com.crossbowffs.quotelock.app.widget.QuoteGlanceWidget
import com.crossbowffs.quotelock.consts.PREF_SHARE_FILE_AUTHORITY
import com.crossbowffs.quotelock.consts.PREF_WIDGET_IMAGE_CHILD_PATH
import com.crossbowffs.quotelock.consts.PREF_WIDGET_IMAGE_EXTENSION
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.data.api.typeface
import com.crossbowffs.quotelock.data.modules.QuoteRepository
import com.crossbowffs.quotelock.di.IoDispatcher
import com.crossbowffs.quotelock.ui.components.makeNewTextLayout
import com.crossbowffs.quotelock.ui.theme.LightQuoteLockColors
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.dp2px
import com.crossbowffs.quotelock.utils.md5
import com.crossbowffs.quotelock.utils.toFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class WidgetRepository @Inject internal constructor(
    @ApplicationContext private val context: Context,
    private val quoteRepository: QuoteRepository,
    private val cardStyleRepository: CardStyleRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) {
    private val cacheFileDir =
        File(App.instance.externalCacheDir, PREF_WIDGET_IMAGE_CHILD_PATH)

    init {
        quoteRepository.quoteDataFlow.onEach {
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(QuoteGlanceWidget::class.java)
            glanceIds.forEach { glanceId ->
                updateGlanceState(glanceId)
            }
        }.launchIn(CoroutineScope(dispatcher))
    }

    fun placeholder() {
        /* no-op */
    }

    suspend fun updateGlanceState(glanceId: GlanceId) {
        // Clear the state to show loading screen
        updateAppWidgetState(context, glanceId, MutablePreferences::clear)
        QuoteGlanceWidget().update(context, glanceId)

        GlanceAppWidgetManager(context).getAppWidgetSizes(glanceId).forEach { size ->
            val quoteWithImageUri = getWidgetImage(
                size.width.value.dp2px().roundToInt(),
                size.height.value.dp2px().roundToInt()
            ).let { (quote, collected, file) ->
                Triple(
                    quote, collected, FileProvider.getUriForFile(
                        context.applicationContext, PREF_SHARE_FILE_AUTHORITY,
                        file
                    ).toString()
                )
            }
            updateAppWidgetState(context, glanceId) { prefs ->
                prefs[QuoteGlanceWidget.quoteContentKey] = quoteWithImageUri.first
                prefs[QuoteGlanceWidget.quoteCollectionStateKey] = quoteWithImageUri.second
                prefs[QuoteGlanceWidget.getImageKey(size)] = quoteWithImageUri.third
            }
        }
        QuoteGlanceWidget().update(context, glanceId)
        Xlog.d("QuoteGlanceWidget", "QuoteGlanceWidget update $glanceId")
    }

    private suspend fun getWidgetImage(width: Int, height: Int): Triple<String, Boolean, File> =
        withContext(dispatcher) {
            val currentQuote = quoteRepository.getCurrentQuote()
            val typeface = cardStyleRepository.quoteFontStyle.typeface
            val fontUid = cardStyleRepository.quoteFontStyle.family.md5().drop(8).dropLast(8)
            // Clear caches
            cacheFileDir.listFiles()?.forEach { file ->
                file.takeIf { !it.name.startsWith(currentQuote.uid) }?.delete()
            }
            return@withContext Triple(
                currentQuote.quote.byteString,
                currentQuote.collectState ?: false,
                File(
                    cacheFileDir,
                    "${currentQuote.uid}-$fontUid-${width}-${height}$PREF_WIDGET_IMAGE_EXTENSION"
                ).also {
                    if (!it.exists()) {
                        generateWidgetBitmap(
                            quote = currentQuote.quote,
                            typeface = typeface,
                            containerColor = LightQuoteLockColors.quoteCardSurface,
                            contentColor = LightQuoteLockColors.quoteCardOnSurface,
                            width = width,
                            height = height
                        ).toFile(it, dispatcher)
                    }
                }
            )
        }

    private fun generateWidgetBitmap(
        quote: QuoteData,
        typeface: Typeface?,
        containerColor: Color,
        contentColor: Color,
        width: Int,
        height: Int,
    ): Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { bitmap ->
        val textSize = width / 14F
        val sourceSize = textSize * 0.5F
        val contentInternalPadding = textSize * 0.7F
        val horizontalPadding = (width * 0.08F).roundToInt()

        val canvas = Canvas(bitmap)
        canvas.drawColor(containerColor.toArgb())
        val textLayout = makeNewTextLayout(
            text = quote.quoteText,
            width = width - horizontalPadding * 2,
            fontSize = textSize,
            textColor = contentColor.toArgb(),
            fontFamily = typeface,
            lineHeightMult = 1.2F,
            ellipsize = TextUtils.TruncateAt.END,
            ellipsizeWidth = width - horizontalPadding * 2,
            maxLines = 3,
        )
        val sourceLayout = quote.readableSourceWithPrefix.takeIf { it.isNotBlank() }?.let {
            makeNewTextLayout(
                text = quote.readableSourceWithPrefix,
                width = width - horizontalPadding * 2,
                fontSize = sourceSize,
                textColor = contentColor.toArgb(),
                alignment = Layout.Alignment.ALIGN_OPPOSITE,
                fontFamily = typeface,
                ellipsize = TextUtils.TruncateAt.END,
                ellipsizeWidth = width - horizontalPadding * 2,
                maxLines = 2,
            )
        }
        val textHeight = textLayout.height
        sourceLayout?.let { it.height + contentInternalPadding } ?: 0
        val contentHeight =
            textHeight + (sourceLayout?.let { it.height + contentInternalPadding } ?: 0F)
        canvas.withTranslation(
            x = horizontalPadding.toFloat(),
            y = (height - contentHeight) / 2F
        ) {
            textLayout.draw(canvas)
            sourceLayout?.let {
                canvas.withTranslation(
                    x = 0F,
                    y = textHeight + contentInternalPadding
                ) {
                    it.draw(canvas)
                }
            }
        }
    }
}