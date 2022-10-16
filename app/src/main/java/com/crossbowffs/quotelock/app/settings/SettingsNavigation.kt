package com.crossbowffs.quotelock.app.settings

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.crossbowffs.quotelock.ui.navigation.QuoteNavigationDestination
import com.crossbowffs.quotelock.ui.navigation.standardPageComposable

object SettingsDestination : QuoteNavigationDestination {
    override val screen: String = "settings"
    override val route: String = screen
}

fun NavGraphBuilder.settingsGraph(
    onLanguageItemClicked: () -> Unit,
    onModuleConfigItemClicked: (String) -> Unit,
    onAboutItemClicked: () -> Unit,
    onBack: () -> Unit,
) {
    standardPageComposable(route = SettingsDestination.route) {
        SettingsRoute(
            onLanguageItemClicked = onLanguageItemClicked,
            onModuleConfigItemClicked = onModuleConfigItemClicked,
            onAboutItemClicked = onAboutItemClicked,
            onBack = onBack
        )
    }
}

fun NavHostController.navigateToSettings() =
    navigate(SettingsDestination.route)