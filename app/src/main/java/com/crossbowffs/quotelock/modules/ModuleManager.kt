package com.crossbowffs.quotelock.modules

import com.crossbowffs.quotelock.api.QuoteModule
import com.crossbowffs.quotelock.modules.brainyquote.BrainyQuoteQuoteModule
import com.crossbowffs.quotelock.modules.collections.CollectionsQuoteModule
import com.crossbowffs.quotelock.modules.custom.CustomQuoteModule
import com.crossbowffs.quotelock.modules.fortune.FortuneQuoteModule
import com.crossbowffs.quotelock.modules.freakuotes.FreakuotesQuoteModule
import com.crossbowffs.quotelock.modules.hitokoto.HitokotoQuoteModule
import com.crossbowffs.quotelock.modules.jinrishici.JinrishiciQuoteModule
import com.crossbowffs.quotelock.modules.libquotes.LibquotesQuoteModule
import com.crossbowffs.quotelock.modules.natune.NatuneQuoteModule
import com.crossbowffs.quotelock.modules.wikiquote.WikiquoteQuoteModule

object ModuleManager {
    private val sModules: MutableMap<String, QuoteModule> = LinkedHashMap()

    init {
        addLocalModule(HitokotoQuoteModule())
        addLocalModule(WikiquoteQuoteModule())
        addLocalModule(JinrishiciQuoteModule())
        addLocalModule(FreakuotesQuoteModule())
        addLocalModule(NatuneQuoteModule())
        addLocalModule(BrainyQuoteQuoteModule())
        addLocalModule(LibquotesQuoteModule())
        addLocalModule(FortuneQuoteModule())
        addLocalModule(CustomQuoteModule())
        addLocalModule(CollectionsQuoteModule())
    }

    private fun addLocalModule(module: QuoteModule) {
        val className = module::class.qualifiedName ?: ""
        sModules[className] = module
    }

    fun getModule(className: String): QuoteModule {
        val module = sModules[className]
        return module ?: throw ModuleNotFoundException("Module not found for class: $className")
    }

    fun getAllModules(): List<QuoteModule> {
        return ArrayList(sModules.values)
    }
}