package com.crossbowffs.quotelock.app.font

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.crossbowffs.quotelock.ui.navigation.QuoteNavigationDestination
import com.crossbowffs.quotelock.ui.navigation.standardPageComposable

object FontManagementDestination : QuoteNavigationDestination {
    override val screen: String = "font_management"
    override val route: String = screen
}

fun NavGraphBuilder.fontManagementGraph(onBack: () -> Unit) {
    standardPageComposable(route = FontManagementDestination.route) {
        FontManagementRoute(onBack = onBack)
    }
}

fun NavHostController.navigateToFontManagement() =
    navigate(FontManagementDestination.route)