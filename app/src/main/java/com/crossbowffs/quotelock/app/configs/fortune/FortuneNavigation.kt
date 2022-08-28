package com.crossbowffs.quotelock.app.configs.fortune

import androidx.navigation.NavGraphBuilder
import com.crossbowffs.quotelock.ui.navigation.QuoteNavigationDestination
import com.crossbowffs.quotelock.ui.navigation.standardPageComposable

object FortuneNavigation : QuoteNavigationDestination {
    override val screen: String = "fortune"
    override val route: String = screen
}

fun NavGraphBuilder.fortuneGraph(onBack: () -> Unit) {
    standardPageComposable(route = FortuneNavigation.route) {
        FortuneRoute(onBack = onBack)
    }
}