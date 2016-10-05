package com.crossbowffs.quotelock.modules;

public class ModuleNotFoundException extends RuntimeException {
    public ModuleNotFoundException(String detailMessage) {
        super(detailMessage);
    }
}
