@file:OptIn(ExperimentalAnimationApi::class)

package com.crossbowffs.quotelock.app.main

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.ui.navigation.QuoteNavigationDestination
import com.google.accompanist.navigation.animation.composable

object MainDestination : QuoteNavigationDestination {
    override val screen: String = "main"
    override val route: String = screen
}

fun NavGraphBuilder.mainGraph(
    onSettingsItemClick: () -> Unit,
    onLockscreenStylesItemClick: () -> Unit,
    onCollectionItemClicked: () -> Unit,
    onHistoryItemClicked: () -> Unit,
    onFontCustomize: () -> Unit,
    onShare: () -> Unit,
    onDetail: (QuoteData) -> Unit,
) {
    composable(route = MainDestination.route) {
        MainRoute(
            onSettingsItemClick = onSettingsItemClick,
            onLockscreenStylesItemClick = onLockscreenStylesItemClick,
            onCollectionItemClick = onCollectionItemClicked,
            onHistoryItemClick = onHistoryItemClicked,
            onFontCustomize = onFontCustomize,
            onShare = onShare,
            onDetail = onDetail
        )
    }
}