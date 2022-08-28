package com.crossbowffs.quotelock.app.collections

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.crossbowffs.quotelock.data.api.ReadableQuote
import com.crossbowffs.quotelock.ui.navigation.QuoteNavigationDestination
import com.crossbowffs.quotelock.ui.navigation.standardPageComposable

object CollectionDestination : QuoteNavigationDestination {
    override val screen: String = "collection"
    override val route: String = screen
}

fun NavGraphBuilder.collectionGraph(onItemClick: (ReadableQuote) -> Unit, onBack: () -> Unit) {
    standardPageComposable(route = CollectionDestination.route) {
        QuoteCollectionRoute(onItemClick = onItemClick, onBack = onBack)
    }
}

fun NavHostController.navigateToCollection() =
    navigate(CollectionDestination.route)