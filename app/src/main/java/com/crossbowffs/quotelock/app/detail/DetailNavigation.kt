package com.crossbowffs.quotelock.app.detail

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.crossbowffs.quotelock.ui.navigation.QuoteNavigationDestination
import okio.ByteString.Companion.decodeHex

object DetailDestination : QuoteNavigationDestination {
    const val QUOTE_ARG = "quote"
    const val SOURCE_ARG = "source"

    override val screen: String = "detail"
    override val route: String = "$screen/{$QUOTE_ARG}?$SOURCE_ARG={$SOURCE_ARG}"
}

fun NavGraphBuilder.detailGraph(onBack: () -> Unit) {
    composable(
        route = DetailDestination.route,
        arguments = listOf(
            navArgument(DetailDestination.QUOTE_ARG) { type = NavType.StringType },
            navArgument(DetailDestination.SOURCE_ARG) {
                type = NavType.StringType
                nullable = true
            },
        )
    ) {
        val quote =
            it.arguments?.getString(DetailDestination.QUOTE_ARG)?.decodeHex()?.utf8().orEmpty()
        val source =
            it.arguments?.getString(DetailDestination.SOURCE_ARG)?.decodeHex()?.utf8().orEmpty()
        QuoteDetailRoute(quote = quote, source = source, onBack = onBack)
    }
}

fun NavHostController.navigateToDetail(quote: String, source: String) {
    navigate(
        "${DetailDestination.screen}/$quote".let {
            if (source.isNotEmpty()) {
                "$it?${DetailDestination.SOURCE_ARG}=$source"
            } else it
        }
    )
}