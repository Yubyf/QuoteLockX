package com.crossbowffs.quotelock.app.lockscreen.styles

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.crossbowffs.quotelock.data.api.ReadableQuote
import com.crossbowffs.quotelock.ui.navigation.QuoteNavigationDestination
import com.crossbowffs.quotelock.ui.navigation.standardPageComposable

object LockscreenStylesDestination : QuoteNavigationDestination {
    override val screen: String = "lockscreen_styles"
    override val route: String = screen
}

fun NavGraphBuilder.lockscreenStylesGraph(
    onPreviewClick: (ReadableQuote) -> Unit,
    onFontCustomize: () -> Unit,
    onBack: () -> Unit,
) {
    standardPageComposable(route = LockscreenStylesDestination.route) {
        LockscreenStylesRoute(
            onPreviewClick = onPreviewClick,
            onFontCustomize = onFontCustomize,
            onBack = onBack
        )
    }
}

fun NavHostController.navigateToLockscreenStyles() =
    navigate(LockscreenStylesDestination.route)