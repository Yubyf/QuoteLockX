package com.crossbowffs.quotelock.provider;

import com.crossbowffs.quotelock.BuildConfig;
import com.crossbowffs.quotelock.consts.PrefKeys;
import com.crossbowffs.remotepreferences.RemotePreferenceProvider;

public class QuoteProvider extends RemotePreferenceProvider {
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".preferences";

    public QuoteProvider() {
        super(AUTHORITY, new String[] {PrefKeys.PREF_QUOTES});
    }

    @Override
    protected boolean checkAccess(String prefName, String prefKey, boolean write) {
        return !write;
    }
}
