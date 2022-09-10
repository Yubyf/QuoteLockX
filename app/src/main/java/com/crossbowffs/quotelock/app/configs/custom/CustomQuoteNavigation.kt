package com.crossbowffs.quotelock.app.configs.custom

import androidx.navigation.NavGraphBuilder
import com.crossbowffs.quotelock.data.api.QuoteDataWithCollectState
import com.crossbowffs.quotelock.ui.navigation.QuoteNavigationDestination
import com.crossbowffs.quotelock.ui.navigation.standardPageComposable

object CustomQuoteDestination : QuoteNavigationDestination {
    override val screen: String = "custom_quote"
    override val route: String = screen
}

fun NavGraphBuilder.customQuoteGraph(
    onItemClick: (QuoteDataWithCollectState) -> Unit,
    onBack: () -> Unit,
) {
    standardPageComposable(route = CustomQuoteDestination.route) {
        CustomQuoteRoute(onItemClick = onItemClick, onBack = onBack)
    }
}