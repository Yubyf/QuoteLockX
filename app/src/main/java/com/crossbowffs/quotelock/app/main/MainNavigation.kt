@file:OptIn(ExperimentalAnimationApi::class)

package com.crossbowffs.quotelock.app.main

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import com.crossbowffs.quotelock.ui.navigation.QuoteNavigationDestination
import com.google.accompanist.navigation.animation.composable

object MainDestination : QuoteNavigationDestination {
    override val screen: String = "main"
    override val route: String = screen
}

fun NavGraphBuilder.mainGraph(
    onCollectionItemClicked: () -> Unit,
    onHistoryItemClicked: () -> Unit,
) {
    composable(route = MainDestination.route) {
        MainScreen(onCollectionItemClicked = onCollectionItemClicked,
            onHistoryItemClicked = onHistoryItemClicked)
    }
}