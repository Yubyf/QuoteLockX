package com.crossbowffs.quotelock.app.settings

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.crossbowffs.quotelock.ui.navigation.QuoteNavigationDestination
import com.crossbowffs.quotelock.ui.navigation.standardPageComposable

object DarkModeNavigation : QuoteNavigationDestination {
    override val screen: String = "dark_mode"
    override val route: String = screen
}

fun NavGraphBuilder.darkModeQuoteGraph(onBack: () -> Unit) {
    standardPageComposable(route = DarkModeNavigation.route) {
        DarkModeRoute(onBack = onBack)
    }
}

fun NavHostController.navigateToDarkMode() =
    navigate(DarkModeNavigation.route)