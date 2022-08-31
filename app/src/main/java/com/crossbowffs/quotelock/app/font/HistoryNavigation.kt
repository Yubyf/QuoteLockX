package com.crossbowffs.quotelock.app.font

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.crossbowffs.quotelock.ui.navigation.QuoteNavigationDestination
import com.crossbowffs.quotelock.ui.navigation.standalonePageComposable

object FontManagementDestination : QuoteNavigationDestination {
    override val screen: String = "font_management"
    override val route: String = screen
}

fun NavGraphBuilder.fontManagementGraph(onBack: () -> Unit) {
    standalonePageComposable(route = FontManagementDestination.route) {
        FontManagementRoute(onBack = onBack)
    }
}

fun NavHostController.navigateToFontManagement() =
    navigate(FontManagementDestination.route)