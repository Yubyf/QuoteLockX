package com.crossbowffs.quotelock.app.share

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.crossbowffs.quotelock.ui.navigation.QuoteNavigationDestination
import com.crossbowffs.quotelock.ui.navigation.standalonePageComposable

object ShareDestination : QuoteNavigationDestination {
    override val screen: String = "share"
    override val route: String = screen
}

fun NavGraphBuilder.shareGraph(onBack: () -> Unit) {
    standalonePageComposable(
        route = ShareDestination.route,
    ) {
        ShareRoute(onBack = onBack)
    }
}

fun NavHostController.navigateToShare() =
    navigate(ShareDestination.screen)