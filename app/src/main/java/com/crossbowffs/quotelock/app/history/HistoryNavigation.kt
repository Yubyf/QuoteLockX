package com.crossbowffs.quotelock.app.history

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.crossbowffs.quotelock.data.api.ReadableQuote
import com.crossbowffs.quotelock.ui.navigation.QuoteNavigationDestination
import com.crossbowffs.quotelock.ui.navigation.quoteItemPageComposable

object HistoryDestination : QuoteNavigationDestination {
    override val screen: String = "history"
    override val route: String = screen
}

fun NavGraphBuilder.historyGraph(onItemClick: (ReadableQuote) -> Unit, onBack: () -> Unit) {
    quoteItemPageComposable(route = HistoryDestination.route) {
        QuoteHistoryRoute(onItemClick = onItemClick, onBack = onBack)
    }
}

fun NavHostController.navigateToHistory() =
    navigate(HistoryDestination.route)