@file:OptIn(ExperimentalTextApi::class)

package com.crossbowffs.quotelock.app

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.os.Environment
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.*
import androidx.test.platform.app.InstrumentationRegistry
import com.crossbowffs.quotelock.app.screenshot.*
import com.crossbowffs.quotelock.consts.PREF_SHARE_IMAGE_EXTENSION
import com.crossbowffs.quotelock.utils.toFile
import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.util.*

class ScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().context

    private val titleFontFamily = FontFamily(
        Font(path = "Inter-VariableFont_slnt,wght.ttf",
            assetManager = context.assets,
            variationSettings = FontVariation.Settings(FontWeight.ExtraBold,
                FontStyle.Normal)
        ))

    @Test
    fun lockscreenScreenshotTest() {
        composeTestRule.apply {
            setContent {
                val configuration =
                    LocalContext.current.resources.configuration.apply { setLocale(Locale.US) }
                val localContext = LocalContext.current.createConfigurationContext(configuration)
                CompositionLocalProvider(LocalContext provides localContext,
                    LocalConfiguration provides configuration) {
                    // parse bitmap from assets
                    val image = context.assets.open("android_13_wallpaper_1_ytechb.webp").use {
                        BitmapFactory.decodeStream(it)
                    }
                    val clockFontFamily = FontFamily(
                        Font(path = "Inter-VariableFont_slnt,wght.ttf",
                            assetManager = context.assets,
                            variationSettings = FontVariation.Settings(FontWeight.Light,
                                FontStyle.Normal)
                        ))
                    LockscreenScreenshotScreen(background = image.asImageBitmap(),
                        titleFontFamily = titleFontFamily,
                        clockFontFamily = clockFontFamily)
                }
            }
            runBlocking {
                onRoot().captureToImage().asAndroidBitmap()
                    .toFile(File(cachePath, lockscreenScreenshotName))
            }
        }
    }

    @Test
    fun mainScreenshotTest() {
        composeTestRule.apply {
            setContent {
                val configuration =
                    LocalContext.current.resources.configuration.apply { setLocale(Locale.US) }
                val localContext = LocalContext.current.createConfigurationContext(configuration)
                CompositionLocalProvider(LocalContext provides localContext,
                    LocalConfiguration provides configuration) {
                    MainScreenshotScreen(fontFamily = titleFontFamily)
                }
            }
            runBlocking {
                onRoot().captureToImage().asAndroidBitmap()
                    .toFile(File(cachePath, mainScreenshotName))
            }
        }
    }

    @Test
    fun lockscreenStyleScreenshotTest() {
        composeTestRule.apply {
            setContent {
                val configuration =
                    LocalContext.current.resources.configuration.apply { setLocale(Locale.US) }
                val localContext = LocalContext.current.createConfigurationContext(configuration)
                CompositionLocalProvider(LocalContext provides localContext,
                    LocalConfiguration provides configuration) {
                    LockscreenStyleScreenshotScreen(fontFamily = titleFontFamily)
                }
            }
            runBlocking {
                onRoot().captureToImage().asAndroidBitmap()
                    .toFile(File(cachePath, lockscreenStyleScreenshotName))
            }
        }
    }

    @Test
    fun fontCustomizationScreenshotTest() {
        composeTestRule.apply {
            setContent {
                val configuration =
                    LocalContext.current.resources.configuration.apply { setLocale(Locale.US) }
                val localContext = LocalContext.current.createConfigurationContext(configuration)
                CompositionLocalProvider(LocalContext provides localContext,
                    LocalConfiguration provides configuration) {
                    FontCustomizationScreenshotScreen(fontFamily = titleFontFamily)
                }
            }
            runBlocking {
                onRoot().captureToImage().asAndroidBitmap()
                    .toFile(File(cachePath, fontCustomizationScreenshotName))
            }
        }
    }

    @Test
    fun dynamicDarkScreenshotTest() {
        composeTestRule.apply {
            setContent {
                val configuration =
                    LocalContext.current.resources.configuration.apply { setLocale(Locale.US) }
                val localContext = LocalContext.current.createConfigurationContext(configuration)
                CompositionLocalProvider(LocalContext provides localContext,
                    LocalConfiguration provides configuration) {
                    DynamicDarkScreenshotScreen(fontFamily = titleFontFamily)
                }
            }
            runBlocking {
                onRoot().captureToImage().asAndroidBitmap()
                    .toFile(File(cachePath, dynamicDarkScreenshotName))
            }
        }
    }

    companion object {
        private const val lockscreenScreenshotName = "lockscreen.png"
        private const val mainScreenshotName = "main_screen.png"
        private const val lockscreenStyleScreenshotName = "lockscreen_style_screen.png"
        private const val fontCustomizationScreenshotName = "font_customization_screen.png"
        private const val dynamicDarkScreenshotName = "dynamic_dark.png"

        private val screenshotNames = arrayOf(
            lockscreenScreenshotName,
            mainScreenshotName,
            lockscreenStyleScreenshotName,
            fontCustomizationScreenshotName,
            dynamicDarkScreenshotName
        )

        private val targetContext = InstrumentationRegistry.getInstrumentation().targetContext

        private val cachePath = targetContext.externalCacheDir?.run {
            if (!exists()) {
                mkdirs()
            }
            File(this, "screenshots").apply {
                if (!exists()) {
                    mkdirs()
                }
            }
        }

        @AfterClass
        @JvmStatic
        fun buildScreenshot() {
            val imageFile =
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    "quotelockX_screenshot$PREF_SHARE_IMAGE_EXTENSION")
            if (imageFile.exists()) {
                imageFile.delete()
            }
            val bitmaps = screenshotNames.map { name ->
                BitmapFactory.decodeFile(File(cachePath,
                    name).also { assert(it.exists()) }.absolutePath)
            }
            val screenshotSpacing = bitmaps.maxOf { it.width } / 5
            val imageWidth = bitmaps.sumOf { it.width } + screenshotSpacing * (bitmaps.size - 1)
            val imageHeight = bitmaps.maxOf { it.height }
            val image = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(image)
            canvas.drawColor(Color.WHITE)
            bitmaps.forEach {
                canvas.drawBitmap(it, 0f, 0f, null)
                canvas.translate(it.width.toFloat() + screenshotSpacing, 0f)
            }
            runBlocking { image.toFile(imageFile) }
        }
    }
}