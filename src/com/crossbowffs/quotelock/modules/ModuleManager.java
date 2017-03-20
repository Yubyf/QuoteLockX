package com.crossbowffs.quotelock.modules;

import android.content.Context;
import com.crossbowffs.quotelock.api.QuoteModule;
import com.crossbowffs.quotelock.modules.brainyquote.BrainyquoteQuoteModule;
import com.crossbowffs.quotelock.modules.custom.CustomQuoteModule;
import com.crossbowffs.quotelock.modules.freakuotes.FreakuotesQuoteModule;
import com.crossbowffs.quotelock.modules.goodreads.GoodreadsQuoteModule;
import com.crossbowffs.quotelock.modules.hitokoto.HitokotoQuoteModule;
import com.crossbowffs.quotelock.modules.natune.NatuneQuoteModule;
import com.crossbowffs.quotelock.modules.vnaas.VnaasQuoteModule;
import com.crossbowffs.quotelock.modules.wikiquote.WikiquoteQuoteModule;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ModuleManager {
    private static final Map<String, QuoteModule> sModules = new LinkedHashMap<>();

    static {
        addLocalModule(new VnaasQuoteModule());
        addLocalModule(new HitokotoQuoteModule());
        addLocalModule(new GoodreadsQuoteModule());
        addLocalModule(new WikiquoteQuoteModule());
        addLocalModule(new FreakuotesQuoteModule());
        addLocalModule(new NatuneQuoteModule());
        addLocalModule(new BrainyquoteQuoteModule());
        addLocalModule(new CustomQuoteModule());
    }

    private static void addLocalModule(QuoteModule module) {
        String className = module.getClass().getName();
        sModules.put(className, module);
    }

    public static QuoteModule getModule(Context context, String className) {
        QuoteModule module = sModules.get(className);
        if (module != null) {
            return module;
        }
        throw new ModuleNotFoundException("Module not found for class: " + className);
    }

    public static List<QuoteModule> getAllModules(Context context) {
        return new ArrayList<>(sModules.values());
    }
}
