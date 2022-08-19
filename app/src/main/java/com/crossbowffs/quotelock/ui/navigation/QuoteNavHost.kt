package com.crossbowffs.quotelock.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.crossbowffs.quotelock.app.detail.detailGraph
import com.crossbowffs.quotelock.app.detail.navigateToDetail
import com.crossbowffs.quotelock.app.history.HistoryDestination
import com.crossbowffs.quotelock.app.history.historyGraph
import okio.ByteString.Companion.encodeUtf8

@Composable
fun QuoteNavHost(
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
            onItemClick = { quote, source ->
                navController.navigateToDetail(quote.encodeUtf8().hex(), source.encodeUtf8().hex())
            },
            onBack = onBack
        )
    }
}