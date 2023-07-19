package com.crossbowffs.quotelock.app.configs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.crossbowffs.quotelock.app.configs.brainyquote.brainyQuoteGraph
import com.crossbowffs.quotelock.app.configs.fortune.fortuneGraph
import com.crossbowffs.quotelock.app.configs.hitokoto.hitkotoGraph
import com.crossbowffs.quotelock.app.configs.openai.openaiGraph
import com.crossbowffs.quotelock.app.configs.wikiquote.wikiquoteGraph
import com.crossbowffs.quotelock.data.modules.Modules

fun NavGraphBuilder.configGraphs(onBack: () -> Unit) {
    hitkotoGraph(onBack)
    brainyQuoteGraph(onBack)
    wikiquoteGraph(onBack)
    fortuneGraph(onBack)
    openaiGraph(onBack)
}

fun NavHostController.navigateToConfigScreen(route: String) =
    Modules.values().find { it.getConfigRoute() == route }?.getConfigRoute()?.let {
        navigate(it)
    }