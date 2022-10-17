package com.crossbowffs.quotelock.app.font

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.crossbowffs.quotelock.ui.navigation.QuoteNavigationDestination
import com.crossbowffs.quotelock.ui.navigation.standalonePageComposable

object FontManagementDestination : QuoteNavigationDestination {
    const val TAB_ARG = "tab"

    override val screen: String = "font_management"
    override val route: String = "${screen}/{$TAB_ARG}"
}

fun NavGraphBuilder.fontManagementGraph(onBack: () -> Unit) {
    standalonePageComposable(
        route = FontManagementDestination.route,
        arguments = listOf(
            navArgument(FontManagementDestination.TAB_ARG) { type = NavType.IntType }
        )
    ) {
        val tab = it.arguments?.getInt(FontManagementDestination.TAB_ARG) ?: 0
        FontManagementRoute(initialTab = tab, onBack = onBack)
    }
}

fun NavHostController.navigateToFontManagement(tab: Int = 0) =
    navigate("${FontManagementDestination.screen}/$tab")