package com.crossbowffs.quotelock.provider;

import android.database.Cursor;
import android.net.Uri;

import com.crossbowffs.quotelock.BuildConfig;
import com.crossbowffs.quotelock.consts.PrefKeys;
import com.crossbowffs.remotepreferences.RemotePreferenceProvider;

public class PreferenceProvider extends RemotePreferenceProvider {
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".preferences";

    public PreferenceProvider() {
        super(AUTHORITY, new String[] {PrefKeys.PREF_COMMON, PrefKeys.PREF_QUOTES});
    }

    @Override
    protected boolean checkAccess(String prefName, String prefKey, boolean write) {
        return !write;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return super.query(uri, projection, selection, selectionArgs, sortOrder);
    }
}
