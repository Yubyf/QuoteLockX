package com.crossbowffs.quotelock.data.modules

import com.crossbowffs.quotelock.data.api.QuoteModule
import com.crossbowffs.quotelock.data.modules.brainyquote.BrainyQuoteQuoteModule
import com.crossbowffs.quotelock.data.modules.collections.CollectionsQuoteModule
import com.crossbowffs.quotelock.data.modules.custom.CustomQuoteModule
import com.crossbowffs.quotelock.data.modules.fortune.FortuneQuoteModule
import com.crossbowffs.quotelock.data.modules.freakuotes.FreakuotesQuoteModule
import com.crossbowffs.quotelock.data.modules.hitokoto.HitokotoQuoteModule
import com.crossbowffs.quotelock.data.modules.jinrishici.JinrishiciQuoteModule
import com.crossbowffs.quotelock.data.modules.libquotes.LibquotesQuoteModule
import com.crossbowffs.quotelock.data.modules.natune.NatuneQuoteModule
import com.crossbowffs.quotelock.data.modules.wikiquote.WikiquoteQuoteModule

object Modules {
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

    private fun getModule(className: String): QuoteModule {
        val module = sModules[className]
        return module ?: throw ModuleNotFoundException("Module not found for class: $className")
    }

    fun values(): List<QuoteModule> {
        return ArrayList(sModules.values)
    }

    operator fun get(className: String): QuoteModule = getModule(className)

    operator fun contains(className: String): Boolean = sModules.containsKey(className)
}