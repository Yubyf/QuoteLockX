package com.crossbowffs.quotelock.app.screenshot

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crossbowffs.quotelock.app.detail.QuoteDetailUiState
import com.crossbowffs.quotelock.app.detail.style.CardStyleUiState
import com.crossbowffs.quotelock.app.emptySnackBarEvent
import com.crossbowffs.quotelock.app.font.FontManagementListUiState
import com.crossbowffs.quotelock.app.font.FontManagementScreen
import com.crossbowffs.quotelock.app.font.FontManager
import com.crossbowffs.quotelock.app.lockscreen.styles.LockscreenStylesScreen
import com.crossbowffs.quotelock.app.lockscreen.styles.LockscreenStylesUiState
import com.crossbowffs.quotelock.app.lockscreen.styles.PreviewScreen
import com.crossbowffs.quotelock.app.lockscreen.styles.PreviewUiState
import com.crossbowffs.quotelock.app.main.MainScreen
import com.crossbowffs.quotelock.app.main.MainUiState
import com.crossbowffs.quotelock.consts.PREF_COMMON_FONT_SIZE_SOURCE_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_COMMON_FONT_SIZE_TEXT_DEFAULT
import com.crossbowffs.quotelock.consts.PREF_QUOTE_SOURCE_PREFIX
import com.crossbowffs.quotelock.data.api.CardStyle
import com.crossbowffs.quotelock.data.api.QuoteDataWithCollectState
import com.crossbowffs.quotelock.data.api.QuoteStyle
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.TestOnly

private val SCREENSHOT_WIDTH = 380.dp
private val SCREENSHOT_HEIGHT = 831.dp
private val TITLE_HEIGHT = 142.dp

private val PREF_QUOTE = QuoteDataWithCollectState(
    "Knowledge is power.",
    "",
    "Francis Bacon",
    false
)
private val PREF_CARD_STYLE = CardStyle(quoteSize = 23, sourceSize = 14)

@TestOnly
@Composable
fun LockscreenScreenshotScreen(
    background: ImageBitmap? = null,
    titleFontFamily: FontFamily = FontFamily.Default,
    clockFontFamily: FontFamily = FontFamily.Default,
) {
    QuoteLockTheme(useDarkTheme = false, dynamicColor = false) {
        Column(modifier = Modifier
            .size(SCREENSHOT_WIDTH, SCREENSHOT_HEIGHT)
            .background(Color.White)
            .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween) {
            Box(modifier = Modifier.requiredHeight(TITLE_HEIGHT),
                contentAlignment = Alignment.Center) {
                Text(text = "Lockscreen",
                    fontSize = 42.sp,
                    fontFamily = titleFontFamily,
                    fontWeight = FontWeight.ExtraBold)
            }
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(SCREENSHOT_HEIGHT)
                .padding(24.dp)
                .shadow(elevation = 16.dp, shape = RoundedCornerShape(12.dp))) {
                Surface {
                    background?.let {
                        Image(bitmap = it,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillHeight)
                    }
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.2F)))
                    Column(modifier = Modifier.fillMaxSize()) {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(text = "5:05",
                            modifier = Modifier.padding(horizontal = 20.dp),
                            fontSize = 68.sp,
                            fontFamily = clockFontFamily,
                            color = Color(0XFFDAE2FF))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Tue, May 05",
                            modifier = Modifier.padding(horizontal = 24.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Knowledge is power.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 22.dp),
                            fontSize = PREF_COMMON_FONT_SIZE_TEXT_DEFAULT.toInt().sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = Color.White
                        )
                        Text(
                            text = "${PREF_QUOTE_SOURCE_PREFIX}Francis Bacon",
                            modifier = Modifier
                                .wrapContentWidth()
                                .padding(top = 8.dp, end = 22.dp)
                                .alpha(0.7F)
                                .align(Alignment.End),
                            fontSize = PREF_COMMON_FONT_SIZE_SOURCE_DEFAULT.toInt().sp,
                            color = Color.White
                        )
                        @Composable
                        fun Notification(shape: Shape = RoundedCornerShape(2.dp)) {
                            Row(modifier = Modifier
                                .height(72.dp)
                                .padding(horizontal = 16.dp)
                                .clip(shape)
                                .background(Color.White.copy(alpha = 0.9F)),
                                verticalAlignment = Alignment.CenterVertically) {
                                Spacer(modifier = Modifier.width(16.dp))
                                Box(modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.DarkGray.copy(alpha = 0.2F)))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)) {
                                    Box(modifier = Modifier
                                        .fillMaxWidth(0.5F)
                                        .height(14.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color.DarkGray.copy(alpha = 0.2F)))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(modifier = Modifier
                                        .fillMaxWidth(0.9F)
                                        .height(14.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color.DarkGray.copy(alpha = 0.2F)))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(36.dp))
                        repeat(3) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Notification(when (it) {
                                0 -> RoundedCornerShape(topStart = 28.dp,
                                    topEnd = 28.dp,
                                    bottomStart = 2.dp,
                                    bottomEnd = 2.dp)
                                2 -> RoundedCornerShape(topStart = 2.dp,
                                    topEnd = 2.dp,
                                    bottomStart = 28.dp,
                                    bottomEnd = 28.dp)
                                else -> RoundedCornerShape(2.dp)
                            })
                        }
                        Spacer(modifier = Modifier.height(112.dp))
                        Box(modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .align(Alignment.CenterHorizontally),
                            contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Lock, contentDescription = null, modifier = Modifier
                                .size(32.dp))
                        }
                    }
                }
            }
        }
    }
}

