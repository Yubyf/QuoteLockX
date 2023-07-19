package com.crossbowffs.quotelock.app.configs.wikiquote

import androidx.navigation.NavGraphBuilder
import com.crossbowffs.quotelock.ui.navigation.QuoteNavigationDestination
import com.crossbowffs.quotelock.ui.navigation.standardPageComposable

object WikiquoteDestination : QuoteNavigationDestination {
    override val screen: String = "wiki_quote"
    override val route: String = screen
}

fun NavGraphBuilder.wikiquoteGraph(onBack: () -> Unit) {
    standardPageComposable(route = WikiquoteDestination.route) {
        WikiquoteRoute(onBack = onBack)
    }
}