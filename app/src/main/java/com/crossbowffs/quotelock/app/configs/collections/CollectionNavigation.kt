@file:OptIn(ExperimentalAnimationApi::class)

package com.crossbowffs.quotelock.app.configs.collections

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.crossbowffs.quotelock.data.api.ReadableQuote
import com.crossbowffs.quotelock.ui.navigation.QuoteNavigationDestination
import com.crossbowffs.quotelock.ui.navigation.quoteItemPageComposable

object CollectionDestination : QuoteNavigationDestination {
    override val screen: String = "collection"
    override val route: String = screen
}

fun NavGraphBuilder.collectionGraph(onItemClick: (ReadableQuote) -> Unit, onBack: () -> Unit) {
    quoteItemPageComposable(route = CollectionDestination.route) {
        QuoteCollectionRoute(onItemClick = onItemClick, onBack = onBack)
    }
}

fun NavHostController.navigateToCollection() =
    navigate(CollectionDestination.route)