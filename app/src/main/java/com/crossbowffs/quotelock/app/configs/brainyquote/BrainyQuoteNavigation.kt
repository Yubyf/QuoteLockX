package com.crossbowffs.quotelock.app.configs.brainyquote

import androidx.navigation.NavGraphBuilder
import com.crossbowffs.quotelock.ui.navigation.QuoteNavigationDestination
import com.crossbowffs.quotelock.ui.navigation.standardPageComposable

object BrainyQuoteDestination : QuoteNavigationDestination {
    override val screen: String = "brainy_quote"
    override val route: String = screen
}

fun NavGraphBuilder.brainyQuoteGraph(onBack: () -> Unit) {
    standardPageComposable(route = BrainyQuoteDestination.route) {
        BrainyQuoteRoute(onBack = onBack)
    }
}