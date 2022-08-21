package com.crossbowffs.quotelock.app.configs.collections

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.crossbowffs.quotelock.data.api.ReadableQuote
import com.crossbowffs.quotelock.ui.navigation.QuoteNavigationDestination

object CollectionDestination : QuoteNavigationDestination {
    override val screen: String = "collection"
    override val route: String = screen
}

fun NavGraphBuilder.collectionGraph(onItemClick: (ReadableQuote) -> Unit, onBack: () -> Unit) {
    composable(route = CollectionDestination.route) {
        QuoteCollectionRoute(onItemClick = onItemClick, onBack = onBack)
    }
}