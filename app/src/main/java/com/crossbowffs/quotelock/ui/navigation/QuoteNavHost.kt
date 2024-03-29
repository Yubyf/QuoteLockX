@file:OptIn(ExperimentalAnimationApi::class)

package com.crossbowffs.quotelock.ui.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.crossbowffs.quotelock.app.about.aboutGraph
import com.crossbowffs.quotelock.app.about.navigateToAbout
import com.crossbowffs.quotelock.app.collections.collectionGraph
import com.crossbowffs.quotelock.app.collections.navigateToCollection
import com.crossbowffs.quotelock.app.configs.configGraphs
import com.crossbowffs.quotelock.app.configs.custom.customQuoteGraph
import com.crossbowffs.quotelock.app.configs.navigateToConfigScreen
import com.crossbowffs.quotelock.app.detail.jinrishici.detailJinrishiciGraph
import com.crossbowffs.quotelock.app.detail.navigateToDetail
import com.crossbowffs.quotelock.app.font.fontManagementGraph
import com.crossbowffs.quotelock.app.font.navigateToFontManagement
import com.crossbowffs.quotelock.app.history.historyGraph
import com.crossbowffs.quotelock.app.history.navigateToHistory
import com.crossbowffs.quotelock.app.lockscreen.styles.lockscreenStylesGraph
import com.crossbowffs.quotelock.app.lockscreen.styles.navigateToLockscreenStyles
import com.crossbowffs.quotelock.app.main.MainDestination
import com.crossbowffs.quotelock.app.main.mainGraph
import com.crossbowffs.quotelock.app.quote.QuoteDestination
import com.crossbowffs.quotelock.app.quote.navigateToQuote
import com.crossbowffs.quotelock.app.quote.quoteGraph
import com.crossbowffs.quotelock.app.settings.SettingsDestination
import com.crossbowffs.quotelock.app.settings.darkModeQuoteGraph
import com.crossbowffs.quotelock.app.settings.languageQuoteGraph
import com.crossbowffs.quotelock.app.settings.navigateToDarkMode
import com.crossbowffs.quotelock.app.settings.navigateToLanguage
import com.crossbowffs.quotelock.app.settings.navigateToSettings
import com.crossbowffs.quotelock.app.settings.settingsGraph
import com.crossbowffs.quotelock.app.share.navigateToShare
import com.crossbowffs.quotelock.app.share.shareGraph
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
            onSettingsItemClick = navController::navigateToSettings,
            onLockscreenStylesItemClick = navController::navigateToLockscreenStyles,
            onCollectionItemClicked = navController::navigateToCollection,
            onHistoryItemClicked = navController::navigateToHistory,
            onFontCustomize = navController::navigateToFontManagement,
            onShare = navController::navigateToShare,
            onDetail = navController::navigateToDetail
        )
        settingsGraph(
            onLanguageItemClicked = navController::navigateToLanguage,
            onDarkModeItemClicked = navController::navigateToDarkMode,
            onModuleConfigItemClicked = navController::navigateToConfigScreen,
            onAboutItemClicked = navController::navigateToAbout,
            onBack = navController::popBackStack
        )
        languageQuoteGraph { navController.popBackStack() }
        darkModeQuoteGraph { navController.popBackStack() }
        lockscreenStylesGraph(
            onPreviewClick = navController::navigateToQuote,
            onFontCustomize = { navController.navigateToFontManagement(1) },
            onBack = navController::popBackStack
        )
        customQuoteGraph(
            onItemClick = navController::navigateToQuote,
            onBack = navController::popBackStack
        )
        historyGraph(
            onItemClick = navController::navigateToQuote,
            onBack = navController::popBackStack
        )
        collectionGraph(
            onItemClick = navController::navigateToQuote,
            onBack = navController::popBackStack
        )
        quoteGraph(
            onFontCustomize = navController::navigateToFontManagement,
            onShare = navController::navigateToShare,
            onDetail = navController::navigateToDetail,
            onBack = navController::popBackStack
        )
        configGraphs(navController::popBackStack)
        fontManagementGraph(navController::popBackStack)
        aboutGraph(navController::popBackStack)
        shareGraph(navController::popBackStack)
        detailJinrishiciGraph(navController::popBackStack)
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
        slideIntoContainer(
            AnimatedContentScope.SlideDirection.Left,
            animationSpec = tween(animationDuration)
        )
    }
    val exitTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?) = {
        when (initialState.destination.route) {
            SettingsDestination.route -> slideOutOfContainer(
                AnimatedContentScope.SlideDirection.Left,
                animationSpec = tween(animationDuration)
            )

            else -> SCALE_FADE_OUT_TRANSITION
        }
    }
    val popEnterTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?) = {
        when (initialState.destination.route) {
            QuoteDestination.route -> SCALE_FADE_IN_TRANSITION
            else -> slideIntoContainer(
                AnimatedContentScope.SlideDirection.Right,
                animationSpec = tween(animationDuration)
            )
        }
    }
    val popExitTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?) = {
        slideOutOfContainer(
            AnimatedContentScope.SlideDirection.Right,
            animationSpec = tween(animationDuration)
        )
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

fun NavGraphBuilder.standalonePageComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit,
) = composable(
    route = route,
    arguments = arguments,
    deepLinks = deepLinks,
    enterTransition = { SCALE_FADE_IN_TRANSITION },
    exitTransition = { SCALE_FADE_OUT_TRANSITION },
    popEnterTransition = { SCALE_FADE_IN_TRANSITION },
    popExitTransition = { SCALE_FADE_OUT_TRANSITION },
    content = content
)

private const val ANIMATION_SCALE_VALUE = 0.8F
private val SCALE_FADE_IN_TRANSITION = scaleIn(initialScale = ANIMATION_SCALE_VALUE) + fadeIn()
private val SCALE_FADE_OUT_TRANSITION = scaleOut(targetScale = ANIMATION_SCALE_VALUE) + fadeOut()