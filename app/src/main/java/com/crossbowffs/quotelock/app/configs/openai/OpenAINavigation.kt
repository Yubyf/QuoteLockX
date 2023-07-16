package com.crossbowffs.quotelock.app.configs.openai

import androidx.navigation.NavGraphBuilder
import com.crossbowffs.quotelock.ui.navigation.QuoteNavigationDestination
import com.crossbowffs.quotelock.ui.navigation.standardPageComposable

object OpenAINavigation : QuoteNavigationDestination {
    override val screen: String = "openai"
    override val route: String = screen
}

fun NavGraphBuilder.openaiGraph(onBack: () -> Unit) {
    standardPageComposable(route = OpenAINavigation.route) {
        OpenAIRoute(onBack = onBack)
    }
}