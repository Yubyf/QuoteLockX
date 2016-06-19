package com.crossbowffs.quotelock.modules;

import com.crossbowffs.quotelock.api.QuoteModule;
import com.crossbowffs.quotelock.modules.custom.CustomQuoteModule;
import com.crossbowffs.quotelock.modules.goodreads.GoodreadsQuoteModule;
import com.crossbowffs.quotelock.modules.hitokoto.HitokotoQuoteModule;
import com.crossbowffs.quotelock.modules.vnaas.VnaasQuoteModule;
import com.crossbowffs.quotelock.modules.wikiquote.WikiquoteQuoteModule;

import java.util.*;

public class ModuleManager {
    private static final List<Class<? extends QuoteModule>> sModules = new ArrayList<>();
    private static final Map<Class<? extends QuoteModule>, QuoteModule> sInstanceCache = new WeakHashMap<>();

    static {
        addModule(VnaasQuoteModule.class);
        addModule(HitokotoQuoteModule.class);
        addModule(GoodreadsQuoteModule.class);
        addModule(WikiquoteQuoteModule.class);
        addModule(CustomQuoteModule.class);
    }

    private static void addModule(Class<? extends QuoteModule> module) {
        sModules.add(module);
    }

    private static QuoteModule getOrCreateModule(Class<? extends QuoteModule> moduleCls) {
        QuoteModule module = sInstanceCache.get(moduleCls);
        if (module == null) {
            try {
                module = moduleCls.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new AssertionError(e);
            }
            sInstanceCache.put(moduleCls, module);
        }
        return module;
    }

    public static QuoteModule getModule(String className) {
        for (Class<? extends QuoteModule> cls : sModules) {
            if (cls.getName().equals(className)) {
                return getOrCreateModule(cls);
            }
        }
        throw new AssertionError("Module not found for class: " + className);
    }

    public static List<QuoteModule> getAllModules() {
        ArrayList<QuoteModule> modules = new ArrayList<>();
        for (Class<? extends QuoteModule> cls : sModules) {
            modules.add(getOrCreateModule(cls));
        }
        return modules;
    }
}
