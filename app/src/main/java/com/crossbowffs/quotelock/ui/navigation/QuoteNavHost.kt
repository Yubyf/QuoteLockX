@file:OptIn(ExperimentalAnimationApi::class)

package com.crossbowffs.quotelock.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.*
import com.crossbowffs.quotelock.app.configs.collections.collectionGraph
import com.crossbowffs.quotelock.app.configs.collections.navigateToCollection
import com.crossbowffs.quotelock.app.configs.custom.CustomQuoteDestination
import com.crossbowffs.quotelock.app.configs.custom.customQuoteGraph
import com.crossbowffs.quotelock.app.detail.DetailDestination
import com.crossbowffs.quotelock.app.detail.detailGraph
import com.crossbowffs.quotelock.app.detail.navigateToDetail
import com.crossbowffs.quotelock.app.history.historyGraph
import com.crossbowffs.quotelock.app.history.navigateToHistory
import com.crossbowffs.quotelock.app.main.MainDestination
import com.crossbowffs.quotelock.app.main.mainGraph
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

@Composable
fun MainNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberAnimatedNavController(),
    startDestination: String = MainDestination.route,
) {
    AnimatedNavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        mainGraph(
            onCollectionItemClicked = navController::navigateToCollection,
            onHistoryItemClicked = navController::navigateToHistory,
        )
        historyGraph(
            onItemClick = navController::navigateToDetail,
            onBack = navController::popBackStack
        )
        collectionGraph(
            onItemClick = { navController.navigateToDetail(it); },
            onBack = navController::popBackStack
        )
        detailGraph(navController::popBackStack)
    }
}

@Composable
fun CustomQuoteNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberAnimatedNavController(),
    startDestination: String = CustomQuoteDestination.screen,
    onBack: () -> Unit,
) {
    AnimatedNavHost(
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

fun NavGraphBuilder.quoteItemPageComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit,
) {
    val enterTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?) = {
        slideIntoContainer(AnimatedContentScope.SlideDirection.Left,
            animationSpec = tween(500))
    }
    val exitTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?) = {
        slideOutOfContainer(AnimatedContentScope.SlideDirection.Left,
            animationSpec = tween(500))
    }
    val popEnterTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?) = {
        when (initialState.destination.route) {
            DetailDestination.route -> {
                slideIntoContainer(AnimatedContentScope.SlideDirection.Right,
                    animationSpec = tween(500))
            }
            else -> EnterTransition.None
        }
    }
    val popExitTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?) = {
        slideOutOfContainer(AnimatedContentScope.SlideDirection.Right,
            animationSpec = tween(500))
    }
    composable(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition,
        content = content
    )
}