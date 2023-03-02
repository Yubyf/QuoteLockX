package com.crossbowffs.quotelock.app.detail

import androidx.navigation.NavHostController
import com.crossbowffs.quotelock.app.detail.jinrishici.DetailJinrishiciDestination
import com.crossbowffs.quotelock.data.api.QuoteData
import com.crossbowffs.quotelock.data.modules.jinrishici.JinrishiciQuoteModule
import com.crossbowffs.quotelock.utils.hexString

fun NavHostController.navigateToDetail(quote: QuoteData) = when (quote.provider) {
    JinrishiciQuoteModule.PREF_JINRISHICI -> navigate(
        "${DetailJinrishiciDestination.screen}/${quote.extra?.hexString()}"
    )

    else -> {}
}