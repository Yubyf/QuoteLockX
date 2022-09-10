package com.crossbowffs.quotelock.app.history

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.crossbowffs.quotelock.data.api.QuoteDataWithCollectState
import com.crossbowffs.quotelock.ui.navigation.QuoteNavigationDestination
import com.crossbowffs.quotelock.ui.navigation.standardPageComposable

object HistoryDestination : QuoteNavigationDestination {
    override val screen: String = "history"
    override val route: String = screen
}

fun NavGraphBuilder.historyGraph(
    onItemClick: (QuoteDataWithCollectState) -> Unit,
    onBack: () -> Unit,
) {
    standardPageComposable(route = HistoryDestination.route) {
        QuoteHistoryRoute(onItemClick = onItemClick, onBack = onBack)
    }
}

fun NavHostController.navigateToHistory() =
    navigate(HistoryDestination.route)