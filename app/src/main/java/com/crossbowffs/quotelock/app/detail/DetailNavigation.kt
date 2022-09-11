@file:OptIn(ExperimentalAnimationApi::class)

package com.crossbowffs.quotelock.app.detail

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.crossbowffs.quotelock.data.api.QuoteDataWithCollectState
import com.crossbowffs.quotelock.ui.navigation.QuoteNavigationDestination
import com.crossbowffs.quotelock.ui.navigation.standalonePageComposable
import okio.ByteString.Companion.decodeHex
import okio.ByteString.Companion.encodeUtf8

object DetailDestination : QuoteNavigationDestination {
    const val QUOTE_ARG = "quote"
    const val SOURCE_ARG = "source"
    const val AUTHOR_ARG = "author"
    const val COLLECT_STATE_ARG = "collect_state"

    override val screen: String = "detail"
    override val route: String =
        "$screen/{$QUOTE_ARG}?$SOURCE_ARG={$SOURCE_ARG}&$AUTHOR_ARG={$AUTHOR_ARG}&$COLLECT_STATE_ARG={$COLLECT_STATE_ARG}"
}

fun NavGraphBuilder.detailGraph(onFontCustomize: () -> Unit, onBack: () -> Unit) {
    standalonePageComposable(
        route = DetailDestination.route,
        arguments = listOf(
            navArgument(DetailDestination.QUOTE_ARG) { type = NavType.StringType },
            navArgument(DetailDestination.SOURCE_ARG) {
                type = NavType.StringType
                nullable = true
            },
            navArgument(DetailDestination.AUTHOR_ARG) {
                type = NavType.StringType
                nullable = true
            },
            navArgument(DetailDestination.COLLECT_STATE_ARG) {
                type = NavType.StringType
                nullable = true
            }
        ),
    ) {
        val quote =
            it.arguments?.getString(DetailDestination.QUOTE_ARG)?.decodeHex()?.utf8().orEmpty()
        val source =
            it.arguments?.getString(DetailDestination.SOURCE_ARG)?.decodeHex()?.utf8()
        val author =
            it.arguments?.getString(DetailDestination.AUTHOR_ARG)?.decodeHex()?.utf8()
        val collectState =
            it.arguments?.getString(DetailDestination.COLLECT_STATE_ARG)
        QuoteDetailRoute(quote = quote,
            source = source,
            author = author,
            initialCollectState = collectState?.toBooleanStrictOrNull(),
            onBack = onBack,
            onFontCustomize = onFontCustomize)
    }
}

fun NavHostController.navigateToDetail(quote: QuoteDataWithCollectState) {
    val encodedText = quote.quoteText.encodeUtf8().hex()
    val encodedSource = quote.quoteSource.encodeUtf8().hex()
    val encodedAuthor = quote.quoteAuthor.encodeUtf8().hex()
    val optionArgs = encodedSource.takeIf { it.isNotBlank() }?.let {
        "${DetailDestination.SOURCE_ARG}=$encodedSource"
    }.let { source ->
        encodedAuthor.takeIf { it.isNotBlank() }?.let {
            (if (source.isNullOrBlank()) "" else "$source&") +
                    "${DetailDestination.AUTHOR_ARG}=$it"
        } ?: source
    }.let { sourceAndAuthor ->
        quote.collectState?.let {
            (if (sourceAndAuthor.isNullOrBlank()) "" else "$sourceAndAuthor&") +
                    "${DetailDestination.COLLECT_STATE_ARG}=$it"
        } ?: sourceAndAuthor
    }.takeIf { !it.isNullOrBlank() }?.let { "?$it" } ?: ""
    navigate(
        "${DetailDestination.screen}/$encodedText$optionArgs"
    )
}