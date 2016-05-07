package com.crossbowffs.quotelock.provider;

import com.crossbowffs.quotelock.preferences.PrefKeys;
import com.crossbowffs.remotepreferences.RemotePreferenceProvider;

public class QuoteProvider extends RemotePreferenceProvider {
    public static final String AUTHORITY = "com.crossbowffs.quotelock.preferences";

    public QuoteProvider() {
        super(AUTHORITY, new String[] {PrefKeys.PREF_QUOTES});
    }

    @Override
    protected boolean checkAccess(String prefName, String prefKey, boolean write) {
        return !write;
    }
}
