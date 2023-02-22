package com.crossbowffs.quotelock.app.quote

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.data.api.QuoteDataWithCollectState
import com.crossbowffs.quotelock.ui.navigation.QuoteNavigationDestination
import com.crossbowffs.quotelock.ui.navigation.standalonePageComposable

object QuoteDestination : QuoteNavigationDestination {
    const val QUOTE_CONTENT_ARG = "quote_content"
    const val COLLECT_STATE_ARG = "collect_state"

    override val screen: String = "quote"
    override val route: String =
        "$screen/{$QUOTE_CONTENT_ARG}?$COLLECT_STATE_ARG={$COLLECT_STATE_ARG}"
}

fun NavGraphBuilder.quoteGraph(
    onFontCustomize: () -> Unit,
    onShare: () -> Unit,
    onBack: () -> Unit,
) {
    standalonePageComposable(
        route = QuoteDestination.route,
        arguments = listOf(
            navArgument(QuoteDestination.QUOTE_CONTENT_ARG) { type = NavType.StringType },
            navArgument(QuoteDestination.COLLECT_STATE_ARG) {
                type = NavType.StringType
                nullable = true
            }
        )
    ) {
        val quote =
            QuoteData.fromByteString(
                it.arguments?.getString(QuoteDestination.QUOTE_CONTENT_ARG).orEmpty()
            )
        val collectState =
            it.arguments?.getString(QuoteDestination.COLLECT_STATE_ARG)
        QuoteRoute(
            quote = quote,
            initialCollectState = collectState?.toBooleanStrictOrNull(),
            onShare = onShare,
            onBack = onBack,
            onFontCustomize = onFontCustomize
        )
    }
}

fun NavHostController.navigateToQuote(quote: QuoteDataWithCollectState) = navigate(
    "${QuoteDestination.screen}/${quote.quote.byteString}" +
            "?${QuoteDestination.COLLECT_STATE_ARG}=${quote.collectState}"
)
