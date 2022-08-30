@file:OptIn(ExperimentalAnimationApi::class)

package com.crossbowffs.quotelock.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.*
import com.crossbowffs.quotelock.app.collections.collectionGraph
import com.crossbowffs.quotelock.app.collections.navigateToCollection
import com.crossbowffs.quotelock.app.configs.configGraphs
import com.crossbowffs.quotelock.app.configs.custom.customQuoteGraph
import com.crossbowffs.quotelock.app.configs.navigateToConfigScreen
import com.crossbowffs.quotelock.app.detail.DetailDestination
import com.crossbowffs.quotelock.app.detail.detailGraph
import com.crossbowffs.quotelock.app.detail.navigateToDetail
import com.crossbowffs.quotelock.app.font.fontManagementGraph
import com.crossbowffs.quotelock.app.font.navigateToFontManagement
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
            onModuleConfigItemClicked = navController::navigateToConfigScreen,
            onCollectionItemClicked = navController::navigateToCollection,
            onHistoryItemClicked = navController::navigateToHistory,
            onFontCustomize = navController::navigateToFontManagement,
        )
        customQuoteGraph(
            onItemClick = navController::navigateToDetail,
            onBack = navController::popBackStack
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
        configGraphs(navController::popBackStack)
        fontManagementGraph(navController::popBackStack)
    }
}

fun NavGraphBuilder.standardPageComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit,
) {
    val animationDuration = 300
    val enterTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?) = {
        slideIntoContainer(AnimatedContentScope.SlideDirection.Left,
            animationSpec = tween(animationDuration))
    }
    val exitTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?) = {
        slideOutOfContainer(AnimatedContentScope.SlideDirection.Left,
            animationSpec = tween(animationDuration))
    }
    val popEnterTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?) = {
        when (initialState.destination.route) {
            DetailDestination.route -> {
                slideIntoContainer(AnimatedContentScope.SlideDirection.Right,
                    animationSpec = tween(animationDuration))
            }
            else -> EnterTransition.None
        }
    }
    val popExitTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?) = {
        slideOutOfContainer(AnimatedContentScope.SlideDirection.Right,
            animationSpec = tween(animationDuration))
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