package com.crossbowffs.quotelock.app.settings

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.crossbowffs.quotelock.ui.navigation.QuoteNavigationDestination
import com.crossbowffs.quotelock.ui.navigation.standardPageComposable

object LanguageDestination : QuoteNavigationDestination {
    override val screen: String = "language"
    override val route: String = screen
}

fun NavGraphBuilder.languageQuoteGraph(onBack: () -> Unit) {
    standardPageComposable(route = LanguageDestination.route) {
        LanguageRoute(onBack = onBack)
    }
}

fun NavHostController.navigateToLanguage() =
    navigate(LanguageDestination.route)