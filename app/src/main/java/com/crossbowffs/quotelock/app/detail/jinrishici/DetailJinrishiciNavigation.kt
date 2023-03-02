package com.crossbowffs.quotelock.app.detail.jinrishici

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.crossbowffs.quotelock.app.detail.jinrishici.DetailJinrishiciDestination.JINRISHICI_DETAIL_ARG
import com.crossbowffs.quotelock.data.modules.jinrishici.detail.JinrishiciDetailData
import com.crossbowffs.quotelock.ui.navigation.QuoteNavigationDestination
import com.crossbowffs.quotelock.ui.navigation.standardPageComposable

object DetailJinrishiciDestination : QuoteNavigationDestination {
    const val JINRISHICI_DETAIL_ARG = "jinrishici_detail"
    override val screen: String = "detail_jinrishici"
    override val route: String = "$screen/{$JINRISHICI_DETAIL_ARG}"
}

fun NavGraphBuilder.detailJinrishiciGraph(onBack: () -> Unit) {
    standardPageComposable(
        route = DetailJinrishiciDestination.route,
        arguments = listOf(
            navArgument(JINRISHICI_DETAIL_ARG) { type = NavType.StringType },
        )
    ) {
        val quoteDetailData = it.arguments?.getString(JINRISHICI_DETAIL_ARG)?.let(
            JinrishiciDetailData.Companion::fromByteString
        )
        DetailJinrishiciRoute(detailData = quoteDetailData, onBack = onBack)
    }
}