package com.crossbowffs.quotelock.modules;

import android.content.Context;
import com.crossbowffs.quotelock.api.QuoteModule;
import com.crossbowffs.quotelock.modules.custom.CustomQuoteModule;
import com.crossbowffs.quotelock.modules.freakuotes.FreakuotesQuoteModule;
import com.crossbowffs.quotelock.modules.goodreads.GoodreadsQuoteModule;
import com.crossbowffs.quotelock.modules.hitokoto.HitokotoQuoteModule;
import com.crossbowffs.quotelock.modules.vnaas.VnaasQuoteModule;
import com.crossbowffs.quotelock.modules.wikiquote.WikiquoteQuoteModule;

import java.util.*;

public class ModuleManager {
    private static final Map<String, QuoteModule> sModules = new HashMap<>();

    static {
        addLocalModule(VnaasQuoteModule.class);
        addLocalModule(HitokotoQuoteModule.class);
        addLocalModule(GoodreadsQuoteModule.class);
        addLocalModule(WikiquoteQuoteModule.class);
        addLocalModule(FreakuotesQuoteModule.class);
        addLocalModule(CustomQuoteModule.class);
    }

    private static void addLocalModule(Class<? extends QuoteModule> moduleCls) {
        String className = moduleCls.getName();
        sModules.put(className, newLocalModule(moduleCls));
    }

    private static QuoteModule newLocalModule(Class<? extends QuoteModule> moduleCls) {
        try {
            return moduleCls.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    public static QuoteModule getModule(Context context, String className) {
        QuoteModule module = sModules.get(className);
        if (module != null) {
            return module;
        }
        throw new AssertionError("Module not found for class: " + className);
    }

    public static List<QuoteModule> getAllModules(Context context) {
        return new ArrayList<>(sModules.values());
    }
}
