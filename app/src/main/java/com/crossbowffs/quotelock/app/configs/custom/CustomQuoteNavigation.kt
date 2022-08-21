package com.crossbowffs.quotelock.app.configs.custom

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.crossbowffs.quotelock.data.api.ReadableQuote
import com.crossbowffs.quotelock.ui.navigation.QuoteNavigationDestination

object CustomQuoteDestination : QuoteNavigationDestination {
    override val screen: String = "custom_quote"
    override val route: String = screen
}

fun NavGraphBuilder.customQuoteGraph(onItemClick: (ReadableQuote) -> Unit, onBack: () -> Unit) {
    composable(route = CustomQuoteDestination.route) {
        CustomQuoteRoute(onItemClick = onItemClick, onBack = onBack)
    }
}