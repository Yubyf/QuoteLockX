package com.crossbowffs.quotelock.app

import android.graphics.Typeface
import android.os.Environment
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crossbowffs.quotelock.ui.components.SnapshotText
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import com.crossbowffs.quotelock.utils.toFile
import com.yubyf.quotelockx.R
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.util.Locale

class WidgetCaptureTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Composable
    private fun WidgetPreview(text: String, source: String) {
        Box(
            modifier = Modifier
                .size(308.dp, 187.dp)
                .background(
                    color = QuoteLockTheme.quotelockColors.quoteCardOnSurface,
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = QuoteLockTheme.quotelockColors.quoteCardSurface,
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(horizontal = 16.dp)
                ) {
                    SnapshotText(
                        text = text,
                        fontSize = 22.sp,
                        fontFamily = Typeface.SERIF,
                        textAlign = TextAlign.Start,
                        color = QuoteLockTheme.quotelockColors.quoteCardOnSurface,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    SnapshotText(
                        text = source,
                        fontSize = 11.sp,
                        fontFamily = Typeface.SERIF,
                        textAlign = TextAlign.End,
                        color = QuoteLockTheme.quotelockColors.quoteCardOnSurface,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }

    @Test
    fun captureWidgetEn() {
        captureWidget(Locale.US)
    }

    @Test
    fun captureWidgetZhs() {
        captureWidget(Locale.SIMPLIFIED_CHINESE)
    }

    @Test
    fun captureWidgetZht() {
        captureWidget(Locale.TRADITIONAL_CHINESE)
    }

    private fun captureWidget(locale: Locale) {
        composeTestRule.apply {
            setContent {
                val configuration =
                    LocalContext.current.resources.configuration.apply { setLocale(locale) }
                val localContext = LocalContext.current.createConfigurationContext(configuration)
                CompositionLocalProvider(
                    LocalContext provides localContext,
                    LocalConfiguration provides configuration
                ) {
                    WidgetPreview(
                        text = stringResource(id = R.string.quote_sample_text),
                        source = stringResource(id = R.string.quote_sample_source)
                    )
                }
            }
            runBlocking {
                onRoot().captureToImage().asAndroidBitmap()
                    .toFile(
                        File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                            locale.toLanguageTag() + "-" + widgetCaptureName
                        )
                    )
            }
        }
    }

    companion object {
        private const val widgetCaptureName = "widget_capture.webp"
    }
}