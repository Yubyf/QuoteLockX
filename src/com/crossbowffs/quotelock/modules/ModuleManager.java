package com.crossbowffs.quotelock.modules;

import com.crossbowffs.quotelock.api.QuoteModule;
import com.crossbowffs.quotelock.modules.vnaas.VnaasQuoteModule;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private static final List<Class<? extends QuoteModule>> sModules = new ArrayList<>();

    static {
        addModule(VnaasQuoteModule.class);
    }

    private static void addModule(Class<? extends QuoteModule> module) {
        sModules.add(module);
    }

    public static QuoteModule getModule(String className) {
        for (Class<?> cls : sModules) {
            if (cls.getName().equals(className)) {
                try {
                    return (QuoteModule)cls.newInstance();
                } catch (InstantiationException | IllegalAccessException | ClassCastException e) {
                    throw new AssertionError(e);
                }
            }
        }
        throw new AssertionError("Module not found for class: " + className);
    }

    public static List<QuoteModule> getAllModules() {
        ArrayList<QuoteModule> modules = new ArrayList<>();
        for (Class<?> cls : sModules) {
            try {
                modules.add((QuoteModule)cls.newInstance());
            } catch (InstantiationException | IllegalAccessException | ClassCastException e) {
                throw new AssertionError(e);
            }
        }
        return modules;
    }
}
