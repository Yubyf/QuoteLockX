package com.crossbowffs.quotelock.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.crossbowffs.quotelock.app.configs.collections.CollectionDestination
import com.crossbowffs.quotelock.app.configs.collections.collectionGraph
import com.crossbowffs.quotelock.app.configs.custom.CustomQuoteDestination
import com.crossbowffs.quotelock.app.configs.custom.customQuoteGraph
import com.crossbowffs.quotelock.app.detail.detailGraph
import com.crossbowffs.quotelock.app.detail.navigateToDetail
import com.crossbowffs.quotelock.app.history.HistoryDestination
import com.crossbowffs.quotelock.app.history.historyGraph

@Composable
fun CustomQuoteNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = CustomQuoteDestination.screen,
    onBack: () -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        customQuoteGraph(
            onItemClick = navController::navigateToDetail,
            onBack = onBack
        )
        detailGraph {
            navController.popBackStack()
        }
    }
}

@Composable
fun QuoteHistoryNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = HistoryDestination.screen,
    onBack: () -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        detailGraph {
            navController.popBackStack()
        }
        historyGraph(
            onItemClick = navController::navigateToDetail,
            onBack = onBack
        )
    }
}

@Composable
fun QuoteCollectionNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = CollectionDestination.screen,
    onBack: () -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        detailGraph {
            navController.popBackStack()
        }
        collectionGraph(
            onItemClick = navController::navigateToDetail,
            onBack = onBack
        )
    }
}