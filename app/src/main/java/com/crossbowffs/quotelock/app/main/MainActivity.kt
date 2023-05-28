package com.crossbowffs.quotelock.app.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.core.util.Consumer
import com.crossbowffs.quotelock.app.quote.QuoteDestination
import com.crossbowffs.quotelock.app.quote.navigateToQuote
import com.crossbowffs.quotelock.data.ConfigurationRepository
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.data.api.withCollectState
import com.crossbowffs.quotelock.ui.navigation.MainNavHost
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var configurationRepository: ConfigurationRepository

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configurationRepository.nightMode.takeIf { it > 0 }
            ?.let(AppCompatDelegate::setDefaultNightMode)

        setContent {
            QuoteLockTheme {
                val systemUiController = rememberSystemUiController()
                val useDarkIcons = !isSystemInDarkTheme()
                val navController = rememberAnimatedNavController()

                DisposableEffect(systemUiController, useDarkIcons) {
                    // Update all of the system bar colors to be transparent, and use
                    // dark icons if we're in light theme
                    systemUiController.setSystemBarsColor(
                        color = Color.Transparent,
                        darkIcons = useDarkIcons
                    )
                    systemUiController.setNavigationBarColor(
                        color = Color.Transparent,
                        darkIcons = useDarkIcons
                    )
                    onDispose {}
                }
                DisposableEffect(Unit) {
                    val listener = Consumer<Intent> {
                        it?.extras?.let { bundle ->
                            bundle.getString(QuoteDestination.QUOTE_CONTENT_ARG)?.let { content ->
                                val collectionState =
                                    bundle.getBoolean(QuoteDestination.COLLECT_STATE_ARG)
                                navController.navigateToQuote(
                                    QuoteData.fromByteString(content)
                                        .withCollectState(collectionState)
                                )
                            }
                        }
                    }
                    addOnNewIntentListener(listener)
                    onDispose { removeOnNewIntentListener(listener) }
                }
                MainNavHost(navController = navController)
            }
        }
    }
}