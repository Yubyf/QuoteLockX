package com.crossbowffs.quotelock.app

import android.os.Environment
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.crossbowffs.quotelock.app.screenshot.WidgetPreview
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