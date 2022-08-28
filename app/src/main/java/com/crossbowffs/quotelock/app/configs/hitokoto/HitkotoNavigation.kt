package com.crossbowffs.quotelock.app.configs.hitokoto

import androidx.navigation.NavGraphBuilder
import com.crossbowffs.quotelock.ui.navigation.QuoteNavigationDestination
import com.crossbowffs.quotelock.ui.navigation.standardPageComposable

object HitkotoNavigation : QuoteNavigationDestination {
    override val screen: String = "hitokoto"
    override val route: String = screen
}

fun NavGraphBuilder.hitkotoGraph(onBack: () -> Unit) {
    standardPageComposable(route = HitkotoNavigation.route) {
        HitokotoRoute(onBack = onBack)
    }
}