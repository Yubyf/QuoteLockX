package com.crossbowffs.quotelock.app.history

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.crossbowffs.quotelock.ui.navigation.QuoteNavigationDestination

object HistoryDestination : QuoteNavigationDestination {
    override val screen: String = "history"
    override val route: String = screen
}

fun NavGraphBuilder.historyGraph(onItemClick: (String, String) -> Unit, onBack: () -> Unit) {
    composable(route = HistoryDestination.route) {
        QuoteHistoryRoute(onItemClick = onItemClick, onBack = onBack)
    }
}