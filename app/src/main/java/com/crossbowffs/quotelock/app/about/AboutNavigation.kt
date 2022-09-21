package com.crossbowffs.quotelock.app.about

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.crossbowffs.quotelock.ui.navigation.QuoteNavigationDestination
import com.crossbowffs.quotelock.ui.navigation.standardPageComposable

object AboutDestination : QuoteNavigationDestination {
    override val screen: String = "about"
    override val route: String = screen
}

fun NavGraphBuilder.aboutGraph(
    onBack: () -> Unit,
) {
    standardPageComposable(route = AboutDestination.route) {
        AboutRoute(
            onBack = onBack
        )
    }
}

fun NavHostController.navigateToAbout() =
    navigate(AboutDestination.route)