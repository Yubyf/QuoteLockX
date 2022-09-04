@file:OptIn(ExperimentalAnimationApi::class)

package com.crossbowffs.quotelock.app.detail

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.crossbowffs.quotelock.consts.PREF_QUOTE_SOURCE_PREFIX
import com.crossbowffs.quotelock.data.api.ReadableQuote
import com.crossbowffs.quotelock.ui.navigation.QuoteNavigationDestination
import com.crossbowffs.quotelock.ui.navigation.standalonePageComposable
import okio.ByteString.Companion.decodeHex
import okio.ByteString.Companion.encodeUtf8

object DetailDestination : QuoteNavigationDestination {
    const val QUOTE_ARG = "quote"
    const val SOURCE_ARG = "source"

    override val screen: String = "detail"
    override val route: String = "$screen/{$QUOTE_ARG}?$SOURCE_ARG={$SOURCE_ARG}"
}

fun NavGraphBuilder.detailGraph(onBack: () -> Unit) {
    standalonePageComposable(
        route = DetailDestination.route,
        arguments = listOf(
            navArgument(DetailDestination.QUOTE_ARG) { type = NavType.StringType },
            navArgument(DetailDestination.SOURCE_ARG) {
                type = NavType.StringType
                nullable = true
            },
        ),
    ) {
        val quote =
            it.arguments?.getString(DetailDestination.QUOTE_ARG)?.decodeHex()?.utf8().orEmpty()
        val source =
            it.arguments?.getString(DetailDestination.SOURCE_ARG)?.decodeHex()?.utf8()
        QuoteDetailRoute(quote = quote, source = source, onBack = onBack)
    }
}

fun NavHostController.navigateToDetail(quote: ReadableQuote) {
    val encodedText = quote.text.encodeUtf8().hex()
    val encodedSource = quote.source?.let {
        (if (it.startsWith(PREF_QUOTE_SOURCE_PREFIX)) it else PREF_QUOTE_SOURCE_PREFIX + it)
            .encodeUtf8().hex()
    }
    navigate(
        "${DetailDestination.screen}/$encodedText".let {
            if (!encodedSource.isNullOrBlank()) {
                "$it?${DetailDestination.SOURCE_ARG}=$encodedSource"
            } else it
        }
    )
}