@TestOnly
@Composable
fun MainScreenshotScreen(fontFamily: FontFamily = FontFamily.Default) {
    QuoteLockTheme(useDarkTheme = false, dynamicColor = false) {
        Column(modifier = Modifier
            .size(SCREENSHOT_WIDTH, SCREENSHOT_HEIGHT)
            .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween) {
            Box(modifier = Modifier
                .weight(1F)
                .padding(24.dp)
                .shadow(elevation = 16.dp, shape = RoundedCornerShape(12.dp))) {
                MainScreen(
                    mainUiState = MainUiState(quoteData = PREF_QUOTE),
                    detailUiState = QuoteDetailUiState(PREF_CARD_STYLE),
                    cardStyleUiState = CardStyleUiState(fonts = emptyList(),
                        cardStyle = CardStyle())
                )
            }
            Box(modifier = Modifier.height(TITLE_HEIGHT), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Quote Card",
                        fontSize = 42.sp,
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.ExtraBold)
                    Text(text = "Collect & Share",
                        style = MaterialTheme.typography.headlineSmall,
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@TestOnly
@Composable
fun LockscreenStyleScreenshotScreen(fontFamily: FontFamily = FontFamily.Default) {
    QuoteLockTheme(useDarkTheme = false, dynamicColor = false) {
        Column(modifier = Modifier
            .size(SCREENSHOT_WIDTH, SCREENSHOT_HEIGHT)
            .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween) {
            Box(modifier = Modifier.height(TITLE_HEIGHT), contentAlignment = Alignment.Center) {
                Text(text = "Lockscreen Style",
                    fontSize = 42.sp,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.ExtraBold)
            }
            Column(modifier = Modifier
                .weight(1F)
                .padding(24.dp)
                .shadow(elevation = 16.dp, shape = RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)) {
                PreviewScreen(uiState = PreviewUiState(PREF_QUOTE, quoteStyle = QuoteStyle()))
                LockscreenStylesScreen(uiState = LockscreenStylesUiState(true))
            }
        }
    }
}

@TestOnly
@Composable
fun FontCustomizationScreenshotScreen(fontFamily: FontFamily = FontFamily.Default) {
    QuoteLockTheme(useDarkTheme = false, dynamicColor = false) {
        Column(modifier = Modifier
            .size(SCREENSHOT_WIDTH, SCREENSHOT_HEIGHT)
            .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween) {
            Box(modifier = Modifier
                .weight(1F)
                .padding(24.dp)
                .shadow(elevation = 16.dp, shape = RoundedCornerShape(12.dp))) {
                val inspectionMode = LocalInspectionMode.current
                val uiState = runBlocking {
                    FontManagementListUiState(
                        inAppFontItems = if (!inspectionMode) FontManager.loadInAppFontsList()
                            ?: emptyList() else emptyList(),
                        systemFontItems = emptyList(),
                        systemTabScrollToBottom = false,
                        inAppTabScrollToBottom = false
                    )
                }
                FontManagementScreen(uiState = uiState,
                    uiEvent = emptySnackBarEvent,
                    onBack = null,
                    onInAppImportButtonClick = {},
                    onSystemImportButtonClick = {},
                    onSystemFontDeleteMenuClick = {},
                    onInAppFontDeleteMenuClick = {},
                    snackBarShown = {},
                    listScrolled = {})
            }
            Box(modifier = Modifier.height(TITLE_HEIGHT), contentAlignment = Alignment.Center) {
                Text(text = "Font Customize",
                    fontSize = 42.sp,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@TestOnly
@Composable
fun DynamicDarkScreenshotScreen(fontFamily: FontFamily = FontFamily.Default) {
    QuoteLockTheme(useDarkTheme = false) {
        Column(modifier = Modifier
            .size(SCREENSHOT_WIDTH, SCREENSHOT_HEIGHT)
            .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween) {
            Box(modifier = Modifier.height(TITLE_HEIGHT), contentAlignment = Alignment.Center) {
                Text(text = "Dark & Dynamic",
                    fontSize = 42.sp,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.ExtraBold)
            }
            Box(modifier = Modifier
                .width(SCREENSHOT_WIDTH)
                .weight(1F)) {
                QuoteLockTheme(useDarkTheme = true) {
                    Surface(modifier = Modifier
                        .width(SCREENSHOT_WIDTH)
                        .padding(24.dp)
                        .shadow(elevation = 16.dp, shape = RoundedCornerShape(12.dp))) {
                        MainScreen(
                            mainUiState = MainUiState(quoteData = PREF_QUOTE),
                            detailUiState = QuoteDetailUiState(PREF_CARD_STYLE),
                            cardStyleUiState = CardStyleUiState(fonts = emptyList(),
                                cardStyle = CardStyle())
                        )
                    }
                }
                Surface(modifier = Modifier.offset(x = SCREENSHOT_WIDTH / 2)) {
                    QuoteLockTheme(useDarkTheme = false) {
                        Surface(modifier = Modifier
                            .width(SCREENSHOT_WIDTH)
                            .offset(x = -SCREENSHOT_WIDTH / 2)
                            .padding(24.dp)
                            .shadow(elevation = 16.dp, shape = RoundedCornerShape(12.dp))) {
                            MainScreen(
                                mainUiState = MainUiState(quoteData = PREF_QUOTE),
                                detailUiState = QuoteDetailUiState(PREF_CARD_STYLE),
                                cardStyleUiState = CardStyleUiState(fonts = emptyList(),
                                    cardStyle = CardStyle())
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LockscreenScreenshotTestPreview() {
    LockscreenScreenshotScreen()
}

@Preview(showBackground = true)
@Composable
fun MainScreenshotTestPreview() {
    MainScreenshotScreen()
}

@Preview(showBackground = true)
@Composable
fun LockscreenStyleScreenshotTestPreview() {
    LockscreenStyleScreenshotScreen()
}

@Preview(showBackground = true)
@Composable
fun FontListScreenshotTestPreview() {
    FontCustomizationScreenshotScreen()
}

@Preview(showBackground = true)
@Composable
fun DynamicDarkScreenshotTestPreview() {
    DynamicDarkScreenshotScreen()
}