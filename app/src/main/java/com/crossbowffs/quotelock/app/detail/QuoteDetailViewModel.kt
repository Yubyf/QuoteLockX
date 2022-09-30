package com.crossbowffs.quotelock.app.detail

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.text.TextPaint
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crossbowffs.quotelock.consts.PREF_QUOTE_CARD_ELEVATION_DP
import com.crossbowffs.quotelock.consts.PREF_SHARE_IMAGE_FRAME_WIDTH
import com.crossbowffs.quotelock.consts.PREF_SHARE_IMAGE_WATERMARK_TEXT_SIZE_PX
import com.crossbowffs.quotelock.data.CardStyleRepository
import com.crossbowffs.quotelock.data.ShareRepository
import com.crossbowffs.quotelock.data.api.*
import com.crossbowffs.quotelock.data.modules.collections.QuoteCollectionRepository
import com.crossbowffs.quotelock.di.IoDispatcher
import com.crossbowffs.quotelock.di.ResourceProvider
import com.crossbowffs.quotelock.utils.dp2px
import com.yubyf.quotelockx.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * UI event for the quote detail screen.
 */
data class QuoteDetailUiEvent(val shareFile: File?)

/**
 * UI state for the quote detail screen.
 */
data class QuoteDetailUiState(
    val cardStyle: CardStyle,
    val collectState: Boolean? = null,
)

/**
 * @author Yubyf
 */
@HiltViewModel
class QuoteDetailViewModel @Inject constructor(
    private val cardStyleRepository: CardStyleRepository,
    private val collectionRepository: QuoteCollectionRepository,
    private val shareRepository: ShareRepository,
    private val resourceProvider: ResourceProvider,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : ViewModel() {

    var quoteData: QuoteData = QuoteData()
        set(value) {
            if (field == value) {
                return
            }
            field = value
            _uiState.value = _uiState.value.copy(collectState = null)
        }

    private val _uiEvent = MutableSharedFlow<QuoteDetailUiEvent?>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _uiState =
        mutableStateOf(QuoteDetailUiState(CardStyle()))
    val uiState: State<QuoteDetailUiState> = _uiState

    init {
        cardStyleRepository.cardStyleFlow.onEach { cardStyle ->
            _uiState.value = _uiState.value.copy(cardStyle = cardStyle)
        }.launchIn(viewModelScope)
        collectionRepository.getAllStream().onEach { collections ->
            val quoteData = quoteData.copy()
            val currentQuoteCollected = collections.find { quoteData.md5 == it.md5 } != null
            _uiState.value = _uiState.value.copy(collectState = currentQuoteCollected)
        }.launchIn(viewModelScope)
    }

    fun queryQuoteCollectState() {
        viewModelScope.launch {
            val state = collectionRepository.getByQuote(quoteData.quoteText,
                quoteData.quoteSource,
                quoteData.quoteAuthor) != null
            _uiState.value = _uiState.value.copy(collectState = state)
        }
    }

    fun switchCollectionState(quoteData: QuoteDataWithCollectState) {
        viewModelScope.launch {
            val currentState = collectionRepository.getByQuote(quoteData.quoteText,
                quoteData.quoteSource,
                quoteData.quoteAuthor) != null
            if (currentState) {
                collectionRepository.delete(quoteData.md5)
            } else {
                collectionRepository.insert(quoteData.toQuoteData())
            }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    fun shareQuote(size: Size, block: ((Canvas) -> Unit)) {
        val shareWatermark = cardStyleRepository.shareWatermark
        viewModelScope.launch {
            val bitmap = withContext(dispatcher) {
                // Added padding around image
                val imageSize = Size(size.width + PREF_SHARE_IMAGE_FRAME_WIDTH * 2,
                    size.height + PREF_SHARE_IMAGE_FRAME_WIDTH * 2)
                val watermarkPadding = PREF_SHARE_IMAGE_WATERMARK_TEXT_SIZE_PX * 2F
                Bitmap.createBitmap(imageSize.width.roundToInt(),
                    (imageSize.height + if (shareWatermark) watermarkPadding else 0F).roundToInt(),
                    Bitmap.Config.ARGB_8888)
                    .also { bitmap ->
                        val canvas = Canvas(bitmap)
                        canvas.drawColor(Color.White.toArgb())
                        canvas.save()
                        canvas.translate(PREF_SHARE_IMAGE_FRAME_WIDTH, PREF_SHARE_IMAGE_FRAME_WIDTH)
                        block.invoke(canvas)
                        canvas.restore()
                        if (!shareWatermark) {
                            return@also
                        }

                        // Watermark
                        val watermarkAlpha = 0.3F
                        canvas.translate(PREF_QUOTE_CARD_ELEVATION_DP.dp2px()
                                + PREF_SHARE_IMAGE_FRAME_WIDTH,
                            imageSize.height + watermarkPadding / 4)
                        val watermarkPaint = TextPaint().apply {
                            color = Color.Black.copy(alpha = watermarkAlpha).toArgb()
                            textSize = PREF_SHARE_IMAGE_WATERMARK_TEXT_SIZE_PX
                        }
                        val watermarkIconSize = PREF_SHARE_IMAGE_WATERMARK_TEXT_SIZE_PX * 0.9F
                        resourceProvider.getDrawable(R.drawable.ic_quotelockx)?.apply {
                            setBounds(0,
                                -(watermarkIconSize / 2).roundToInt(),
                                watermarkIconSize.roundToInt(),
                                (watermarkIconSize / 2).roundToInt())
                            setTint(Color.Black.toArgb())
                            alpha = (255 * watermarkAlpha).roundToInt()
                            canvas.translate(watermarkIconSize / 2F, 0F)
                            draw(canvas)
                            canvas.translate(watermarkIconSize * 1.5F, 0F)
                        }
                        val watermark = resourceProvider.getString(R.string.quotelockx)
                        val textBounds = Rect()
                        watermarkPaint.getTextBounds(watermark, 0, watermark.length, textBounds)
                        // Draw watermark text vertically centered.
                        canvas.translate(0F, -textBounds.exactCenterY())
                        canvas.drawText(resourceProvider.getString(R.string.quotelockx),
                            0F, 0F, watermarkPaint)
                    }
            }
            val sharedFile = shareRepository.saveBitmap(bitmap)
            if (sharedFile.exists()) {
                _uiEvent.emit(QuoteDetailUiEvent(sharedFile))
            }
        }
    }

    fun quoteShared() {
        viewModelScope.launch {
            _uiEvent.emit(null)
        }
    }
}